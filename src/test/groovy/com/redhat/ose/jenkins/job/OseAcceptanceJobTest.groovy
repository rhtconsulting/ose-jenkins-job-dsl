package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

@Mixin(JobSpecMixin)
class OseAcceptanceJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test acceptance job"() {
		
		given:
		OseAcceptanceJob job = new OseAcceptanceJob(
			jobName: "ose-acceptance",
			acceptanceUrl: "http://example.com/rest/valicate",
			downstreamProject: "promote-prod"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedJobs.size() == 1
		
		Job referencedJob = jobParent.referencedJobs.first()
		
		
		// Print out resulting xml for debug testing
		//println referencedJob.xml
		
		referencedJob.name == 'ose-acceptance'
		Node project = referencedJob.node
		
		with(project) {
			
			// Core Job Properties
			description.text() == "OSE Acceptance Project - ose-acceptance"
			keepDependencies.text() == 'false'
			disabled.text() == 'false'
			blockBuildWhenDownstreamBuilding.text() == 'false'
			blockBuildWhenUpstreamBuilding.text() == 'false'
			
			// Validate Next Pipeline Step
			with(publishers[0].'au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger') {
				downstreamProjectNames.text() == "promote-prod"
			}
			
			
			equalsIgnoreWhitespace builders."hudson.tasks.Shell"[0].command.text(), '''
				set +x

				MAX_ATTEMPTS=100
				DELAY=5
				COUNTER=0

				echo -n "Attempting to Verify Application "
				
				while [ \$COUNTER -lt \$MAX_ATTEMPTS ]
				do
				
					RESPONSE=\$(curl -s -o /dev/null -w '%{http_code}\\n' \$ACCEPTANCE_URL)
					
					if [ \$RESPONSE -eq 200 ]; then
						echo 
						echo 
						echo "Application Verified"
						exit 0
					fi
				
				    echo -n "."
					COUNTER=\$(( \$COUNTER + 1 ))
					
					sleep \$DELAY
					
				done
				
				echo 
				echo "Error: Application Could Not Be Verified After \$COUNTER Attempts"
        '''


		}
		
		
		// Validate build parameters
		with(project.'properties'.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'hudson.model.StringParameterDefinition') {
		
			it[0].name.text() == 'BUILD_PROJECT_VERSION'
			it[0].defaultValue.text() == ''
			it[0].description.text() == 'Build version of the Project'
			
			
			it[1].name.text() == 'ACCEPTANCE_URL'
			it[1].defaultValue.text() == 'http://example.com/rest/valicate'
			it[1].description.text() == 'URL of the Endpoint to Test'
		
		}
				
		
	}


}

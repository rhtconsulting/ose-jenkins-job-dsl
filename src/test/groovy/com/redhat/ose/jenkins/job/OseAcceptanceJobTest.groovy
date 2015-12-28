package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests and validates the creation of the acceptance job
 *
 */
@Mixin(JobSpecMixin)
class OseAcceptanceJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test acceptance job"() {
		
		given:
		OseAcceptanceJob job = new OseAcceptanceJob(
			appName: "ose-app",
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
				
				with(configs.'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters') {
					properties.text() == "BUILD_PROJECT_VERSION=\$BUILD_PROJECT_VERSION"
				}
			}
			
			
			equalsIgnoreWhitespace builders."hudson.tasks.Shell"[0].command.text(), '''
				set +x

				if [ ! -z \$ACCEPTANCE_URL ]; then

					MAX_ATTEMPTS=100
					DELAY=5
					COUNTER=0
	
					echo "Attempting to Verify Application "
					
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
				else
					echo "No acceptance url specified. Bypassing acceptance check"
				fi 	
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
	
	def "slack test acceptance job"() {
		
		given:
		OseAcceptanceJob job = new OseAcceptanceJob(
			appName: "ose-app",
			jobName: "ose-acceptance",
			acceptanceUrl: "http://example.com/rest/valicate",
			downstreamProject: "promote-prod",
			slackTokenCredential: "slack-token",
			slackChannelName: "#ose-slack"
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
				
				with(configs.'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters') {
					properties.text() == "BUILD_PROJECT_VERSION=\$BUILD_PROJECT_VERSION"
				}
			}
			
			
			equalsIgnoreWhitespace builders."hudson.tasks.Shell"[0].command.text(), '''
				set +x
				
				if [ ! -z \$ACCEPTANCE_URL ]; then

					MAX_ATTEMPTS=100
					DELAY=5
					COUNTER=0
	
					echo "Attempting to Verify Application "
					
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
				else
					echo "No acceptance url specified. Bypassing acceptance check"
				fi 
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
		
		// Credential Binding
		with(project.buildWrappers.'org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper'.bindings.'org.jenkinsci.plugins.credentialsbinding.impl.StringBinding'[0]) {
			variable.text() == "SLACK_TOKEN"
			credentialsId.text() == "slack-token"
		}
		
		// Chat Post Build
		with(project.publishers[0].'org.jenkinsci.plugins.postbuildscript.PostBuildScript') {
			scriptOnlyIfSuccess.text() == "true"
			scriptOnlyIfFailure.text() == "false"
			markBuildUnstable.text() == "false"
			
			equalsIgnoreWhitespace buildSteps."hudson.tasks.Shell".command.text(), '''
                    set +x

					echo "Posting update to slack"

                    curl -s https://slack.com/api/chat.postMessage --data-urlencode token=\${SLACK_TOKEN} --data-urlencode channel="#ose-slack" --data-urlencode "username=jenkins" --data-urlencode "icon_url=http://www.yolinux.com/TUTORIALS/images/Jenkins/Jenkins_logo.png" --data-urlencode "attachments=[{\\"fallback\\": \\"ose-app \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT - http://10.3.9.43/jenkins/\\",\\"pretext\\": \\"Jenkins ose-app Notification\\",\\"title\\": \\"ose-app \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT\\",\\"title_link\\": \\"http://10.3.9.43/jenkins\\",\\"text\\": \\"Click the link above to view the job in Jenkins\\",\\"color\\": \\"#7CD197\\"}]" > /dev/null

           '''
			
		}
				
		
	}


}

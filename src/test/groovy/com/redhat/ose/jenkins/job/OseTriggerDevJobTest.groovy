package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests and validates the creation of the developement trigger job
 *
 */
@Mixin(JobSpecMixin)
class OseTriggerDevJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test ose trigger dev job"() {
		
		given:
		OseTriggerDevJob job = new OseTriggerDevJob(
			jobName: "ose-trigger-dev",
			oseMaster: "master.ose.example.com",
			oseProject: "jenkins-dsl",
			oseApp: "dsl",
			gitOwner: "rhtconsulting",
			gitProject: "ose-jenkins-dsl",
			oseTokenCredential: "ose-token-dev",
			downstreamProject: "acceptance",
			srcAppUrl: "http://cicd.rhc-ose.labs.redhat.com/nexus/service/local/artifact/maven/redirect?r=public&g=com.redhat&a=app-build-timestamp&v=\${BUILD_PROJECT_VERSION}&p=war"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedJobs.size() == 1
		
		Job referencedJob = jobParent.referencedJobs.first()
		
		
		// Print out resulting xml for debug testing
		//println referencedJob.xml
		
		referencedJob.name == 'ose-trigger-dev'
		Node project = referencedJob.node
		
		with(project) {
			
			// Core Job Properties
			description.text() == "OSE Trigger Dev Project - ose-trigger-dev"
			keepDependencies.text() == 'false'
			disabled.text() == 'false'
			blockBuildWhenDownstreamBuilding.text() == 'false'
			blockBuildWhenUpstreamBuilding.text() == 'false'
			
			with(scm) {
				configVersion.text() == '2'
				skipTag.text() == 'true'
				userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.url.text() == 'https://github.com/rhtconsulting/ose-jenkins-dsl.git'
				branches.'hudson.plugins.git.BranchSpec'.name.text() == 'master'
				doGenerateSubmoduleConfigurations.text() == 'false'
				with(browser) {
					it[0].attribute('class') == 'hudson.plugins.git.browser.GithubWeb'
					url.text() == 'https://github.com/rhtconsulting/ose-jenkins-dsl/'
				}
			}
			
			with(publishers[0].'hudson.plugins.parameterizedtrigger.BuildTrigger'.'configs'.'hudson.plugins.parameterizedtrigger.BuildTriggerConfig') {
				projects.text() == 'acceptance'
				condition.text() == 'SUCCESS'
				triggerWithNoParameters.text() == 'false'
				
				with(configs.'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters') {
					properties.text() == 'BUILD_PROJECT_VERSION=\${BUILD_PROJECT_VERSION}'
				}
			}

			
			// Credential Binding
			with(buildWrappers.'org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper'.bindings.'org.jenkinsci.plugins.credentialsbinding.impl.StringBinding') {
				variable.text() == "OSE_SERVICE_ACCOUNT_TOKEN"
				credentialsId.text() == "e72e16c7e42f292c6912e7710c838347ae178b4a"
			}
			
			equalsIgnoreWhitespace builders."hudson.tasks.Shell"[0].command.text(), '''
                  set +x 

                  echo "Triggering Dev Build of jenkins-dsl ${BUILD_PROJECT_VERSION}"

                  sh ${WORKSPACE}/ose-update-src-app-url.sh -h=\${OSE_HOST} -t=\${OSE_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT} -a=\${OSE_APP} -s="http://cicd.rhc-ose.labs.redhat.com/nexus/service/local/artifact/maven/redirect?r=public&g=com.redhat&a=app-build-timestamp&v=${BUILD_PROJECT_VERSION}&p=war" 

                  echo "Building Image in OSE"

                  sh ${WORKSPACE}/trigger-monitor-build.sh -h=\${OSE_HOST} -t=\${OSE_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT} -a=\${OSE_APP}

        '''


		}
		
		
		// Validate build parameters
		with(project.'properties'.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'hudson.model.StringParameterDefinition') {
		
			it[0].name.text() == 'BUILD_PROJECT_VERSION'
			it[0].defaultValue.text() == ''
			it[0].description.text() == 'Build version of the Project'
			
			
			it[1].name.text() == 'OSE_HOST'
			it[1].defaultValue.text() == 'master.ose.example.com'
			it[1].description.text() == 'OpenShift hostname'

			it[2].name.text() == 'OSE_PROJECT'
			it[2].defaultValue.text() == 'jenkins-dsl'
			it[2].description.text() == 'Name of the OpenShift project'
			
			it[3].name.text() == 'OSE_APP'
			it[3].defaultValue.text() == 'dsl'
			it[3].description.text() == 'Name of the OpenShift application'	
		
		}
				
		
	}


}

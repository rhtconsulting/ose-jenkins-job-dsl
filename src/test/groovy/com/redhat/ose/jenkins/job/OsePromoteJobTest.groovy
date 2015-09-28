package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

@Mixin(JobSpecMixin)
class OsePromoteJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test ose promote job"() {
		
		given:
		OsePromoteJob job = new OsePromoteJob(
			jobName: "ose-promote",
			gitOwner: "rhtconsulting",
			gitProject: "ose-jenkins-dsl",
			oseSrcTokenCredential: "ose-token-dev",
			oseDestTokenCredential: "ose-token-prod",
			oseRegistrySrc: "registry.dev.ose.example.com",
            oseRegistryDest: "registry.prod.ose.example.com",
			oseProjectSrc: "jenkins-dsl",
			oseProjectDest: "jenkins-dsl",
			oseAppSrc: "dsl-app",
			oseAppDest: "dsl-app",
			downstreamProject: "ose-promote-next"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedJobs.size() == 1
		
		Job referencedJob = jobParent.referencedJobs.first()
		
		
		// Print out resulting xml for debug testing
		//println referencedJob.xml
		
		referencedJob.name == 'ose-promote'
		Node project = referencedJob.node
		
		with(project) {
			
			// Core Job Properties
			description.text() == "OSE Promote App - ose-promote"
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
	
			// Credential Binding
			with(buildWrappers.'org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper'.bindings.'org.jenkinsci.plugins.credentialsbinding.impl.StringBinding'[0]) {
				variable.text() == "OSE_SRC_SERVICE_ACCOUNT_TOKEN"
				credentialsId.text() == "e72e16c7e42f292c6912e7710c838347ae178b4a"
			}
			
			// Test workspace cleanup
			with(buildWrappers['hudson.plugins.ws__cleanup.PreBuildCleanup']) {
					deleteDirs.text() == 'true'
			}
			
			equalsIgnoreWhitespace builders."hudson.tasks.Shell"[0].command.text(), '''
                  set +x 

                  # Pull Image from Source Environment

                  sh \${WORKSPACE}/ose-docker-push-pull-operations.sh -o=pull -h=\${OSE_REGISTRY_SRC} -u=jenkins -t=\${OSE_SRC_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT_SRC} -a=\${OSE_APP_SRC} 

                  # Tag Image for Destination Environment

                 docker tag -f \${OSE_REGISTRY_SRC}/\${OSE_PROJECT_SRC}/\${OSE_APP_SRC} \${OSE_REGISTRY_DEST}/\${OSE_PROJECT_DEST}/\${OSE_APP_DEST}

                 # Push Image to Destination Environment 

                 sh \${WORKSPACE}/ose-docker-push-pull-operations.sh -o=push -h=\${OSE_REGISTRY_DEST} -u=jenkins -t=\${OSE_DEST_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT_DEST} -a=\${OSE_APP_DEST}

                 # Delete Images

                 docker rmi \${OSE_REGISTRY_SRC}/\${OSE_PROJECT_SRC}/\${OSE_APP_SRC} 

                 docker rmi \${OSE_REGISTRY_DEST}/\${OSE_PROJECT_DEST}/\${OSE_APP_DEST}
        '''


		}
		
		// Validate Next Pipeline Step
		with(project.publishers[0].'au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger') {
			downstreamProjectNames.text() == "ose-promote-next"
		}
		
		// Validate build parameters
		with(project.'properties'.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'hudson.model.StringParameterDefinition') {

			it[0].name.text() == 'BUILD_PROJECT_VERSION'
			it[0].defaultValue.text() == ''
			it[0].description.text() == 'Build version of the Project'

			
			it[1].name.text() == 'OSE_REGISTRY_SRC'
			it[1].defaultValue.text() == 'registry.dev.ose.example.com'
			it[1].description.text() == 'OpenShift Source Environment Host'
			
			
			it[2].name.text() == 'OSE_REGISTRY_DEST'
			it[2].defaultValue.text() == 'registry.prod.ose.example.com'
			it[2].description.text() == 'OpenShift Destination Environment Host'

			it[3].name.text() == 'OSE_PROJECT_SRC'
			it[3].defaultValue.text() == 'jenkins-dsl'
			it[3].description.text() == 'OpenShift Project for the Source Environment'
			
			it[4].name.text() == 'OSE_APP_SRC'
			it[4].defaultValue.text() == 'dsl-app'
			it[4].description.text() == 'OpenShift App for the Source Environment'	
			
			it[5].name.text() == 'OSE_PROJECT_DEST'
			it[5].defaultValue.text() == 'jenkins-dsl'
			it[5].description.text() == 'OpenShift Project for the Destination Environment'
			
			it[6].name.text() == 'OSE_APP_DEST'
			it[6].defaultValue.text() == 'dsl-app'
			it[6].description.text() == 'OpenShift App for the Destination Environment'
	
		}
				
		
	}


}

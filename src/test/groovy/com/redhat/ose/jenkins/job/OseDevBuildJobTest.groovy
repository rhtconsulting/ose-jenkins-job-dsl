package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.JenkinsDslConstants;

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests and validates the creation of the build job
 *
 */
@Mixin(JobSpecMixin)
class OseDevBuildJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test ose dev build job"() {
		
		given:
		OseDevBuildJob job = new OseDevBuildJob(
			jobName: "ose-dev-build",
			gitOwner: "rhtconsulting",
			gitProject: "ose-app",
			mavenDeployServerId: JenkinsDslConstants.MAVEN_DEPLOY_DEFAULT_SERVER_ID,
			mavenDeployRepoUrl: JenkinsDslConstants.MAVEN_DEPLOY_DEFAULT_REPO_URL,
			downstreamProject: "ose-trigger-dev",
			scmPollSchedule: "* * * * *"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedJobs.size() == 1
		
		Job referencedJob = jobParent.referencedJobs.first()
		
		
		// Print out resulting xml for debug testing
		//println referencedJob.xml
		
		referencedJob.name == 'ose-dev-build'
		Node project = referencedJob.node
		
		with(project) {
			
			// Core Job Properties
			description.text() == "OSE Development Build - ose-dev-build"
			keepDependencies.text() == 'false'
			disabled.text() == 'false'
			blockBuildWhenDownstreamBuilding.text() == 'false'
			blockBuildWhenUpstreamBuilding.text() == 'false'
			goals.text() == "clean package"
			rootPOM.text() == "pom.xml"
			
			with(scm) {
				configVersion.text() == '2'
				skipTag.text() == 'true'
				userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.url.text() == 'https://github.com/rhtconsulting/ose-app.git'
				branches.'hudson.plugins.git.BranchSpec'.name.text() == 'master'
				doGenerateSubmoduleConfigurations.text() == 'false'
				with(browser) {
					it[0].attribute('class') == 'hudson.plugins.git.browser.GithubWeb'
					url.text() == 'https://github.com/rhtconsulting/ose-app/'
				}
			}
			
			with(prebuilders.'hudson.tasks.Maven') {
				targets.text() == 'build-helper:parse-version versions:set'
				mavenName.text() == 'maven'
				pom.text() == "pom.xml"
				properties.text() == 'newVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}-${BUILD_NUMBER}'
			}
			
			with(publishers[0].'hudson.maven.RedeployPublisher') {
				id.text() == 'nexus'
				url.text() == 'http://127.0.0.1:8081/nexus/content/repositories/releases'
				uniqueVersion.text() == 'true'
				evenIfUnstable.text() == 'false'
			}
			
			with(publishers[0].'hudson.plugins.parameterizedtrigger.BuildTrigger'.'configs'.'hudson.plugins.parameterizedtrigger.BuildTriggerConfig') {
				projects.text() == 'ose-trigger-dev'
				condition.text() == 'SUCCESS'
				triggerWithNoParameters.text() == 'false'
				
				with(configs.'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters') {
					properties.text() == 'BUILD_PROJECT_VERSION=\${POM_VERSION}'
				}
			}
			
			with(triggers.'hudson.triggers.SCMTrigger') {
				spec.text() == "* * * * *"
			}
				
		}
		
	}


}

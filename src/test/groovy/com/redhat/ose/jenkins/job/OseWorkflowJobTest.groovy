package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import spock.lang.Shared
import spock.lang.Specification

import com.redhat.ose.jenkins.JenkinsDslConstants

/**
 * Tests and validates the creation of the acceptance job
 *
 */
@Mixin(JobSpecMixin)
class OseWorkflowJobTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test acceptance job"() {
		
		given:
		OseWorkflowJob job = new OseWorkflowJob(
			appName: "ose-app",
			jobName: "ose-workflow",
			acceptanceUrl: "http://example.com/rest/valicate",
			appGitOwner: "rhtconsulting",
			appGitProject: "ose-app",
			mavenDeployServerId: JenkinsDslConstants.MAVEN_DEPLOY_DEFAULT_SERVER_ID,
			mavenDeployRepoUrl: JenkinsDslConstants.MAVEN_DEPLOY_DEFAULT_REPO_URL,
			scmPollSchedule: "* * * * *",
			devOseMaster: "master.ose.example.com",
			devOseProject: "jenkins-dsl",
			devOseApp: "dsl",
			utilGitOwner: "rhtconsulting",
			utilGitProject: "ose-jenkins-dsl",
			devOseTokenCredential: "ose-token-dev",
			srcAppUrl: "http://cicd.rhc-ose.labs.redhat.com/nexus/service/local/artifact/maven/redirect?r=public&g=com.redhat&a=app-build-timestamp&v=\${BUILD_PROJECT_VERSION}&p=war",
			promoteUatOseSrcTokenCredential: "ose-token-dev",
			promoteUatOseDestTokenCredential: "ose-token-uat",
			promoteUatOseRegistrySrc: "registry.dev.ose.example.com",
			promoteUatOseRegistryDest: "registry.uat.ose.example.com",
			promoteUatOseProjectSrc: "jenkins-dsl",
			promoteUatOseProjectDest: "jenkins-dsl",
			promoteUatOseAppSrc: "dsl-app",
			promoteUatOseAppDest: "dsl-app",
			promoteProdOseSrcTokenCredential: "ose-token-uat",
			promoteProdOseDestTokenCredential: "ose-token-prod",
			promoteProdOseRegistrySrc: "registry.uat.ose.example.com",
			promoteProdOseRegistryDest: "registry.prod.ose.example.com",
			promoteProdOseProjectSrc: "jenkins-dsl",
			promoteProdOseProjectDest: "jenkins-dsl",
			promoteProdOseAppSrc: "dsl-app",
			promoteProdOseAppDest: "dsl-app",
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedJobs.size() == 1
		
		Job referencedJob = jobParent.referencedJobs.first()
		
		
		// Print out resulting xml for debug testing
		//println referencedJob.xml
		
		referencedJob.name == 'ose-workflow'
		Node project = referencedJob.node
		
		
		with(project) {
			
			// Core Job Properties
			description.text() == "OSE Workflow - ose-workflow"
			keepDependencies.text() == 'false'
		}
		
		// Validate build parameters
		with(project.'triggers'.'hudson.triggers.SCMTrigger') {
		
			spec.text() == "* * * * *"
			ignorePostCommitHooks.text() == "false"
		
		}
				
		
	}
	

}

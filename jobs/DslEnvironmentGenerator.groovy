import groovy.json.JsonSlurper

import com.redhat.ose.jenkins.job.OseAcceptanceJob
import com.redhat.ose.jenkins.job.OseDeliveryPipelineView
import com.redhat.ose.jenkins.job.OseDevBuildJob
import com.redhat.ose.jenkins.job.OsePromoteJob
import com.redhat.ose.jenkins.job.OseTriggerDevJob


def jobParent = this

String file = jobParent.binding.variables["FILE_NAME"]

if (file == null) {
	throw new Exception("Input file is null")
}

def resource = null

if(file.startsWith("http") || file.startsWith("https")) {
	resource = new URL(file)
}
else {
	resource = new File(file)
}


def items = new JsonSlurper().parseText(resource.text)

items.each {
	
	def jsonName = it.name
	def jsonGitbranch = it.gitBranch ?: "master"
	def jsonGitProject = it.gitProject
	def jsonGitOwner = it.gitOwner
	def jsonUtilGitbranch = it.utilGitBranch ?: "master"
	def jsonUtilGitProject = it.utilGitProject
	def jsonUtilGitOwner = it.utilGitOwner
	def jsonScmPollSchedule = it.scmPollSchedule
	def jsonMavenDeployRepoUrl = it.mavenDeployRepoUrl
	def jsonMavenDeployServerId = it.mavenDeployServerId
	def jsonOseDevTokenCredential = it.oseDevTokenCredential
	def jsonOseUatTokenCredential = it.oseUatTokenCredential
	def jsonOseProdTokenCredential = it.oseProdTokenCredential
	def jsonOseRegistryDev = it.oseRegistryDev
	def jsonOseRegistryUat = it.oseRegistryUat
	def jsonOseRegistryProd = it.oseRegistryProd
	def jsonOseProjectDev = it.oseProjectDev
	def jsonOseProjectUat = it.oseProjectUat
	def jsonOseProjectProd = it.oseProjectProd
	def jsonOseAppDev = it.oseAppDev
	def jsonOseAppUat = it.oseAppUat
	def jsonOseAppProd = it.oseAppProd
	def jsonOseDevMaster = it.oseDevMaster
	def jsonSrcAppUrl = it.srcAppUrl
	def jsonMavenRootPom = it.mavenRootPom
	def jsonMavenGoals = it.mavenGoals
	def jsonAcceptanceurl = it.acceptanceUrl
	def jsonSlackTokenCredential = it.slackTokenCredential
	def jsonSlackChannelName = it.slackChannelName
	

	// Create Prod Promotion
	new OsePromoteJob(
		jobName: "${jsonName}-promote-prod",
		gitOwner: "${jsonUtilGitOwner}",
		gitProject: "${jsonUtilGitProject}",
		oseSrcTokenCredential: "${jsonOseUatTokenCredential}",
		oseDestTokenCredential: "${jsonOseProdTokenCredential}",
		oseRegistrySrc: "${jsonOseRegistryUat}",
		oseRegistryDest: "${jsonOseRegistryProd}",
		oseProjectSrc: "${jsonOseProjectUat}",
		oseProjectDest: "${jsonOseProjectProd}",
		oseAppSrc: "${jsonOseAppUat}",
		oseAppDest: "${jsonOseAppProd}"
	).create(jobParent)

	// Create Uat Promotion
	new OsePromoteJob(
		jobName: "${jsonName}-promote-uat",
		gitOwner: "${jsonUtilGitOwner}",
		gitProject: "${jsonUtilGitProject}",
		oseSrcTokenCredential: "${jsonOseDevTokenCredential}",
		oseDestTokenCredential: "${jsonOseUatTokenCredential}",
		oseRegistrySrc: "${jsonOseRegistryDev}",
		oseRegistryDest: "${jsonOseRegistryUat}",
		oseProjectSrc: "${jsonOseProjectDev}",
		oseProjectDest: "${jsonOseProjectUat}",
		oseAppSrc: "${jsonOseAppDev}",
		oseAppDest: "${jsonOseAppUat}",
		downstreamProject: "${jsonName}-promote-prod",
	).create(jobParent)
	
	
	// Create Acceptance Test
	new OseAcceptanceJob(
		appName: jsonName,
		jobName: "${jsonName}-acceptance",
		acceptanceUrl: jsonAcceptanceurl,
		downstreamProject: "${jsonName}-promote-uat",
		slackTokenCredential: jsonSlackTokenCredential,
		slackChannelName: jsonSlackChannelName
	).create(jobParent)
	
	// Create Trigger Job
	new OseTriggerDevJob(
		jobName: "${jsonName}-trigger-dev",
		oseMaster: "${jsonOseDevMaster}",
		oseProject: "${jsonOseProjectDev}",
		oseApp: "${jsonOseAppDev}",
		gitOwner: "${jsonUtilGitOwner}",
		gitProject: "${jsonUtilGitProject}",
		oseTokenCredential: "${jsonOseDevTokenCredential}",
		downstreamProject: "${jsonName}-acceptance",
		srcAppUrl: "${jsonSrcAppUrl}"
	).create(jobParent)
		
	// Create Dev Build
	def oseDevBuildJob = new OseDevBuildJob(
		jobName: "${jsonName}-build",
		gitOwner: jsonGitOwner,
		gitProject: jsonGitProject,
		downstreamProject: "${jsonName}-trigger-dev",
		gitBranch: jsonGitbranch
	)
	
	if(jsonMavenRootPom != null) {
		oseDevBuildJob.rootPom = jsonMavenRootPom
	}
	
	if(jsonMavenGoals != null) {
		oseDevBuildJob.mvnGoals = jsonMavenGoals
	}
	
	if(jsonScmPollSchedule) {
		oseDevBuildJob.scmPollSchedule = jsonScmPollSchedule
	}
	
	if(jsonMavenDeployRepoUrl) {
		oseDevBuildJob.mavenDeployRepoUrl =jsonMavenDeployRepoUrl
	}
	
	if(jsonMavenDeployServerId) {
		oseDevBuildJob.mavenDeployServerId = jsonMavenDeployServerId
	}
	
	oseDevBuildJob.create(jobParent)
	
	
	// Create Delivery Pipeline
	new OseDeliveryPipelineView(
		pipelineName: "${jsonName}",
		viewName: "${jsonName}-delivery-pipeline",
		startJob: "${jsonName}-build"
	).create(jobParent)
	
}
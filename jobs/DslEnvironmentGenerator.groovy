import groovy.json.JsonSlurper

import com.redhat.ose.jenkins.job.OseAcceptanceJob
import com.redhat.ose.jenkins.job.OseDeliveryPipelineView
import com.redhat.ose.jenkins.job.OseDevBuildJob
import com.redhat.ose.jenkins.job.OsePromoteJob
import com.redhat.ose.jenkins.job.OseTriggerDevJob
import com.redhat.ose.jenkins.job.OseWorkflowJob


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
	def jsonFolder = it.folder
	def jsonGitBranch = it.gitBranch ?: "master"
	def jsonGitProject = it.gitProject
	def jsonGitOwner = it.gitOwner
	def jsonUtilGitBranch = it.utilGitBranch ?: "master"
	def jsonUtilGitProject = it.utilGitProject
	def jsonUtilGitOwner = it.utilGitOwner
	def jsonScmPollSchedule = it.scmPollSchedule
	def jsonMavenDeployRepoUrl = it.mavenDeployRepoUrl
	def jsonMavenDeployServerId = it.mavenDeployServerId
	def jsonOseDevTokenCredential = it.oseDevTokenCredential
	def jsonOseUatTokenCredential = it.oseUatTokenCredential
	def jsonOseProdTokenCredential = it.oseProdTokenCredential
	def jsonOseRegistryDev = it.oseRegistryDev
	def jsonOseRegistryUserDev = it.oseRegistryUserDev
	def jsonOseRegistryUat = it.oseRegistryUat
	def jsonOseRegistryUserUat = it.oseRegistryUserUat
	def jsonOseRegistryProd = it.oseRegistryProd
	def jsonOseRegistryUserProd = it.oseRegistryUserProd
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
	def jsonMavenTargets = it.mavenTargets
	def jsonMavenName = it.mavenName
	def jsonAcceptanceurl = it.acceptanceUrl
	def jsonSlackTokenCredential = it.slackTokenCredential
	def jsonSlackChannelName = it.slackChannelName
	def jsonSlackUsername = it.slackUsername
	def jsonCiCdType = it.cicdType ?: "pipeline"
	

	// Determine CICD Type to Build
	if(jsonCiCdType == "pipeline") {
	
		// Create Prod Promotion
		def oseProdPromotionJob = new OsePromoteJob(
			jobName: "${jsonName}-promote-prod",
			folder: jsonFolder,
			gitOwner: jsonUtilGitOwner,
			gitProject: jsonUtilGitProject,
			oseSrcTokenCredential: jsonOseUatTokenCredential,
			oseDestTokenCredential: jsonOseProdTokenCredential,
			oseRegistrySrc: jsonOseRegistryUat,
			oseRegistryDest: jsonOseRegistryProd,
			oseProjectSrc: jsonOseProjectUat,
			oseProjectDest: jsonOseProjectProd,
			oseAppSrc: jsonOseAppUat,
			oseAppDest: jsonOseAppProd
		)
	
		// Create Uat Promotion
		def oseUatPromotionJob = new OsePromoteJob(
			jobName: "${jsonName}-promote-uat",
			folder: jsonFolder,
			gitOwner: jsonUtilGitOwner,
			gitProject: jsonUtilGitProject,
			oseSrcTokenCredential: jsonOseDevTokenCredential,
			oseDestTokenCredential: jsonOseUatTokenCredential,
			oseRegistrySrc: jsonOseRegistryDev,
			oseRegistryDest: jsonOseRegistryUat,
			oseProjectSrc: jsonOseProjectDev,
			oseProjectDest: jsonOseProjectUat,
			oseAppSrc: jsonOseAppDev,
			oseAppDest: jsonOseAppUat,
			downstreamProject: "${jsonName}-promote-prod",
		)
		
		
		// Create Acceptance Test
		def oseAcceptanceJob = new OseAcceptanceJob(
			appName: jsonName,
			folder: jsonFolder,
			jobName: "${jsonName}-acceptance",
			acceptanceUrl: jsonAcceptanceurl,
			downstreamProject: "${jsonName}-promote-uat",
			slackTokenCredential: jsonSlackTokenCredential,
			slackChannelName: jsonSlackChannelName
		)
		
		// Create Trigger Job
		def oseTriggerDevJob = new OseTriggerDevJob(
			jobName: "${jsonName}-trigger-dev",
			folder: jsonFolder,
			oseMaster: jsonOseDevMaster,
			oseProject: jsonOseProjectDev,
			oseApp: jsonOseAppDev,
			gitOwner: jsonUtilGitOwner,
			gitProject: jsonUtilGitProject,
			oseTokenCredential: jsonOseDevTokenCredential,
			downstreamProject: "${jsonName}-acceptance",
			srcAppUrl: "${jsonSrcAppUrl}"
		)
			
		// Create Dev Build
		def oseDevBuildJob = new OseDevBuildJob(
			jobName: "${jsonName}-build",
			folder: jsonFolder,
			gitOwner: jsonGitOwner,
			gitProject: jsonGitProject,
			downstreamProject: "${jsonName}-trigger-dev",
			gitBranch: jsonGitBranch
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
		
		if(jsonGitBranch) {
			oseDevBuildJob.gitBranch = jsonGitBranch
		}
		
		if(jsonMavenTargets) {
			oseDevBuildJob.mavenTargets = jsonMavenTargets
		}
		
		if(jsonMavenName) {
			oseDevBuildJob.mavenName = jsonMavenName
		}
		
		if(jsonUtilGitBranch) {
			oseTriggerDevJob.gitBranch = jsonUtilGitBranch
		}
		
		if(jsonOseRegistryUserDev) {
			oseUatPromotionJob.oseRegistryUserSrc = jsonOseRegistryUserDev
		}

		if(jsonOseRegistryUserUat) {
			oseUatPromotionJob.oseRegistryUserDest = jsonOseRegistryUserUat
		}

		if(jsonOseRegistryUserUat) {
			oseProdPromotionJob.oseRegistryUserSrc = jsonOseRegistryUserUat
		}

		if(jsonOseRegistryUserProd) {
			oseProdPromotionJob.oseRegistryUserDest = jsonOseRegistryUserProd
		}
						
		oseProdPromotionJob.create(jobParent)
		oseUatPromotionJob.create(jobParent)
		oseAcceptanceJob.create(jobParent)
		oseTriggerDevJob.create(jobParent)
		oseDevBuildJob.create(jobParent)
		
		
		// Create Delivery Pipeline
		new OseDeliveryPipelineView(
			pipelineName: jsonName,
			folder: jsonFolder,
			viewName: "${jsonName}-delivery-pipeline",
			startJob: "${jsonName}-build"
		).create(jobParent)
	}
	else if (jsonCiCdType == "workflow") {
		def oseWorkflowJob = new OseWorkflowJob(
			jobName: "${jsonName}-workflow",
			appName: jsonName,
			folder: jsonFolder,
			acceptanceUrl: jsonAcceptanceurl,
			slackTokenCredential: jsonSlackTokenCredential,
			slackChannelName: jsonSlackChannelName,
			appGitOwner: jsonGitOwner,
			appGitProject: jsonGitProject,
			utilGitOwner: jsonUtilGitOwner,
			utilGitProject: jsonUtilGitProject,
			scmPollSchedule: jsonScmPollSchedule,
			mavenDeployRepoUrl: jsonMavenDeployRepoUrl,
			mavenDeployServerId: jsonMavenDeployServerId,
			devOseMaster: jsonOseDevMaster,
			devOseProject: jsonOseProjectDev,
			devOseApp: jsonOseAppDev,
			devOseTokenCredential: jsonOseDevTokenCredential,
			srcAppUrl: jsonSrcAppUrl,
			promoteUatOseRegistrySrc: jsonOseRegistryDev,
			promoteUatOseRegistryDest: jsonOseRegistryUat,
			promoteUatOseProjectSrc: jsonOseProjectDev,
			promoteUatOseProjectDest: jsonOseProjectUat,
			promoteUatOseAppSrc: jsonOseAppDev,
			promoteUatOseAppDest: jsonOseAppUat,
			promoteUatOseSrcTokenCredential: jsonOseDevTokenCredential,
			promoteUatOseDestTokenCredential: jsonOseUatTokenCredential,
			promoteProdOseRegistrySrc: jsonOseRegistryUat,
			promoteProdOseRegistryDest: jsonOseRegistryProd,
			promoteProdOseProjectSrc: jsonOseProjectUat,
			promoteProdOseProjectDest: jsonOseProjectProd,
			promoteProdOseAppSrc: jsonOseAppUat,
			promoteProdOseAppDest: jsonOseAppProd,
			promoteProdOseSrcTokenCredential: jsonOseUatTokenCredential,
			promoteProdOseDestTokenCredential: jsonOseProdTokenCredential,
	
		)
		
		if(jsonSlackUsername) {
			oseWorkflowJob.slackUsername = jsonSlackUsername
		}
		
		if(jsonGitBranch) {
			oseWorkflowJob.appGitBranch = jsonGitBranch
		}
		
		if(jsonUtilGitBranch) {
			oseWorkflowJob.utilGitBranch = jsonUtilGitBranch;
		}
		
		if(jsonMavenTargets) {
			oseWorkflowJob.mavenTargets = jsonMavenTargets
		}
		
		if(jsonMavenName) {
			oseWorkflowJob.mavenName = jsonMavenName
		}
		
		if(jsonMavenGoals) {
			oseWorkflowJob.mavenGoals = jsonMavenGoals
		}
		
		if(jsonMavenRootPom) {
			oseWorkflowJob.rootPom = jsonMavenRootPom
		}
		
		if(jsonUtilGitBranch) {
			oseWorkflowJob.utilGitBranch = jsonUtilGitBranch
		}
		
		if(jsonOseRegistryUserDev) {
			oseWorkflowJob.promoteUatOseUserSrc = jsonOseRegistryUserDev
		}

		if(jsonOseRegistryUserUat) {
			oseWorkflowJob.promoteUatOseUserDest = jsonOseRegistryUserUat
			oseWorkflowJob.promoteProdOseUserSrc = jsonOseRegistryUserUat
		}

		if(jsonOseRegistryUserProd) {
			oseWorkflowJob.promoteProdOseUserDest = jsonOseRegistryUserProd
		}
		
		oseWorkflowJob.create(jobParent)
	}
	else {
		throw new Exception("Unknown CICD Type")
	}
	
}
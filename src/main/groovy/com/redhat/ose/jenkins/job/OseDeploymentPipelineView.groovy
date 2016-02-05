package com.redhat.ose.jenkins.job

/**
 * Create a new Pipeline Deployment View
 *
 */
class OseDeploymentPipelineView {
	
	String pipelineName
	String folder
	String pipelineTitle
	Integer noDisplayedBuilds = 3
	String initialJob
	Boolean manualTrigger = true
	String startJob
	
	def create(jobParent) {

		def pName = folder != null ? "/${folder}/${pipelineName}" : "${pipelineName}"

		jobParent.buildPipelineView("${pName}") {

			title(pipelineTitle)
			displayedBuilds(noDisplayedBuilds)
			alwaysAllowManualTrigger(manualTrigger)
			selectedJob(startJob)
			

		}
		this
	}


}

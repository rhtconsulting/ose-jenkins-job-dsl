package com.redhat.ose.jenkins.job

/**
 * Create a new Pipeline Deployment View
 *
 */
class OseDeploymentPipelineView {
	
	String pipelineName
	String pipelineTitle
	Integer noDisplayedBuilds = 3
	String initialJob
	Boolean manualTrigger = true
	String startJob
	
	def create(jobParent) {
				
		jobParent.buildPipelineView("${pipelineName}") {
			
			title(pipelineTitle)
			displayedBuilds(noDisplayedBuilds)
			alwaysAllowManualTrigger(manualTrigger)
			selectedJob(startJob)
			

		}
		this
	}


}

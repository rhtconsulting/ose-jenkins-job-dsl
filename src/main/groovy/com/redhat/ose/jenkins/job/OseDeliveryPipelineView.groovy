package com.redhat.ose.jenkins.job

/**
 * Create a new Pipeline Deployment View
 *
 */
class OseDeliveryPipelineView {
	

	String viewName
	String folder
	String pipelineName
	Integer noDisplayedBuilds = 3
	String initialJob
	Boolean manualTrigger = true
	String startJob
	
	def create(jobParent) {
			
		def pName = folder != null ? "/${folder}/${viewName}" : "${viewName}"

		jobParent.deliveryPipelineView("${pName}") {
			
			execute {
				it / allowRebuild(true)
				it / allowPipelineStart(true)
				it / showDescription(true)
				it / showPromotions(true)
				it / displayedBuilds(noDisplayedBuilds)
				it / showTotalBuildTime(true)
			}
			

			columns(1)
			enableManualTriggers(manualTrigger)
			pipelineInstances(3)
			updateInterval(2)
			showAggregatedPipeline(true)
			pipelines {
				component(pipelineName, startJob)
			}
			

		}
		this
	}


}

package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.views.DeliveryPipelineView
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests and validates the creation of the delivery pipeline view
 *
 */
@Mixin(JobSpecMixin)
class OseDeliveryPipelineViewTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test ose promote job"() {
		
		given:
		OseDeliveryPipelineView job = new OseDeliveryPipelineView(
			viewName: "ose-promote-view",
			pipelineName: "Promote Build",
			startJob: "ose-build"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedViews.size() == 1
		
		DeliveryPipelineView deliveryPipelineView = jobParent.referencedViews.first()
		
		// Print out resulting xml for debug testing
		//println deliveryPipelineView.xml

		deliveryPipelineView.name == 'ose-promote-view'
		Node project = deliveryPipelineView.getNode()

		with(project) {

			updateInterval.text() == "2"
			allowManualTriggers.text() == "true"
			noOfPipelines.text() == "3"
			showAggregatedPipeline.text() == "true"
			noOfColumns.text() == "1"
			updateInterval.text() == "2"
			allowManualTriggers.text() == "true"
			allowRebuild.text() == "true"
			allowPipelineStart.text() == "true"
			showDescription.text() == "true"
		    showPromotions.text() == "true"
			displayedBuilds.text() == "3"
			showTotalBuildTime.text() == "true"
			with(componentSpecs) {
				with(it."se.diabol.jenkins.pipeline.DeliveryPipelineView_-ComponentSpec") {
					name.text() == "Promote Build"
					firstJob.text() == "ose-build"
				}
			}
		}
		
		
	}

}

package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.BuildBlockerContext;
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.views.BuildPipelineView;
import spock.lang.Shared
import spock.lang.Specification

@Mixin(JobSpecMixin)
class OseDeploymentPipelineViewTest extends Specification {
	
	JobParent jobParent = createJobParent()
	
	@Shared
	JobManagement jobManagement = Mock(JobManagement)
	
	def "test ose promote job"() {
		
		given:
		OseDeploymentPipelineView job = new OseDeploymentPipelineView(
			pipelineName: "ose-promote-view",
			pipelineTitle: "Promote View",
			startJob: "ose-build"
		)
		
		when:
		job.create(jobParent)
		
		then:
		jobParent.referencedViews.size() == 1
		
		BuildPipelineView buildPipelineView = jobParent.referencedViews.first()
		
		
		// Print out resulting xml for debug testing
		//println buildPipelineView.xml
		
		buildPipelineView.name == 'ose-promote-view'
		Node project = buildPipelineView.node

		with(project) {
			selectedJob.text() == "ose-build"
		}
		
		
	}

}

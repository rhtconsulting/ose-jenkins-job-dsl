package com.redhat.ose.jenkins.job

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.MemoryJobManagement;
import javaposse.jobdsl.dsl.ScriptRequest
import spock.lang.Specification

/**
 * Tests the script used to build out the delivery pipeline
 *
 */
class DslEnvironmentSpec extends Specification {

	private final def resourcesDir = new File("jobs")
	
	def 'Verify logic'() {
        		
		def paramMap = [:]
		
        when:
		MemoryJobManagement jm = [
			getOutputStream: { System.out },
			queueJob: {}
		] as MemoryJobManagement
	
	// Set parameters
	jm.parameters.put("FILE_NAME", "src/test/resources/testjobs.json")
	
	ScriptRequest request = new ScriptRequest('DslEnvironmentGenerator.groovy', null, resourcesDir.toURL(), false);
	
        def dsl = DslScriptLoader.runDslEngine(request, jm)
		def jobs = dsl.jobs
		def views = dsl.views
		
        then:
        jobs != null

        jobs.size() == 5
        def jobsIt = jobs.iterator()
		
		jobsIt.next().jobName == 'ose-app-promote-prod'
		jobsIt.next().jobName == 'ose-app-promote-uat'
		jobsIt.next().jobName == 'ose-app-acceptance'
		jobsIt.next().jobName == 'ose-app-trigger-dev'
		jobsIt.next().jobName == 'ose-app-build'
		

				
		views.size() == 1
		views.iterator().next().name == "ose-app-delivery-pipeline"
    }
	
}

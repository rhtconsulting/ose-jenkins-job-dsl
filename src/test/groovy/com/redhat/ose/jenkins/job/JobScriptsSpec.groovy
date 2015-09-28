package com.redhat.ose.jenkins.job

import groovy.io.FileType
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.MemoryJobManagement
import spock.lang.Unroll

/**
 * Tests that all dsl scripts in the jobs directory will compile.
 */
class JobScriptsSpec {

	@Unroll
	void 'test script #file.name'(File file) {
		given:
		JobManagement jm = new MemoryJobManagement()

		when:
		DslScriptLoader.runDslEngine file.text, jm

		then:
		noExceptionThrown()

		where:
		file << jobFiles
	}

	static List<File> getJobFiles() {
		List<File> files = []
		new File('jobs').eachFileRecurse(FileType.FILES) {
			if(it.name.endsWith('.groovy')) {
				files << it
			}
		}
		files
	}

	
}

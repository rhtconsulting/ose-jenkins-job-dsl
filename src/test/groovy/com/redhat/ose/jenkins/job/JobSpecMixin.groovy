package com.redhat.ose.jenkins.job;

import static org.junit.Assert.*
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.MemoryJobManagement


/**
 * Project level Groovy Mixins
 *
 */
class JobSpecMixin {

	JobParent createJobParent() {
		JobParent jp = new JobParent() {
			@Override
			Object run() {
				return null
			}
		}
		
		jp.setJm(new MemoryJobManagement())
		
		jp
		
	}

	boolean equalsIgnoreWhitespace(String s1, String s2) {
		s1.replaceAll(/\s+/, '').trim() == s2.replaceAll(/\s+/, '').trim()
	}
	
}

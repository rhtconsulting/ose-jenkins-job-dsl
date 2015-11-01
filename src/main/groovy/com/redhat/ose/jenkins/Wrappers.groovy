package com.redhat.ose.jenkins


class Wrappers {

	/**
	 * Workspace Cleanup Plugin support
	 * 
	 * @param wrapperContext
	 */
	static void workspacePreBuildCleanup(wrapperContext) {
		wrapperContext.wrapperNodes << new NodeBuilder().'hudson.plugins.ws__cleanup.PreBuildCleanup' {
			deleteDirs(false)
			cleanupParameter()
			externalDelete()
		}

	}

	
	/**
	 * Manage Delivery Plugin Pipeline Version
	 *
	 * @param wrapperContext
	 */
	static void pipelineVersionContributor(wrapperContext) {
		wrapperContext.wrapperNodes << new NodeBuilder().'se.diabol.jenkins.pipeline.PipelineVersionContributor' {
			versionTemplate("\${POM_VERSION}")
			updateDisplayName(true)
		}

	}

	
}

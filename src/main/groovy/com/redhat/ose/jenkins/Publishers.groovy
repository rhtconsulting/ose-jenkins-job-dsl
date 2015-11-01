package com.redhat.ose.jenkins

class Publishers {
	
	
	/**
	 *  Deployment to Maven repository
	 */
	static def deployArtifacts = { String deployId, String deployUrl, publisherContext ->
		def nodeBuilder = new NodeBuilder();
		publisherContext.publisherNodes << nodeBuilder.'hudson.maven.RedeployPublisher' {
			
			if(deployId) {
				id deployId
			}

			url deployUrl
			uniqueVersion(true)
			evenIfUnstable(false)
		}
	}
	
	/**
	 *  Post Build Script
	 */
	static def postbuildScript = { String script, publisherContext ->
		def nodeBuilder = new NodeBuilder();
		publisherContext.publisherNodes << nodeBuilder.'org.jenkinsci.plugins.postbuildscript.PostBuildScript' {
			scriptOnlyIfSuccess(true)
			scriptOnlyIfFailure(false)
			markBuildUnstable(false)
			
			buildSteps {
				"hudson.tasks.Shell" {
					command script
				}
			}
			
			
		}
	}

}

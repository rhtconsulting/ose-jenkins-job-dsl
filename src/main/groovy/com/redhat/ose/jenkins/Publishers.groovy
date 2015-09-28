package com.redhat.ose.jenkins

class Publishers {
	
	
	/**
	 *  Deployment to Maven repository
	 */
	static def deployArtifacts = { String deployId, String deployUrl, publisherContext ->
		def nodeBuilder = new NodeBuilder();
		publisherContext.publisherNodes << nodeBuilder.'hudson.maven.RedeployPublisher' {
			id deployId
			url deployUrl
			uniqueVersion(true)
			evenIfUnstable(false)
		}
	}

}

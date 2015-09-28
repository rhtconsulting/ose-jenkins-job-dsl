package com.redhat.ose.jenkins

class JenkinsDslConstants {
	
	static String GIT_HOST = "http://github.com"
	
	static String NEXUS_REPO_BASE = "http://127.0.0.1:8081/nexus/content/repositories/"
	
	static Map NEXUS_RELEASES_REPO = [
		id: 'nexus',
		url: NEXUS_REPO_BASE + "releases"
	]

}

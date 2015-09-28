package com.redhat.ose.jenkins.job

class CredentialManager {
	
	static def lookupCredential(key) {
		
		def credential
		
		if(key != null) {
			
			switch(key) {
				case "ose-token-dev":
					credential = "e72e16c7e42f292c6912e7710c838347ae178b4a"
					break;
				case "ose-token-prod":
					credential = "F6uI6MnqR_EcATNCds80HZHJiEoZw2Dv8IVtOp35y2c"
					break;
			}
			
		}
		
		credential
	}

}

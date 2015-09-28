package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.JenkinsDslConstants;
import com.redhat.ose.jenkins.Publishers;

/**
 * Creates a Jenkins Job to validate an endpoint as part of an acceptance phase
 *
 */
class OseAcceptanceJob {
	
	String folder
	String jobName
	String acceptanceUrl
	String downstreamProject
	
	def create(jobParent) {
				
		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}"
		
		jobParent.job(jName) {
			
			parameters {
				stringParam "BUILD_PROJECT_VERSION",null,"Build version of the Project"
				stringParam "ACCEPTANCE_URL",acceptanceUrl, "URL of the Endpoint to Test"
			}

			
			description("OSE Acceptance Project - ${jobName}")
			
			
			// TODO: Externalize out Nexus Server and artifact information
			steps {
				shell """
				set +x

				MAX_ATTEMPTS=100
				DELAY=5
				COUNTER=0

				echo -n "Attempting to Verify Application "
				
				while [ \$COUNTER -lt \$MAX_ATTEMPTS ]
				do
				
					RESPONSE=\$(curl -s -o /dev/null -w '%{http_code}\\n' \$ACCEPTANCE_URL)
					
					if [ \$RESPONSE -eq 200 ]; then
						echo 
						echo 
						echo "Application Verified"
						exit 0
					fi
				
				    echo -n "."
					COUNTER=\$(( \$COUNTER + 1 ))
					
					sleep \$DELAY
					
				done
				
				echo 
				echo "Error: Application Could Not Be Verified After \$COUNTER Attempts"				
				""".stripIndent().trim()
				
			}

			publishers {
				
				// Manual trigger for next pipeline job	 
				buildPipelineTrigger(downstreamProject)

			}

		}
		this
	}


}

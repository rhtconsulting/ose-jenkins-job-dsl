package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.JenkinsDslConstants;
import com.redhat.ose.jenkins.Publishers;

/**
 * Creates a Jenkins Job to validate an endpoint as part of an acceptance phase. The absence of a configured
 * url will bypass the acceptance check.
 *
 */
class OseAcceptanceJob {
	
	String folder
	String jobName
	String appName
	String acceptanceUrl
	String downstreamProject
	String slackTokenCredential
	String slackUsername = "jenkins"
	String slackChannelName
	
	def create(jobParent) {
				
		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}"
		
		jobParent.job(jName) {
			
			parameters {
				stringParam "BUILD_PROJECT_VERSION",null,"Build version of the Project"
				stringParam "ACCEPTANCE_URL",acceptanceUrl, "URL of the Endpoint to Test"
			}

			
			description("OSE Acceptance Project - ${jobName}")
			
			
			steps {
				shell """
				set +x

				if [ ! -z \$ACCEPTANCE_URL ]; then
					MAX_ATTEMPTS=100
					DELAY=5
					COUNTER=0
	
					echo "Attempting to Verify Application "
					
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
				else
					echo "No acceptance url specified. Bypassing acceptance check"
				fi 
				""".stripIndent().trim()
				
			}
			
			if(slackTokenCredential && slackChannelName) {
				
				wrappers {
					credentialsBinding {
						string('SLACK_TOKEN', slackTokenCredential)
					}
	
	
				}
			}


			publishers {
				
				// Manual trigger for next pipeline job	 
				buildPipelineTrigger(downstreamProject) {
					parameters {
						predefinedProp('BUILD_PROJECT_VERSION', '$BUILD_PROJECT_VERSION')
					}
				}
				
				if(slackTokenCredential && slackChannelName) {
				
					String postBuildScript = """
                    set +x

					echo "Posting update to slack"

                    curl -s https://slack.com/api/chat.postMessage --data-urlencode token=\${SLACK_TOKEN} --data-urlencode channel="${slackChannelName}" --data-urlencode "username=${slackUsername}" --data-urlencode "icon_url=http://www.yolinux.com/TUTORIALS/images/Jenkins/Jenkins_logo.png" --data-urlencode "attachments=[{\\"fallback\\": \\"${appName} \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT - http://10.3.9.43/jenkins/\\",\\"pretext\\": \\"Jenkins ${appName} Notification\\",\\"title\\": \\"${appName} \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT\\",\\"title_link\\": \\"http://10.3.9.43/jenkins\\",\\"text\\": \\"Click the link above to view the job in Jenkins\\",\\"color\\": \\"#7CD197\\"}]" > /dev/null

                    """.stripIndent().trim()
					
					Publishers.postbuildScript(postBuildScript, delegate)
				
				}

			}

		}
		this
	}


}

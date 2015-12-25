package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.JenkinsDslConstants;
import com.redhat.ose.jenkins.Publishers;

/**
 * Deployment to the OpenShift Development environment
 * 
 * 1. Updates OpenShift BuildConfig SRC_APP_URL with location of application binary
 * 2. Triggers new build in OpenShift
 *
 */
class OseTriggerDevJob {
	
	String folder
	String jobName
	String gitOwner
	String gitProject
	String gitBranch = "master"
	String oseMaster
	String oseProject
	String oseApp
	String downstreamProject
	String oseTokenCredential
	String srcAppUrl
	Map mavenDeployRepo
	
	def create(jobParent) {

		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}"

		jobParent.job(jName) {
			
			parameters {
				stringParam "BUILD_PROJECT_VERSION",null,"Build version of the Project"
				stringParam "OSE_HOST",oseMaster,"OpenShift hostname"
				stringParam "OSE_PROJECT",oseProject, "Name of the OpenShift project"
				stringParam "OSE_APP",oseApp, "Name of the OpenShift application"
				
			}

			
			description("OSE Trigger Dev Project - ${jobName}")

			scm {
				github(gitOwner + '/' + (gitProject ? gitProject : jobName), gitBranch)
			}
			
			
			// TODO: Externalize out Nexus Server and artifact information
			steps {
				shell """
                  set +x 

                  echo "Triggering Dev Build of ${oseProject} \${BUILD_PROJECT_VERSION}"

                  sh \${WORKSPACE}/ose-update-src-app-url.sh -h=\${OSE_HOST} -t=\${OSE_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT} -a=\${OSE_APP} -s="${srcAppUrl}" 

                  echo "Building Image in OSE"

                  sh \${WORKSPACE}/trigger-monitor-build.sh -h=\${OSE_HOST} -t=\${OSE_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT} -a=\${OSE_APP}
				
				""".stripIndent().trim()
				
			}

			publishers {
				
				// Trigger downstream build
				downstreamParameterized {
					
					trigger("${downstreamProject}") {
							predefinedProp("BUILD_PROJECT_VERSION", "\${BUILD_PROJECT_VERSION}")
					}
				}
			}
			
			wrappers {
				credentialsBinding {
					string('OSE_SERVICE_ACCOUNT_TOKEN', oseTokenCredential)
				}

			}

			configure { node ->
				node / runPostStepsIfResult {
					name('SUCCESS')
					ordinal(0)
					color('BLUE')
					completeBuild(true)
				}
			}

		}
		this
	}


}

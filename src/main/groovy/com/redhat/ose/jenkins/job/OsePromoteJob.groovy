package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.Wrappers



/**
 * Promotion of a Docker image from an OpenShit environment/project to another
 *
 */
class OsePromoteJob {
	
	String folder
	String jobName
	String gitOwner
	String gitProject
	String gitBranch = "master"
	String oseRegistrySrc
	String oseRegistryUserSrc = "jenkins"
	String oseRegistryDest
	String oseRegistryUserDest = "jenkins"
	String oseProjectSrc
	String oseProjectDest
	String oseAppSrc
	String oseAppDest
	String oseSrcTokenCredential
	String oseDestTokenCredential
	String downstreamProject
	
	
	def create(jobParent) {
				
		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}" 

	    jobParent.job(jName) {
			
			parameters {
				stringParam "BUILD_PROJECT_VERSION",null,"Build version of the Project"
				stringParam "OSE_REGISTRY_SRC",oseRegistrySrc,"OpenShift Source Environment Host"
				stringParam "OSE_REGISTRY_USER_SRC",oseRegistryUserSrc,"OpenShift Source Username"
				stringParam "OSE_REGISTRY_DEST",oseRegistryDest,"OpenShift Destination Environment Host"
				stringParam "OSE_REGISTRY_USER_DEST",oseRegistryUserDest,"OpenShift Destination Username"
				stringParam "OSE_PROJECT_SRC",oseProjectSrc, "OpenShift Project for the Source Environment"
				stringParam "OSE_APP_SRC",oseAppSrc, "OpenShift App for the Source Environment"
				stringParam "OSE_PROJECT_DEST",oseProjectDest, "OpenShift Project for the Destination Environment"
				stringParam "OSE_APP_DEST",oseAppDest, "OpenShift App for the Destination Environment"
				
			}

			description("OSE Promote App - ${jobName}")

			scm {
				github(gitOwner + '/' + (gitProject ? gitProject : jobName), gitBranch)
			}
			
			steps {
				shell '''

                  set +x 

                  # Pull Image from Source Environment

                  sh \${WORKSPACE}/ose-docker-push-pull-operations.sh -o=pull -h=\${OSE_REGISTRY_SRC} -u=\${OSE_REGISTRY_USER_SRC} -t=\${OSE_SRC_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT_SRC} -a=\${OSE_APP_SRC} 

                  # Tag Image for Destination Environment

                 docker tag -f \${OSE_REGISTRY_SRC}/\${OSE_PROJECT_SRC}/\${OSE_APP_SRC} \${OSE_REGISTRY_DEST}/\${OSE_PROJECT_DEST}/\${OSE_APP_DEST}

                 # Push Image to Destination Environment 

                 sh \${WORKSPACE}/ose-docker-push-pull-operations.sh -o=push -h=\${OSE_REGISTRY_DEST} -u=\${OSE_REGISTRY_USER_DEST} -t=\${OSE_DEST_SERVICE_ACCOUNT_TOKEN} -n=\${OSE_PROJECT_DEST} -a=\${OSE_APP_DEST}

                 # Delete Images

                 docker rmi \${OSE_REGISTRY_SRC}/\${OSE_PROJECT_SRC}/\${OSE_APP_SRC} 

                 docker rmi \${OSE_REGISTRY_DEST}/\${OSE_PROJECT_DEST}/\${OSE_APP_DEST}
				
				'''.stripIndent().trim()
				
			}

			
			wrappers {
				credentialsBinding {
					string('OSE_SRC_SERVICE_ACCOUNT_TOKEN', oseSrcTokenCredential)
					string('OSE_DEST_SERVICE_ACCOUNT_TOKEN', oseDestTokenCredential)
				}
				
				// Workspace cleanup
				preBuildCleanup {
					deleteDirectories()
				} 

			}
			
			publishers {
				
				// Manual trigger for next pipeline job
				if(downstreamProject) {
					buildPipelineTrigger(downstreamProject)
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

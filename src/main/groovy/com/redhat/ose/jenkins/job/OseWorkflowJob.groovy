package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.JenkinsDslConstants;
import com.redhat.ose.jenkins.Publishers;

/**
 * Create a new CICD workflow
 *
 */
class OseWorkflowJob {
	
	String folder
	String jobName
	String appName
	String acceptanceUrl
	String slackTokenCredential
	String slackUsername = "jenkins"
	String slackChannelName	
	String appGitOwner
	String appGitProject
	String appGitBranch = "master"
	String scmPollSchedule
	String mavenDeployRepoUrl
	String mavenDeployServerId
	String mavenTargets = "build-helper:parse-version versions:set"
	String mavenName = "maven"
	String mavenGoals = "clean package"
	String rootPom = "pom.xml"
	
	
	String utilGitOwner
	String utilGitProject
	String utilGitBranch = "master"
	String devOseMaster
	String devOseProject
	String devOseApp
	String devOseTokenCredential
	String srcAppUrl
	
	String promoteUatOseUserSrc = "jenkins"
	String promoteUatOseUserDest = "jenkins"
	String promoteUatOseRegistrySrc
	String promoteUatOseRegistryDest
	String promoteUatOseProjectSrc
	String promoteUatOseProjectDest
	String promoteUatOseAppSrc
	String promoteUatOseAppDest
	String promoteUatOseSrcTokenCredential
	String promoteUatOseDestTokenCredential
	
	String promoteProdOseUserSrc = "jenkins"
	String promoteProdOseUserDest = "jenkins"
	String promoteProdOseRegistrySrc
	String promoteProdOseRegistryDest
	String promoteProdOseProjectSrc
	String promoteProdOseProjectDest
	String promoteProdOseAppSrc
	String promoteProdOseAppDest
	String promoteProdOseSrcTokenCredential
	String promoteProdOseDestTokenCredential

	
	def create(jobParent) {
						
		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}"
		
		jobParent.workflowJob(jName) {
			
		
			description("OSE Workflow - ${jobName}")
			
			// Tigger Workflow based on SCM polling
			if(scmPollSchedule) {
				
				triggers {
					scm scmPollSchedule
				}
				
			}
			
			definition {
				cps {
					script(createWorkflow())
				}
			}

		}
		this
	}
	
	def createWorkflow() {
		def workflow = 
		"""
		def BUILD_PROJECT_VERSION = null

		${createBuildStep()}
		${triggerDevBuild()}
		${acceptanceStep()}
		${promoteUat()}
		${promoteProd()}

		def getProjectVersion(fileName='pom.xml'){
			def file = readFile(fileName)
			def project = new XmlSlurper().parseText(file)
			return project.version.text()
		}
        """
				
		workflow
		
	}
	
	def createBuildStep() {
		def buildStep = 		
		"""
		stage 'build'
		node {
			echo "Building Project"

			${appBuildScm()}
        
		 	def mvnHome = tool '${mavenName}'
        
			env.PATH = \"\${mvnHome}/bin:\${env.PATH}\"

			// Update Maven Artifact Version
			sh "mvn -f ${rootPom} ${mavenTargets} -DnewVersion=\\\\\\\${parsedVersion.majorVersion}.\\\\\\\${parsedVersion.minorVersion}.\\\\\\\${parsedVersion.incrementalVersion}-\${env.BUILD_ID}"
    
			// Grab Maven Version
			BUILD_PROJECT_VERSION = getProjectVersion("${rootPom}")

			echo "Running Workflow Version: \${BUILD_PROJECT_VERSION}"
    
			// Build and Deploy Project Maven Repository
			sh "mvn -f ${rootPom} ${mavenGoals} ${deployProject()}"

		}	
		"""
		
		buildStep
	}
	
	def appBuildScm() {
		return buildScm(appGitProject, appGitOwner, appGitBranch)
	}
	
	def utilBuildScm() {
		return buildScm(utilGitProject, utilGitOwner, utilGitBranch)
	}
	
	def buildScm(project, owner, branch) {
		def scmProject = project ? project : jobName
		
		return "git url: \"https://github.com/${owner}/${project}.git\", branch: \"${branch}\""
	}
	
	def deployProject() {
		
		if(mavenDeployRepoUrl) {
			return "deploy:deploy -DaltDeploymentRepository=${mavenDeployServerId}::default::${mavenDeployRepoUrl}"
		}
		else {
			return ""
		}
	}

	def triggerDevBuild() {
		def triggerDevBuild = 
		"""
			stage 'trigger-dev'
			node {
				echo "Triggering Development Build"
				
				${utilBuildScm()}
     
				withCredentials([[\$class: 'StringBinding', credentialsId: "${devOseTokenCredential}", variable: 'devToken']]) {
     
				echo "Triggering Dev Build of ${devOseApp} \${BUILD_PROJECT_VERSION}"

				sh "./ose-update-src-app-url.sh -h=${devOseMaster} -t=\${env.devToken} -n=${devOseProject} -a=${devOseApp} -s=\\\"${srcAppUrl}\\\""

				echo "Building Image in OSE"

				sh "./trigger-monitor-build.sh -h=${devOseMaster} -t=\${env.devToken} -n=${devOseProject} -a=${devOseApp}"
			}
           }
		"""
	}
	
	def acceptanceStep() {
		
		if(acceptanceUrl) {
			def acceptanceStep =
			"""
			stage 'acceptance'
			node {
		
				echo "Running Acceptance Tests" 
	     
				sh \"\"\"
				
					set +x
		
					MAX_ATTEMPTS=100
					DELAY=5
					COUNTER=0
		
					echo -n "Attempting to Verify Application "
		
					while [ \\\$COUNTER -lt \\\$MAX_ATTEMPTS ]
					do
		
						RESPONSE=\\\$(curl -s -o /dev/null -w '%{http_code}\n' ${acceptanceUrl})
		
						if [ \\\$RESPONSE -eq 200 ]; then
							echo 
							echo 
							echo "Application Verified"
							exit 0
						fi
		
						echo -n \".\"
						COUNTER=\\\$(( \\\$COUNTER + 1 ))
		
						sleep \\\$DELAY

					done

					echo 
					echo "Error: Application Could Not Be Verified After \\\$COUNTER Attempts"
					exit 1
	     
					\"\"\"

					"""
					
					if(slackTokenCredential && slackChannelName) {
	
						acceptanceStep += """

					withCredentials([[\$class: 'StringBinding', credentialsId: "${slackTokenCredential}", variable: 'slackToken']]) {
	        
						sh "curl https://slack.com/api/chat.postMessage --data-urlencode token=\${env.slackToken} --data-urlencode channel=\\\'${slackChannelName}\\\' --data-urlencode \\\"username=${slackUsername}\\\" --data-urlencode \'icon_url=http://www.yolinux.com/TUTORIALS/images/Jenkins/Jenkins_logo.png\' --data-urlencode \'attachments=[{\\\"fallback\\\": \\\"${appName} \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT - http://10.3.9.43/jenkins/\\\",\\\"pretext\\\": \\\"Jenkins ${appName} Notification\\\",\\\"title\\\": \\\"${appName} \${BUILD_PROJECT_VERSION} Ready to Deploy to UAT\\\",\\\"title_link\\\": \\\"http://10.3.9.43/jenkins\\\",\\\"text\\\": \\\"Click the link above to view the job in Jenkins\\\",\\\"color\\\": \\\"#7CD197\\\"}]\'"
	       
					}

						"""
					}

					acceptanceStep += """
	
				}
	
				"""
			
			acceptanceStep 
		}
		else {
			return ""
		}
	}
	
	def promoteUat() {
		
		def promoteUat = 
		 """
		 	input message: 'Deploy to UAT?', ok: 'Deploy UAT'
			stage 'deploy-uat'
			node {
				echo "Deploying to UAT"

			"""
		promoteUat += promotionStep(promoteUatOseSrcTokenCredential, promoteUatOseDestTokenCredential,promoteUatOseRegistrySrc, promoteUatOseUserSrc, promoteUatOseProjectSrc, promoteUatOseAppSrc, promoteUatOseRegistryDest, promoteUatOseUserDest, promoteUatOseProjectDest, promoteUatOseAppDest)
	
		promoteUat +=
			"""
			}
			"""
		promoteUat
	}
	
	def promoteProd() {
		
		def promoteProd =
		 """
		 	input message: 'Deploy to Prod?', ok: 'Deploy Prod'
			stage 'deploy-prod'
			node {
		 		echo "Deploying to Prod"
			"""
		promoteProd += promotionStep(promoteProdOseSrcTokenCredential, promoteProdOseDestTokenCredential,promoteProdOseRegistrySrc, promoteProdOseUserSrc, promoteProdOseProjectSrc, promoteProdOseAppSrc, promoteProdOseRegistryDest, promoteProdOseUserDest, promoteProdOseProjectDest, promoteProdOseAppDest)
	
		promoteProd +=
			"""
			}
			"""
		promoteProd
	}
	
	def promotionStep(sourceToken,destToken,sourceRegistry,sourceUser,sourceNamespace,sourceApp,destRegistry,destUser,destNamespace,destApp) {
		def promotionStep = 
		"""
				withCredentials([[\$class: 'StringBinding', credentialsId: "${sourceToken}", variable: 'oseTokenSource']]) {
	        
					withCredentials([[\$class: 'StringBinding', credentialsId: "${destToken}", variable: 'oseTokenDest']]) {
	     
	          			sh \"\"\"
	     
						#set +x 
	            
						# Pull Image from Source Environment
	
						sh ./ose-docker-push-pull-operations.sh -o=pull -h=${sourceRegistry} -u=${sourceUser} -t=\${env.oseTokenSource} -n=${sourceNamespace} -a=${sourceApp}
	            
						# Tag Image for Destination Environment
	            
						docker tag -f ${sourceRegistry}/${sourceNamespace}/${sourceApp} ${destRegistry}/${destNamespace}/${destApp}
	            
						# Push Image to Destination Environment 
	            
						sh ./ose-docker-push-pull-operations.sh -o=push -h=${destRegistry} -u=${destUser} -t=\${env.oseTokenDest} -n=${destNamespace} -a=${destApp}
	            
						# Delete Images
	            
						docker rmi ${sourceRegistry}/${sourceNamespace}/${sourceApp}
						
						docker rmi ${destRegistry}/${destNamespace}/${destApp}
	     
	        			\"\"\"
	
					}
				}
		    """
		promotionStep;
	}

}

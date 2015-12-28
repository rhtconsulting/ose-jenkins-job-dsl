package com.redhat.ose.jenkins.job

import com.redhat.ose.jenkins.Publishers
import com.redhat.ose.jenkins.Wrappers



/**
 * Commit Phase 
 * 
 * 1. Pulls source from repository and branch
 * 2. Updates Maven POM version to support continous delivery versioning
 * 3. Performs Maven build
 * 4. Deploys artifacts to Maven repository
 * 5. Triggers deployment to development environment
 *
 */
class OseDevBuildJob {
	
	String folder
	String jobName
	String gitOwner
	String gitProject
	String gitBranch = "master"
	String oseMaster
	String oseProject
	String scmPollSchedule
	String oseApp
	String downstreamProject
	String mavenDeployRepoUrl
	String mavenDeployServerId
	String mavenTargets = "build-helper:parse-version versions:set"
	String mavenName = "maven"
	String mvnGoals = "clean package"
	String rootPom = "pom.xml"
	
	def create(jobParent) {
				
		def jName = folder != null ? "/${folder}/${jobName}" : "${jobName}"
		
		jobParent.mavenJob(jName) {
			
			
			description("OSE Development Build - ${jobName}")
			rootPOM(rootPom)
			goals(mvnGoals)

			scm {
				github(gitOwner + '/' + (gitProject ? gitProject : jobName), gitBranch)
			}
			
			if(scmPollSchedule) {
				
				triggers {
					scm scmPollSchedule
				}
				
			}
			
			preBuildSteps {
				maven {
					mavenInstallation(mavenName)
					goals(mavenTargets)
					rootPOM(rootPom)
					property("newVersion", "\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion}-\${BUILD_NUMBER}")
				}
			}

			publishers {
				
				// Deploy artifacts to repository
				if(mavenDeployRepoUrl) {
					Publishers.deployArtifacts(mavenDeployServerId, mavenDeployRepoUrl, delegate)
				}
				
				// Trigger downstream build
				downstreamParameterized {
					
					trigger("${downstreamProject}") {
						parameters {
							predefinedProp("BUILD_PROJECT_VERSION", "\${POM_VERSION}")
						}
					}
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

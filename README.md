ose-jenkins-job-dsl
==============

Library to support a Continuous Deployment pipeline for OpenShift using Jenkins

## Jobs

The following jobs are available

1. OseDevBuildJob - Pulls source code from a GitHub based repository, updates Maven project to support continuous deployment, builds and deploys artifact to Maven repository
2. OseTriggerDevJob - Updates the *SRC_APP_URL* environment variable of the BuildConfig within OpenShift and triggers a new build in OpenShift
3. OseAcceptanceJob - Performs an acceptance test within OpenShift to verify a Restful services endpoint is active
4. OsePromoteJob - Promotion of an image within the Integrated Docker registry from one environment/project to another

## Views

Visualizing of the chaining of jobs is facilitated using the [Build Pipeline Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin). The *OseDeploymentPipelineView* script will produce a new pipeline view

## Jenkins Seed Job

The Jenkins [Job DSL Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) makes use of a seed job to produce the scripted jobs. A job in Jenkins is designated as a seed job uses the plugin to reference a directory containing scripts. This library is used by this Maven seed job. The `workspace` Maven profile will prepare the directory structure that can be read by the DSL plugin. The*DslEnvironmentGenerator* script within this file executes the job and view template scripts based on the values of a json file either located  relative to the Jenkins build or read from a remote location to drive the creation of the pipeline. The following is a sample json representation:

```
[
	{
		"name": "ose-app",
		"gitOwner": "sabre1041",
		"gitProject": "ose-app",
		"gitOwner": "sabre1041",
		"gitProject": "ose3-samples",
		"oseDevTokenCredential": "ose-token-dev",
		"oseDevTokenCredential": "ose-token-uat",
		"oseProdTokenCredential": "ose-token-prod",
		"oseRegistryDev": "registry.dev.ose.example.com",
		"oseRegistryUat": "registry.uat.ose.example.com",
		"oseRegistryProd": "registry.prod.ose.example.com",
		"oseProjectDev": "jenkins-dsl",
		"oseProjectUat": "jenkins-dsl",
		"oseProjectProd": "jenkins-dsl",
		"oseAppDev": "dsl-app",
		"oseAppUat": "dsl-app",
		"oseAppProd": "dsl-app",
		"oseDevMaster": "master.ose.example.com",
		"srcAppurl": "http://cicd.ose.example.com/nexus/service/local/artifact/maven/redirect?r=public&g=com.redhat&a=app-build-timestamp&v=${BUILD_PROJECT_VERSION}&p=war"
	}
]
```

## Prerequisites

The following prerequisites must be completed prior to utilizing this library

### Jenkins Plugins

The following are a high level list of plugins that must be installed and configured in Jenkins

* [Build Pipeline Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin)
* [Job DSL Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin)
* [Workspace Cleanup Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Workspace+Cleanup+Plugin)
* [Plain Credentials Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Plain+Credentials+Plugin)

### OpenShift Configuration 

The following configurations within OpenShift must be completed

* BuildConfig - A *SRC_APP_URL* environment variable must be defined within the *BuildConfig* definition in the development environment
* Integrated Docker Registry - The integrated docker registry must be exposed to allow external entities, such as jenkins, to communicate
* Project/Registry access - An account with push/pull access to the Integrated Docker registry


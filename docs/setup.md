# Zero to OpenShift Continuous Delivery Hero

Implementing continuous delivery of applications on OpenShift and Jenkins using this library can be completed by using the following steps

## Jenkins Seed Job

The Jenkins [Job DSL Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) makes use of a seed job to produce the scripted jobs. A job in Jenkins is designated as a seed job uses the plugin to reference a directory containing scripts. This library is used by this Maven seed job. The `workspace` Maven profile will prepare the directory structure that can be read by the DSL plugin. The*DslEnvironmentGenerator* script within this project executes the job and view template scripts based on the values of a json file either located  relative to the Jenkins job or read from a location either in another location on the CI server or remove source to drive the creation of the pipeline. The following is a sample json representation:

```
[
        {
                "name": "ose-app",
                "gitOwner": "sabre1041",
                "gitProject": "ose-app",
                "gitBranch": "ose-webinar",
                "mavenRootPom": "demo/pom.xml",
                "utilGitOwner": "sabre1041",
                "utilGitProject": "ose3-samples",
                "oseDevTokenCredential": "ose-token-dev",
                "oseUatTokenCredential": "ose-token-uat",
                "oseProdTokenCredential": "ose-token-prod",
                "oseRegistryDev": "registry.apps.ose.example.com",
                "oseRegistryUat": "registry.apps.ose.example.com",
                "oseRegistryProd": "registry.apps.ose.example.com",
                "oseProjectDev": "ose-app-dev",
                "oseProjectUat": "ose-app-uat",
                "oseProjectProd": "ose-app-prod",
                "oseAppDev": "ose-app",
                "oseAppUat": "ose-app",
                "oseAppProd": "ose-app",
                "oseDevMaster": "master.ose.example.com",
                "acceptanceUrl": "http://ose-app-dev.apps.example.com/rest/forge/events",
                "srcAppUrl": "http://<ip-address>/nexus/service/local/artifact/maven/redirect?r=public&g=org.jboss.examples&a=ticket-monster&v=${BUILD_PROJECT_VERSION}&p=war",
                "slackTokenCredential": "slack-token",
                "slackChannelName": "#openshift-dev",
                "cicdType": "pipeline"
        }
]
```

An important distinction is the *cicdType* property which will determine whether the CICD process will utilize the Jenkins Build Pipeline Plugin or Workflow plugin. Use **pipeline** for the Build Pipeline Plugin or **workflow** for the Workflow plugin

### Configuring the Seed Job

A preconfigured seed job has been provided called *ose-dsl* to build the Delivery pipeline in the jenkins/jobs folder. Simply copy the contents of this folder to the jobs folder of the Jenkins server. You will need to restart the server for the new job to appear. 

Modify the DSL file included in the ose-dsl folder to match your environment. Please see the [DSL JSON Driver] section explaining the different configuration parameters

Finally, configure the location of the JSON driver file in the the ose-dsl seed job.

Locate the seed job and click **Configure**. Next, update the *FILE_NAME* string parameter with the location of the json configuration file. The location of this file can be located on the Jenkins server or in a remote location. 

Click **Save** to apply the changes. 

Click the **Build with Parameters** link on the left. Verify the location of the *FILE_NAME* parameter and click **Build**

The seed job will build out the pipeline

## DSL JSON Driver

The DSL project is driven by a JSON file containing the parameters necessary to build the delivery pipeline.

The following table details the parameters and their significance

| Field        | Description | Required  |
| ------------- |-------------|------|
| name | Name of the application | yes
| gitBranch | Git branch of the application (Defaults to master) | no
| gitProject | Git project | yes
| gitOwner | Owner of the application git repository | yes
| utilGitBranch | Git branch of the repository containing utility scripts | no
| utilGitProject | Git project containing utility scripts | yes
| utilGitOwner | Owner of the git repository containing utility scripts | yes
| scmPollSchedule | Cron schedule to poll application Git repository for changes | no
| mavenDeployRepoUrl | Url of the Maven repository to deploy packaged artifacts | no
| mavenDeployServerId | Id of the Maven server to deploy artifacts as configured in the settings.xml file | no
| oseDevTokenCredential | Name of the Jenkins credential containing the OpenShift authentication token for the Development environment | yes
| oseUatTokenCredential | Name of the Jenkins credential containing the OpenShift authentication token for the UAT environment | yes
| oseProdTokenCredential | Name of the Jenkins credential containing the OpenShift authentication token for the Production environment | yes
| oseRegistryDev | URL of the integrated OpenShift docker registry for the Development environment | yes
| oseRegistryUat | URL of the integrated OpenShift docker registry for the UAT environment | yes
| oseRegistryProd | URL of the integrated OpenShift docker registry for the Production environment | yes
| oseProjectDev | Name of the OpenShift project for the Development environment | yes
| oseProjectUat | Name of the OpenShift project for the UAT environment | yes
| oseProjectProd | Name of the OpenShift project for the Production environment | yes
| oseAppDev | Name of the OpenShift application for the Development environment | yes
| oseAppUat | Name of the OpenShift application for the UAT environment | yes
| oseAppProd | Name of the OpenShift application for the Production environment | yes
| oseDevMaster | URL of the OpenShift master API server | yes
| srcAppUrl | URL where the packaged application should be retrieved as part of the OpenShift S2I build  | yes
| mavenRootPom | Location of the Maven pom within the git repository (defaults to pom.xml) | no
| mavenGoals | Maven goals to execute during the build of the application  | no
| acceptanceUrl | URL to facilitate automated acceptance testing the application in the development environment | no
| slackTokenCredential | Name of the Jenkins credential containing the Slack authentication token | no
| slackChannelName | Name of the sack channel or private group to post (include # for channels) | no

The following is an example of a such file

```
[
	{
		"name": "ticket-monster-new",
		"gitOwner": "sabre1041",
		"gitProject": "ticket-monster-webinar",
		"gitBranch": "ose-webinar",
 		"mavenRootPom": "demo/pom.xml",
 		"utilGitOwner": "sabre1041",
		"utilGitProject": "ose3-samples",
 		"mavenDeployRepoUrl": "http://127.0.0.1:8081/nexus/content/repositories/releases",
 		"mavenDeployServerId": "nexus",
		"oseDevTokenCredential": "ose-token-dev",
		"oseUatTokenCredential": "ose-token-uat",
		"oseProdTokenCredential": "ose-token-prod",
		"oseRegistryDev": "registry.apps.ose.example.com.com",
		"oseRegistryUat": "registry.apps.ose.example.com.com",
		"oseRegistryProd": "registry.apps.ose.example.com.com",
		"oseProjectDev": "ticket-monster-dev",
		"oseProjectUat": "ticket-monster-uat",
		"oseProjectProd": "ticket-monster",
		"oseAppDev": "ticket-monster",
		"oseAppUat": "ticket-monster",
		"oseAppProd": "ticket-monster",
		"oseDevMaster": "master.ose.labs.redhat.com",
 		"acceptanceUrl": "http://ose-app-dev.apps.ose.example.com/rest/forge/events",
		"srcAppUrl": "http://cicd.ose.example.com/nexus/service/local/artifact/maven/redirect?r=public&g=org.jboss.examples&a=ticket-monster&v=${BUILD_PROJECT_VERSION}&p=war",
 		"slackTokenCredential": "slack-token",
 		"slackChannelName": "#openshift-dev"
	}
]
```

## Specifying Location of Packaged Application

Once a build has completed in Jenkins, a new S2I build in OpenShift will be triggered. As part of the S2I build, the packaged source will be retrieved and inserted into the resulting docker image. The location of the packaged source must be configured in the *srcAppUrl* field of the JSON driver file. Jenkins build environment variables can be used since this field is evaluated during the job execution.

## Triggering Application Builds

Application builds can be triggered either directly or through a polling mechanism to the source repository. 


### Manual Triggering 

A new delivery pipeline can be initiated by logging into Jenkins and selecting the view at the top of the main page (Suffixed by *-delivery-pipeline*) 

Select the green build icon to start a new delivery pipeline

### Polling for changes

Pipelines can be triggered when changes occur to the application source repository. A cron based schedule when polling should occur can be configured by specifying the *scmPollSchedule* in the JSON driver file


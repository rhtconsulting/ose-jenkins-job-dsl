# Prerequisites

The following are a list of prerequisites must be met with the OpenShift and CI environments 

## Configuring OpenShift

OpenShift must have the following configured

* A [secured](https://docs.openshift.com/enterprise/3.0/install_config/install/docker_registry.html#securing-the-registry) and [exposed](https://docs.openshift.com/enterprise/3.0/install_config/install/docker_registry.html#exposing-the-registry) integrated Docker registry
* A [service account](https://docs.openshift.com/enterprise/3.0/dev_guide/service_accounts.html) with access to push and pull images from the integrated Docker registry and to trigger new application builds
* Three environments must be created. There are many logical architectures that can be applied (All three in the same project, three separate project in the same OpenShift environment, separate projects in multiple OpenShift environments). 
	* Templates are available for configuring the building and deploying of applications
		* [Build Template](https://raw.githubusercontent.com/sabre1041/ose3-samples/master/ose3-cicd-eap-sti-basic-build.json)- Used in the development environment to build and deploy the application
		* [Deploy Template](https://raw.githubusercontent.com/sabre1041/ose3-samples/master/ose3-cicd-eap-sti-basic-deploy.json) - Used in all other environments to deploy the application


## CI Server

The following components must be configured in the CI environment

### JQ

The [JQ](https://stedolan.github.io/jq/manual/) JSON parsing library is used to manipuate the JSON responses from the OpenShift API

### Maven  

Several of the dependencies included in this project are not found in Maven Central and instead found in the [Jenkins Maven Repository Server](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+Maven+Repository+Server).

Add the following inside the *profiles* section of the Maven *settings.xml* file

```
<profile>
    <id>jenkins</id>
    <repositories>
        <repository>
            <id>jenkins</id>
            <url>http://repo.jenkins-ci.org/public/</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>jenkins</id>
            <name>Jenkins Repository</name>
            <url>http://repo.jenkins-ci.org/public/</url>
        </pluginRepository>
    </pluginRepositories>
</profile>
```

Next add the profile inside the *ActiveProfiles* section:

```
<activeProfile>jenkins</activeProfile>
```

Jenkins is configured to publish artifacts produced from the build to an artifact repository. By default, it uses values configured in *servers* section of the Maven *settings.xml* file for authentication. 

```
<servers>  
      <server>
            <id>nexus</id>
            <username>admin</username>
            <password>admin123</password>
      </server>
</servers>
 ```

### Docker

Docker is used to pull and push images from the integrated OpenShift registry. The following must be configured:

* The user used to run Jenkins must have access to connect to the docker socket. More information can be found [here](https://docs.docker.com/articles/basics/)
* Since the OpenShift registry is secured, the CA certificate used to secure the registry must be configured in a folder on the CI server called */etc/docker/certs.d/&lt;fqdn of registry&gt;*

### Jenkins

The following must be configured within Jenkins

#### Jenkins Plugins

The following are a list of plugins that must be installed and configured within Jenkins

* [Delivery Pipeline Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Delivery+Pipeline+Plugin)
* [Job DSL Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin)
* [Parameterized Trigger Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Parameterized+Trigger+Plugin)
* [Workspace Cleanup Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Workspace+Cleanup+Plugin)
* [Plain Credentials Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Plain+Credentials+Plugin)
* [Credentials Binding Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin)
* [Post-Build Script Plugin](http://wiki.jenkins-ci.org/display/JENKINS/PostBuildScript+Plugin)

#### Jenkins Credentials

Jenkins uses secured values to communicate with OpenShift. These secure values are stored within *Credentials* within Jenkins. 

The authentication token used to communicate with OpenShift in each environment should be configured used the following steps

Note: The same token could be used for all environments if configured 

Login to Jenkins and select **Credentials** on the left side of the page. Select **Global Credentials** and then the **Add Credentials** linkUnder Kind, select **Secret Text**. Keep the scope as Global (Jenkins, nodes, slaves, items, all child items, etc)Enter the token value in the Secret field and insert a description for the item in the description fieldIf desired, click Advanced and then insert an ID value. Otherwise, a GUID value will be generated automaticallyClick **Save** to apply the changes*Note:* The ID value configured previously will need to be added to the configuration JSON file
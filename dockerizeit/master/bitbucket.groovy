import java.lang.System
import hudson.model.*
import jenkins.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())


def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration")

// Note: Hook management only supported by Bitbucket Cloud (dec 2017, Bitbucket Branch Source Plugin 2.2.7).
// For bitbucket servers, ensure that manageHooks is disabled in properties to avoid confusion
// https://go.cloudbees.com/docs/cloudbees-documentation/cje-user-guide/index.html#bitbucket
properties.bitbucketEndpoints.each {
    if(it.value.enabled) {
        println "--> Create bitbucket server ${it.key}"

        if (it.value.get('type', 'server') == 'cloud') {
            try {
                def cloudEndpointClazz = Class.forName("com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketCloudEndpoint")
                def cloudEndpointConstructor = cloudEndpointClazz.getDeclaredConstructor(boolean.class, String.class)
                def cloudInstance = cloudEndpointConstructor.newInstance(it.value.get('manageHooks', false), it.value.get('credentialsId', ""))
                desc.updateEndpoint(cloudInstance)
                desc.save()
            } catch (ClassNotFoundException ex) {
                println "ERROR: Can not configure BitBucket no plugin installed"
                return
            }
        } else {
            try {
                def serverEndpointClazz = Class.forName("com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketServerEndpoint")
                def serverEndpointConstructor = serverEndpointClazz.getDeclaredConstructor(boolean.class, String.class)
                def serverInstance = serverEndpointConstructor.newInstance(it.value.get('name', ""),
                                                                           it.value.get('serverUrl', ""),
                                                                           it.value.get('manageHooks', false),
                                                                           it.value.get('credentialsId', ""))
                desc.updateEndpoint(serverInstance)
                desc.save()
            } catch (ClassNotFoundException ex) {
                println "ERROR: Can not configure BitBucket no plugin installed"
                return
            }
        }
    }
}

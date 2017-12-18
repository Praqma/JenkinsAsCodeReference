import java.lang.System
import hudson.model.*
import jenkins.model.*
import com.cloudbees.jenkins.plugins.bitbucket.endpoints.*

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
    println it

    AbstractBitbucketEndpoint endpoint

    if (it.value.get('type', 'server') == 'cloud') {
      endpoint = new BitbucketCloudEndpoint(it.value.get('manageHooks', false),
                                            it.value.get('credentialsId', ""))
    } else {
      endpoint = new BitbucketServerEndpoint(it.value.get('name', ""), 
                                             it.value.get('serverUrl', ""), 
                                             it.value.get('manageHooks', false),
                                             it.value.get('credentialsId', ""))
    }
     
    desc.updateEndpoint((AbstractBitbucketEndpoint)endpoint)
  }
}

desc.save()
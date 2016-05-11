import java.lang.System
import hudson.model.*
import jenkins.model.*
import java.net.InetAddress

// load helpers and read properties
def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

println "--> set number of executors on master to ${properties.global.numExecutorsOnMaster}"
Jenkins.instance.setNumExecutors(properties.global.numExecutorsOnMaster)

println "--> set quite period to ${properties.global.scmQuietPeriod}"
Jenkins.instance.setQuietPeriod(properties.global.scmQuietPeriod)

println "--> set checkout retry to ${properties.global.scmCheckoutRetryCount}"
Jenkins.instance.setScmCheckoutRetryCount(properties.global.scmCheckoutRetryCount)

// Change it to the DNS name if you have it
println "--> setting jenkins root url"
def ip = InetAddress.localHost.getHostAddress()
jlc = JenkinsLocationConfiguration.get()
jlc.setUrl("http://$ip:8080")
jlc.save()

println "--> Set Global GIT configuration name to ${properties.global.git.name} and email address to ${properties.global.git.email}"
def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")
desc.setGlobalConfigName(properties.global.git.name)
desc.setGlobalConfigEmail(properties.global.git.email)

println "--> Set system message "
def env = System.getenv()
if ( env.containsKey('master_image_version') ) {
  // master_image_version set as env variable by the build process
  // Set it as a global variable in Jenkins to increase visibility
  helpers.addGlobalEnvVariable(Jenkins, 'master_image_version', env['master_image_version'])
  systemMessage = "This Jenkins instance generated from code.\n " +
                  "Avoid any manual changes since they will be discarded with next deployment.\n " +
                  "Change source instead. Jenkins docker image version: ${env['master_image_version']}\n\n" +
                  "Update ${properties.global.variables.default_repo} to change configuration"
  println "Set system message to:\n ${systemMessage}"
  Jenkins.instance.setSystemMessage(systemMessage)
} else {
  prinln "Can't set system message - missing env variable master_image_version"
}

println "--> Set global env variables"
properties.global.variables.each { key, value ->
  helpers.addGlobalEnvVariable(Jenkins, key, value)
}

// Set Global Slack configuration
/* def slack = Jenkins.instance.getExtensionList(jenkins.plugins.slack.SlackNotifier.DescriptorImpl.class)[0]
def params = [
  slackTeamDomain: "<mydomain>",
  slackToken: "<mytoken>",
  slackRoom: "",
  slackBuildServerUrl: "",
  slackSendAs: ""/
]
def req = [getParameter: { name -> params[name] }] as org.kohsuke.stapler.StaplerRequest
slack.configure(req, null)
slack.save()*/

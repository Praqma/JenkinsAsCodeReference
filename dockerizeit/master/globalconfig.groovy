import java.util.Properties
import java.lang.System
import hudson.model.*
import jenkins.model.*
import java.net.InetAddress

// load helpers
def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
Properties properties = helpers.readProperties("$home_dir/jenkins.properties")

println "--> disabling master executors"
Jenkins.instance.setNumExecutors(0)

println "--> setting quite period to 3"
Jenkins.instance.setQuietPeriod(3)

println "--> setting checkout retry to 3"
Jenkins.instance.setScmCheckoutRetryCount(3)

// Change it to the DNS name if you have it
println "--> setting jenkins root url"
def ip = InetAddress.localHost.getHostAddress()
jlc = JenkinsLocationConfiguration.get()
jlc.setUrl("http://$ip:8080")
jlc.save()

println "--> Set Global GIT configuration name to ${properties.globalConfigname} and email address to ${properties.globalConfigEmail}"
def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")
desc.setGlobalConfigName(properties.globalConfigname)
desc.setGlobalConfigEmail(properties.globalConfigEmail)

println "--> Set SOURCE_REPO env variable"
helpers.addGlobalEnvVariable(Jenkins, 'default_repo', properties.gitRepo)

println "--> Set system message "
def env = System.getenv()
if ( env.containsKey('jenkins_image_version') ) {
  println "jenkins_image_version = ${env['jenkins_image_version']}"
  // jenkins_image_version set as env variable by the build process
  // Set it as a global variable in Jenkins to increase visibility
  helpers.addGlobalEnvVariable(Jenkins, 'jenkins_image_version', env['jenkins_image_version'])
  systemMessage = "This Jenkins instance generated from code.\n " +
                  "Avoid any manual changes since they will be discarded with next deployment.\n " +
                  "Change source instead. Jenkins docker image version: ${env['jenkins_image_version']}\n\n" +
                  "Update ${properties.gitRepo} to change configuration"
  println "Set system message to:\n ${systemMessage}"
  Jenkins.instance.setSystemMessage(systemMessage)
} else {
  prinln "Can't set system message - missing env variable jenkins_image_version"
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

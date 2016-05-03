import java.util.Properties
import java.lang.System
import hudson.model.*
import jenkins.model.*
import java.net.InetAddress
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry

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

println "--> Read properties from the file"
Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
propertiesFile.withInputStream {
    properties.load(it)
}

println "--> Set Global GIT configuration name to ${properties.globalConfigname} and email address to ${properties.globalConfigEmail}"
def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")
desc.setGlobalConfigName(properties.globalConfigname)
desc.setGlobalConfigEmail(properties.globalConfigEmail)

println "--> Set SOURCE_REPO env variable to ${properties.gitRepo} "
EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("SOURCE_REPO", properties.gitRepo);
Jenkins.instance.getGlobalNodeProperties().add(new EnvironmentVariablesNodeProperty(entry));
Jenkins.instance.save()

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

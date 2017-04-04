import java.lang.System
import jenkins.model.*
import hudson.security.*
import jenkins.plugins.slack.SlackNotifier.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

def getPasswordCredentials(String id) {
  def all = CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class)
  return all.findResult { it.id == id ? it : null }
}

properties.slack.each() { configName, slackConfig ->
	if(slackConfig.enabled) {
		println '--> Configure Slack Notifier plugin'
		def slack = Jenkins.instance.getDescriptorByType(jenkins.plugins.slack.SlackNotifier.DescriptorImpl)
		slack.teamDomain = slackConfig.slackTeamDomain
		slack.token = slackConfig.slackToken
		slack.room = slackConfig.slackRoom
		slack.save()
	}
}
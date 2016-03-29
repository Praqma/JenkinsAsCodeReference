/*************************************************************************************************************************************
/*    Created by alexsedova 2015/11/09 copied fron praqma job dsl tranning
/*    Jenkins initial global configuration 
/*    Here configure all global settings for Jenkins
******************************************************************************************************************************/


import hudson.model.*
import jenkins.model.*
import java.net.InetAddress
import hudson.slaves.*
import javaposse.jobdsl.plugin.*
// Plugins for SSH credentials
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*

println "--> disabling master executors"
Jenkins.instance.setNumExecutors(0)

println "--> setting quite period to 3"
Jenkins.instance.setQuietPeriod(3)

println "--> setting checkout retry to 3"
Jenkins.instance.setScmCheckoutRetryCount(3)

// Change it to the DNS name if you have it
//Fix for Trifork, set dns=kitana and port=8081
println "--> setting jenkins root url"
def ip = InetAddress.localHost.getHostAddress()
jlc = JenkinsLocationConfiguration.get()
jlc.setUrl("http://$HOSTIP:8080")
jlc.save()

// Update Global Credetials setting with new user wirh ~/.ssh master key 
// TODO: Add here jenkins user with x-permissions on gitlab and create ssh public key for the user
println "--> set SSH user private Key "
global_domain = Domain.global()
credentials_store =Jenkins.instance.getExtensionList(
            'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
            )[0].getStore()
credentials = new BasicSSHUserPrivateKey(
          CredentialsScope.GLOBAL,
          "jenkins",
          "jenkins",
          new BasicSSHUserPrivateKey.UsersPrivateKeySource(),
          "",
          ""
        )
credentials_store.addCredentials(global_domain, credentials)

//Set Global GIT configuration name and email address
def inst = Jenkins.getInstance()

def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")

desc.setGlobalConfigName("alexsedova")
desc.setGlobalConfigEmail("als@praqma.net")

// Set Global Slack configuration
//def slack = Jenkins.instance.getExtensionList(jenkins.plugins.slack.SlackNotifier.DescriptorImpl.class)[0]
//def params = [
//  slackTeamDomain: "<mydomain>",
//  slackToken: "<mytoken>",
//  slackRoom: "",
//  slackBuildServerUrl: "",
//  slackSendAs: ""/
//]
//def req = [getParameter: { name -> params[name] }] as org.kohsuke.stapler.StaplerRequest
//slack.configure(req, null)

//slack.save()

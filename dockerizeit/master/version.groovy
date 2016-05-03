import hudson.model.*
import jenkins.*
import jenkins.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

println "--> Setting info about Jenkins image version"

for (e in System.getenv()) {
  if ( e.key == 'jenkins_image_version' ) {
      println "jenkins_image_version variable found jenkins_image_version = ${e.value}"
      EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry(e.key, e.value);
      Jenkins.instance.getGlobalNodeProperties().add(new EnvironmentVariablesNodeProperty(entry));
      Jenkins.instance.save()
      systemMessage = "This Jenkins instance generated from code. " +
                      "Avoid any manual changes since they will be discarded with next deployment. " +
                      "Change source instead. Jenkins docker image version: ${e.value}"
      println "Set system message to:\n ${systemMessage}"
      Jenkins.instance.setSystemMessage(systemMessage)
  }
}



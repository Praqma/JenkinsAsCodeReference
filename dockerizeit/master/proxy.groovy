import hudson.model.*
import jenkins.*
import jenkins.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry

println "--> Setting up proxy"

def gradle_opts = ""
def proxy_vars = ['HTTP_PROXY', 'HTTPS_PROXY', 'NO_PROXY']
for (e in System.getenv()) {
  def key = e.key.toUpperCase()
  if ( ! proxy_vars.contains(key) ) {
    continue
  }
  if ( e.value == "" ) {
    println "${e.key} defined but contains empty string. Skip it"
    continue
  }
  // Add proxy variables to the Jenkins global config - they are already available as env variables set
  // by Docker build but we want to increase visibility for the users
  EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry(e.key, e.value)
  Jenkins.instance.getGlobalNodeProperties().add(new EnvironmentVariablesNodeProperty(entry))
  Jenkins.instance.save()
  println "Added global environment variable ${e.key}=${e.value}"
  // Prepare GRADLE_OPTS variable
  switch (e.key.toUpperCase()) {
    case 'NO_PROXY':
      println "No proxy configuration found ${e.key} = ${e.value}"
      gradle_opts += " -Dhttp.nonProxyHosts=${e.value}"
      break
    case 'HTTP_PROXY':
      println "http_proxy configuration found ${e.key} = ${e.value}"
      def url = e.value.toURL()
      gradle_opts += " -Dhttp.proxyHost=${url.host.toString()} -Dhttp.proxyPort=${url.port.toString()}"
      break
    case 'HTTPS_PROXY':
      println "https_proxy configuration found ${e.key} = ${e.value}"
      def url = e.value.toURL()
      gradle_opts += " -Dhttps.proxyHost=${url.host.toString()} -Dhttps.proxyPort=${url.port.toString()}"
      break
    default:
      break
  }
}

// Set GRADLE_OPTS variable to configure proxy for Gradle since it does not respect http_proxy
if ( gradle_opts != "" ) {
  println "Setting up GRADLE_OPTS=${gradle_opts}"
  EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("GRADLE_OPTS",gradle_opts);
  Jenkins.instance.getGlobalNodeProperties().add(new EnvironmentVariablesNodeProperty(entry));
  Jenkins.instance.save()
} else {
  println "GRADLE_OPTS is empty - no proxy settings to set. Do nothing"
}

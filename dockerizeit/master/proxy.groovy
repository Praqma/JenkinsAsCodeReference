import hudson.model.*
import jenkins.*
import jenkins.model.*

// load helpers
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("/var/jenkins_home/init.groovy.d/helpers.groovy"))

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
  helpers.addGlobalEnvVariable(Jenkins, e.key, e.value)
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
  helpers.addGlobalEnvVariable(Jenkins, 'GRADLE_OPTS', gradle_opts)
} else {
  println "GRADLE_OPTS is empty - no proxy settings to set. Do nothing"
}

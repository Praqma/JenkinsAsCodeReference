import hudson.model.*
import jenkins.*
import jenkins.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

println "--> Setting up proxy"

def gradle_opts = ""
def proxy_vars = ['HTTP_PROXY', 'HTTPS_PROXY', 'NO_PROXY']
for (e in System.getenv()) {
  def key = e.key.toUpperCase()
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

println "Setting up GRADLE_OPTS=${gradle_opts}"

EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("GRADLE_OPTS",gradle_opts);
Jenkins.instance.getGlobalNodeProperties().add(new EnvironmentVariablesNodeProperty(entry));
Jenkins.instance.save()

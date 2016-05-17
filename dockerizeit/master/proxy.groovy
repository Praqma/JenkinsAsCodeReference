import hudson.model.*
import jenkins.*
import jenkins.model.*

// load helpers
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("/var/jenkins_home/init.groovy.d/helpers.groovy"))

println "--> Setting up proxy"

def gradle_opts = ""
def proxy_vars = ['HTTP_PROXY', 'HTTPS_PROXY', 'NO_PROXY', 'JAVA_OPTS']
for (e in System.getenv()) {
  def key = e.key.toUpperCase()
  if ( ! proxy_vars.contains(key) ) {
    continue
  }
  // Add proxy variables to the Jenkins global config - they are already available as env variables set
  // by Docker build but we want to increase visibility for the users
  helpers.addGlobalEnvVariable(Jenkins, e.key, e.value)
}

import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import hudson.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.matrixbasedsecurity.enabled){
  println "--> Configure Matrix-Based security"

  def instance = Jenkins.getInstance()
  def anonymous = hudson.security.ACL.ANONYMOUS_USERNAME

  def strategy = new GlobalMatrixAuthorizationStrategy()
  strategy.add(Jenkins.READ, anonymous)
  strategy.add(Jenkins.ADMINISTER, "authenticated")
  // Add permissions for anonymous slaves to connect to the master 
  strategy.add(Computer.CONNECT, anonymous)
  strategy.add(Computer.CREATE, anonymous)
  instance.setAuthorizationStrategy(strategy)

  instance.save()
}
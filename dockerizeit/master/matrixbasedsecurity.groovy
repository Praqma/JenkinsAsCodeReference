import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import hudson.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("${home_dir}/security.properties").toURI().toURL())

if(properties.matrixbasedsecurity.enabled){
  println "--> Configure Matrix-Based security"
  def strategy = new GlobalMatrixAuthorizationStrategy()

  properties.matrixbasedsecurity.users.each() { key, value ->
    value.permissions.each() {
      println "--> Add permission ${value.userId} for user ${it}"
      strategy.add(it, value.userId)
    }
  }

  Jenkins.instance.setAuthorizationStrategy(strategy)
  Jenkins.instance.save()
}
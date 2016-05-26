import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import hudson.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("${home_dir}/security.properties").toURI().toURL())
// To set permissions use list of Permission only from hudson.model.* 
def userProps = new ConfigSlurper().parse(new File("${home_dir}/permissions.properties").toURI().toURL())

if(properties.matrixbasedsecurity.enabled){
	println "--> Configure Matrix-Based security"
	def strategy = new GlobalMatrixAuthorizationStrategy()

	userProps.each() { key, value ->
		value.permissions.each() { strategy.add(it, value.userId)}
	}
	
	Jenkins.instance.setAuthorizationStrategy(strategy)
	Jenkins.instance.save()
}
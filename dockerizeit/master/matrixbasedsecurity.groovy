import java.util.Properties
import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import hudson.model.*

println "--> Matrix-Based security: Read properties from the file"

def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
Properties properties = helpers.readProperties("$home_dir/jenkins.properties")

if(properties.isMatrixBasedSecurity.toBoolean()){
	println "--> Configure Matrix-Based security"

	def instance = Jenkins.getInstance()
	def anonymous = hudson.security.ACL.ANONYMOUS_USERNAME
	def hudsonRealm = new HudsonPrivateSecurityRealm(false)
	instance.setSecurityRealm(hudsonRealm)

	def strategy = new GlobalMatrixAuthorizationStrategy()
	strategy.add(Jenkins.READ, anonymous)
	strategy.add(Jenkins.ADMINISTER, "authenticated")
	instance.setAuthorizationStrategy(strategy)

	instance.save()
}
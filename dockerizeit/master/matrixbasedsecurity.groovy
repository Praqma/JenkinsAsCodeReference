import java.util.Properties
import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import hudson.model.*

println "--> Matrix-Based security: Read properties from the file"

Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
propertiesFile.withInputStream {
    properties.load(it)
}
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
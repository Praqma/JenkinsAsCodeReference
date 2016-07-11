import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.googlelogin.GoogleOAuth2SecurityRealm

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.googlelogin.enabled) {
    println '--> Configure Google Login'
    def secretFile = new File(properties.googlelogin.clientSecret)
    SecurityRealm googleLogin_realm = new GoogleOAuth2SecurityRealm(properties.googlelogin.clientId,
            secretFile.text.trim(),
            properties.googlelogin.domain)
    Jenkins.instance.setSecurityRealm(googleLogin_realm)
    Jenkins.instance.save()
}
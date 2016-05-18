import java.lang.System
import jenkins.model.*
import hudson.plugins.active_directory.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.ad.enabled) {
  println "--> Configure AD"
  ActiveDirectorySecurityRealm realm = new ActiveDirectorySecurityRealm(properties.ad.domain,
                                                                        properties.ad.site,
                                                                        properties.ad.bindName,
                                                                        properties.ad.bindPassword,
                                                                        properties.ad.server,
                                                                        GroupLookupStrategy.valueOf(properties.ad.groupLookupStrategy.toString().toUpperCase()))
  Jenkins.instance.setSecurityRealm(realm)
  Jenkins.instance.save()
}

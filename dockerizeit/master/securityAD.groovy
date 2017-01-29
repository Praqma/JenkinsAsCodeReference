import java.lang.System
import jenkins.model.*
import hudson.plugins.active_directory.*
import com.google.common.collect.Lists

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.ad.enabled) {
  println "--> Configure AD"
  ActiveDirectorySecurityRealm realm = new ActiveDirectorySecurityRealm(properties.ad.domain,
                                                                        Lists.newArrayList(new ActiveDirectoryDomain(properties.ad.domain, properties.ad.server)),
                                                                        properties.ad.site,
                                                                        properties.ad.bindName,
                                                                        properties.ad.bindPassword,
                                                                        properties.ad.server,
                                                                        GroupLookupStrategy.valueOf(properties.ad.groupLookupStrategy.toString().toUpperCase()),
									true,
									true,
									new CacheConfiguration(1000, 6000))
  Jenkins.instance.setSecurityRealm(realm)
  Jenkins.instance.save()
}

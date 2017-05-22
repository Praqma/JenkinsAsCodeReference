import java.lang.System
import jenkins.model.*
import com.google.common.collect.Lists

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.ad.enabled) {
  println "--> Configure AD"
  def realm = new hudson.plugins.active_directory.ActiveDirectorySecurityRealm(properties.ad.domain,
                                                                        Lists.newArrayList(new hudson.plugins.active_directory.ActiveDirectoryDomain(properties.ad.domain, properties.ad.server)),
                                                                        properties.ad.site,
                                                                        properties.ad.bindName,
                                                                        properties.ad.bindPassword,
                                                                        properties.ad.server,
                                                                        hudson.plugins.active_directory.GroupLookupStrategy.valueOf(properties.ad.groupLookupStrategy.toString().toUpperCase()),
									true,
									true,
									new hudson.plugins.active_directory.CacheConfiguration(1000, 6000))
  Jenkins.instance.setSecurityRealm(realm)
  Jenkins.instance.save()
}

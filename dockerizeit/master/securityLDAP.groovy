import java.util.Properties
import java.lang.System
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*

println "--> Read properties from the file"

Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
propertiesFile.withInputStream {
    properties.load(it)
}
println "--> Configure LDAP"

if(properties.isLDAP.toBoolean()) {
    SecurityRealm ldap_realm = new LDAPSecurityRealm(properties.ldapServer,
                                                     properties.ldapRootDN,
                                                     properties.ldapUserSearchBase,
                                                     properties.ldapUserSearch,
                                                     properties.ldapGroupSearchBase,
                                                     properties.ldapManagerDN,
                                                     properties.ldapManagerPassword,
                                                     properties.ldapInhibitInferRootDN.toBoolean())
    Jenkins.instance.setSecurityRealm(ldap_realm)
    Jenkins.instance.save()
}
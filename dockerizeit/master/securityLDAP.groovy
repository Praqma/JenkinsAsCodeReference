import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*

String server = 'ldap://1.2.3.4'
String rootDN = 'dc=foo,dc=com'
String userSearchBase = 'cn=users,cn=accounts'
String userSearch = ''
String groupSearchBase = ''
String managerDN = 'uid=serviceaccount,cn=users,cn=accounts,dc=foo,dc=com'
String managerPassword = 'password'
boolean inhibitInferRootDN = false

SecurityRealm ldap_realm = new LDAPSecurityRealm(server, rootDN, userSearchBase, userSearch, groupSearchBase, managerDN, managerPassword, inhibitInferRootDN)
Jenkins.instance.setSecurityRealm(ldap_realm)
Jenkins.instance.save()

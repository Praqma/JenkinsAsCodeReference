import java.lang.System
import jenkins.model.*
import hudson.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

// JobDSL security is enabled by default
if(properties.scriptSecurity.jobDsl == false) {
  // Read more here https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration#migrating-to-160
  println "--> Attention! Disabling script secutiry for JobDSL"
  Descriptor dslSecurity = Jenkins.instance.getDescriptor('javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration')
  dslSecurity.setUseScriptSecurity(properties.scriptSecurity.jobDsl)
  Jenkins.instance.save()
}

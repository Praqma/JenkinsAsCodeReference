import java.lang.System
import hudson.model.*
import java.util.Collections
import java.util.List

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

def gl = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get()
def configs = new ArrayList<org.jenkinsci.plugins.workflow.libs.LibraryConfiguration>()

properties.libraries.each() { key, value ->
	if(value.enabled) {
		println "--> Configure Pipeline shared groovy library: ${value.name}"
		def userRemoteList = new ArrayList<hudson.plugins.git.UserRemoteConfig>()
		userRemoteList.add(new hudson.plugins.git.UserRemoteConfig(value.scm_path, "", "", value.credentialsId))
		def branches = Collections.singletonList(new hudson.plugins.git.BranchSpec(value.branch))
		println(userRemoteList)
		def gitSCM = new hudson.plugins.git.GitSCM(userRemoteList,
		                           branches,
		                           false,
		                           null, null, null, null)
		def globalConfig = new org.jenkinsci.plugins.workflow.libs.LibraryConfiguration(value.name, new org.jenkinsci.plugins.workflow.libs.SCMRetriever(gitSCM))
		globalConfig.setDefaultVersion(value.version)
		globalConfig.setImplicit(value.implicitly)
		globalConfig.setAllowVersionOverride(value.allow_overridden)
		configs.add(globalConfig)
	}
}
gl.setLibraries(configs)
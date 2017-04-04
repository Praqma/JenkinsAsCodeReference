import java.lang.System
import hudson.model.*
import hudson.plugins.git.GitSCM
import org.jenkinsci.plugins.workflow.libs.*
import hudson.plugins.git.*
import java.util.Collections
import java.util.List

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

GlobalLibraries gl = GlobalLibraries.get()
List<LibraryConfiguration> configs = new ArrayList<LibraryConfiguration>()

properties.libraries.each() { key, value ->
	if(value.enabled) {
		println "--> Configure Pipeline shared groovy library: ${value.name}"
		List<UserRemoteConfig> userRemoteList = new ArrayList<UserRemoteConfig>()
		userRemoteList.add(new UserRemoteConfig(value.scm_path, "", "", value.credentialsId))
		List<BranchSpec> branches = Collections.singletonList(new BranchSpec(value.branch))
		println(userRemoteList)
		GitSCM gitSCM = new GitSCM(userRemoteList,
		                           branches,
		                           false,
		                           null, null, null, null)
		LibraryConfiguration globalConfig = new LibraryConfiguration(value.name, new SCMRetriever(gitSCM))
		globalConfig.setDefaultVersion(value.version)
		globalConfig.setImplicit(value.implicitly)
		globalConfig.setAllowVersionOverride(value.allow_overridden)
		configs.add(globalConfig)
	}
}
gl.setLibraries(configs)
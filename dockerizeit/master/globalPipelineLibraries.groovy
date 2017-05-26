import java.lang.System
import hudson.model.*
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

GlobalLibraries gl = GlobalLibraries.get()
List<LibraryConfiguration> configs = new ArrayList<LibraryConfiguration>()

properties.libraries.each() { key, value ->
	if(value.enabled) {
		println "--> Configure Pipeline shared groovy library: ${value.name}"
		GitSCMSource gitSCM = new GitSCMSource(value.name,
		                                       value.scm_path,
                                                       value.credentialsId,
                                                       null, null, false)
		LibraryConfiguration globalConfig = new LibraryConfiguration(value.name, new SCMSourceRetriever(gitSCM))
		globalConfig.setDefaultVersion(value.version)
		globalConfig.setImplicit(value.implicitly)
		globalConfig.setAllowVersionOverride(value.allow_overridden)
		configs.add(globalConfig)
	}
}
gl.setLibraries(configs)

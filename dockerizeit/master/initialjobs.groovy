import java.util.Properties
import java.lang.System
import hudson.model.*
import jenkins.model.*
import hudson.slaves.*
import javaposse.jobdsl.plugin.*
import hudson.plugins.git.*
import java.util.Collections
import java.util.List
import hudson.security.ACL
import hudson.triggers.TimerTrigger
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*

def jobName = 'jenkins_as_a_code-seedjob'
def instance = Jenkins.getInstance()

println "--> Remove seed job if already exists"
job = Jenkins.instance.getJob(jobName)
if (job) {
  job.delete()
}

println "--> Read properties from the file"
Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
propertiesFile.withInputStream {
  properties.load(it)
}
println "--> Create seed-jod. The job will initiate all jobs from alljobs.dsl"
def project = new FreeStyleProject(Jenkins.instance, jobName)
project.setAssignedLabel()

// You can mount your local jobdsl repo to /home/jenkins-dsl-gradle - in this case this script
// will use that one instead of pulling official repo from GitHub. Good for testing small changes!
println "--> Configute Git access"
def localGitPath = properties.localRepoPath
def localRepo = new File(localGitPath)
List<UserRemoteConfig> dslGitrepoList = new ArrayList<UserRemoteConfig>()
if ( !localRepo.exists() ) {
  // default_repo will be set in globalconfig.groovy to point out path to this repo
  dslGitrepoList.add(new UserRemoteConfig('$default_repo', "", "", properties.gitUserName))
} else {
  dslGitrepoList.add(new UserRemoteConfig("file://" + localGitPath + '/', "origin", "", null))
}

List<BranchSpec> dslGitBranches = Collections.singletonList(new BranchSpec(properties.gitBranch))
GitSCM dslGitSCM = new GitSCM(dslGitrepoList,
        dslGitBranches,
        false,
        null, null, null, null);
project.setScm(dslGitSCM)

println "--> Setup JobDSL build step"
def jobDslBuildStep = new ExecuteDslScripts(scriptLocation=new ExecuteDslScripts.ScriptLocation(value = "false",
                                                                      targets=properties.dslTargetDirectory,
                                                                      scriptText=""),
                                            ignoreExisting=false,
                                            removedJobAction=RemovedJobAction.DELETE,
                                            removedViewAction=RemovedViewAction.DELETE,
                                            lookupStrategy=LookupStrategy.JENKINS_ROOT,
                                            additionalClasspath=properties.dslAdditionalClasspath);

project.getBuildersList().add(jobDslBuildStep)
project.addTrigger(new TimerTrigger("@midnight"))
project.save()
Jenkins.instance.reload()

println "--> Trigger jobs generation"
job = Jenkins.instance.getJob(jobName)
hudson.model.Hudson.instance.queue.schedule(job)
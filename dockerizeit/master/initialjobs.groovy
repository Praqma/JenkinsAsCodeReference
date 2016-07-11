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

def instance = Jenkins.getInstance()
def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

properties.seedjobs.each {
  println "--> Remove ${it.value.name} seed job if already exists"
  def job = Jenkins.instance.getJob(it.value.name)
  if (job) { job.delete() }
  println "--> Create ${it.value.name} seed jod"
  def project = new FreeStyleProject(Jenkins.instance, it.value.name)
  project.setAssignedLabel(new labels.LabelAtom(properties.global.variables.utility_slave))
  List<UserRemoteConfig> dslGitrepoList = new ArrayList<UserRemoteConfig>()
  dslGitrepoList.add(new UserRemoteConfig(it.value.repo, "", "", it.value.credentials))
  List<BranchSpec> dslGitBranches = Collections.singletonList(new BranchSpec(it.value.branch))
  GitSCM dslGitSCM = new GitSCM(dslGitrepoList,
                                dslGitBranches,
                                false,
                                null, null, null, null);
  project.setScm(dslGitSCM)
  def jobDslBuildStep = new ExecuteDslScripts(scriptLocation=new ExecuteDslScripts.ScriptLocation(value = "false",
                                                                      targets=it.value.path,
                                                                      scriptText=""),
                                                                      ignoreExisting=false,
                                                                      removedJobAction=RemovedJobAction.DELETE,
                                                                      removedViewAction=RemovedViewAction.DELETE,
                                                                      lookupStrategy=LookupStrategy.JENKINS_ROOT,
                                                                      additionalClasspath=it.value.classpath);

  project.getBuildersList().add(jobDslBuildStep)
  project.addTrigger(new TimerTrigger("@midnight"))
  it.value.parameters.each { key, value ->
    helpers.addBuildParameter(project, key, value)
  }
  project.save()
}

Jenkins.instance.reload()

properties.seedjobs.each {
  println "--> Schedule ${it.value.name} seed jod"
  Jenkins.instance.getJob(it.value.name).scheduleBuild()
}
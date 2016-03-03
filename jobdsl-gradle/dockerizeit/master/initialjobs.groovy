/******************************************************************************************************************************/
/* Created by alexsedova 
/* Initial seed job 
/* Seed job initial all jobs from *.dsl scripts 
/*
/******************************************************************************************************************************/
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

def jobName = 'seed-job'
def instance = Jenkins.getInstance()

// Remove seed job if already exists
job = Jenkins.instance.getJob(jobName)
if (job) {
  job.delete()
}

// Create seed-jod. The job will initiate all jobs from alljobs.dsl
def project = new FreeStyleProject(Jenkins.instance, jobName)
project.setAssignedLabel()
//Get local git for checking modifications:
// TODO add many branches
List<BranchSpec> dslGitBranches = Collections.singletonList(new BranchSpec("*/master"))
List<UserRemoteConfig> dslGitrepoList = new ArrayList<UserRemoteConfig>()
def localRepoPath = '/home/jenkins-dsl-gradle'
def localRepo = new File(localRepoPath)
if ( !localRepo.exists() ) {
  dslGitrepoList.add(new UserRemoteConfig("git@gitlab.trifork.se:trifork/jenkins-dsl-gradle.git",
          "",
          "",
          'jenkins'))
}
else {
  dslGitrepoList.add(new UserRemoteConfig("file://" + localRepoPath + '/', "origin", "", null))
}

GitSCM dslGitSCM = new GitSCM(dslGitrepoList,
        dslGitBranches,
        false,
        null, null, null, null);
project.setScm(dslGitSCM)
// Just add url:
// def scm = new GitSCM(projectURL) 
// Add also branch and credentialsId. 
// Get script execute from checked out git repository:
def jobDslBuildStep = new ExecuteDslScripts(scriptLocation=new ExecuteDslScripts.ScriptLocation(value = "false", targets="jobs/*.gdsl", scriptText=""),
                                            ignoreExisting=false,
                                            removedJobAction=RemovedJobAction.DELETE,
                                            removedViewAction=RemovedViewAction.DELETE,
                                            lookupStrategy=LookupStrategy.JENKINS_ROOT,
                                            additionalClasspath='src/main/groovy');

project.getBuildersList().add(jobDslBuildStep)
project.addTrigger(new TimerTrigger("@midnight"))
project.save()
Jenkins.instance.reload()

// trigger jobs generation
job = Jenkins.instance.getJob(jobName)
hudson.model.Hudson.instance.queue.schedule(job)
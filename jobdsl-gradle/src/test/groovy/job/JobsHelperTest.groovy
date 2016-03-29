package job

/**
 * Created by asa on 18/03/16.
 */

import javaposse.jobdsl.dsl.Job
import job.JobsHelper
import spock.lang.*

import static job.JobsHelper.createJob
import static job.SupportTestHelper.getJobParent

class JobsHelperTest extends Specification {
    private Job getDefaultJob() {
        createJob(getJobParent(), "testjob")
    }

    def "Should create a job"() {
        when:
        def Job newJob = getDefaultJob()

        then:
        newJob.name == "testjob"
    }
    def "Should add shell commands to a existing job"() {
        given:
        def Job newJob = getDefaultJob()

        when:
        newJob = JobsHelper.addShellStep(newJob, "echo 'shell'")

        then:
        newJob.node.builders.toString().contains("echo 'shell'")

        when:
        newJob = JobsHelper.addShellStep(newJob, "echo 'new'")

        then:
        newJob.node.builders.toString().contains("echo 'shell'")
        newJob.node.builders.toString().contains("echo 'new'")
    }
    def "Should add path to the shell script"(){
        given:
        def Job newJob = getDefaultJob()

        when:
        newJob = JobsHelper.addShellStep(newJob, "resources/file.sh")

        then:
        newJob.node.builders.toString().contains("resources/file.sh")

       // when:
       // newJob = JobsHelper.addShellScriptStep(newJob, "/users/user/path/to/another/script.sh")

        //then:
        //newJob.node.builders.toString().contains("'/users/user/path/to/script.sh'")
        //newJob.node.builders.toString().contains("'/users/user/path/to/another/script.sh'")
    }
    def "should add default params to a job"(){
        given:
        def Job newJob = getDefaultJob()

        when:
        newJob = JobsHelper.addDescriptionParam(newJob)

        then:
        newJob.node.logRotator[0].numToKeep[0].value() == 20

    }
    def "Should add a git repository to a job"() {
        given:
        def Job newJob = getDefaultJob()

        when:
        newJob = JobsHelper.addScmBlock(newJob, "git@testproject.git", "master", "user")

        then:
        with(newJob.node.scm) {
            it.branches.'hudson.plugins.git.BranchSpec'.name.text() == "master"
            it.userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.url.text() == "git@testproject.git"
            it.userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.credentialsId.text() == "user"
        }
    }
    def "Should add GitLab trigger to a job"(){
        given:
        def Job newJob = getDefaultJob()

        when:
        newJob=JobsHelper.addGitLabTrigger(newJob)

        then:
        with(newJob.node.'triggers'[0].'com.dabsquared.gitlabjenkins.GitLabPushTrigger'[0]) {
            it.triggerOnPush[0].value() == true
            it.triggerOnMergeRequest[0].value() == true
            it.triggerOpenMergeRequestOnPush[0].value() == 'never'
            it.ciSkip[0].value() == false
            it.setBuildDescription[0].value() == true
            it.addNoteOnMergeRequest[0].value() == false
            it.addVoteOnMergeRequest[0].value() == false
            it.allowAllBranches[0].value() == false
            it.includeBranchesSpec[0].value().empty
            it.excludeBranchesSpec[0].value().empty
        }
    }
}
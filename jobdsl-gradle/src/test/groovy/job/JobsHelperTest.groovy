package job

import spock.lang.*

import static job.SupportTestHelper.getJobParent

class JobsHelperTest extends Specification {

    def "Should create a job"() {
        when:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        then:
        builder.job.name == "testjob"
    }

    def "Should add shell commands to a existing job"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addShellStep("echo 'shell'")

        then:
        builder.job.node.builders.toString().contains("echo 'shell'")

        when:
        builder.addShellStep("echo 'new'")

        then:
        builder.job.node.builders.toString().contains("echo 'shell'")
        builder.job.node.builders.toString().contains("echo 'new'")
    }

    def "Should add shell script from the filepath"(){
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addShellScript('file.sh')

        then:
        builder.job.node.builders.toString().contains('#!/usr/bin/env bash\n' +
                'echo "Hello from file!"')
    }
    def "should add default params to a job"(){
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addLogRotator()

        then:
        builder.job.node.logRotator[0].numToKeep[0].value() == 20

    }

    def "Should add a git repository to a job"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addScmBlock("git@testproject.git", "master", "user")

        then:
        with(builder.job.node.scm) {
            it.branches.'hudson.plugins.git.BranchSpec'.name.text() == "master"
            it.userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.url.text() == "git@testproject.git"
            it.userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.credentialsId.text() == "user"
        }
    }

    def "Should add GitLab trigger to a job"(){
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addGitLabTrigger()

        then:
        with(builder.job.node.'triggers'[0].'com.dabsquared.gitlabjenkins.GitLabPushTrigger'[0]) {
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

    def "Should set delivery pipeline configuration"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addDeliveryPipelineConfiguration("build stage", "step name")

        then:
        with(builder.job.node.'properties'[0].'se.diabol.jenkins.pipeline.PipelineProperty'[0]) {
            it.taskName[0].value() == "step name"
            it.stageName[0].value() == "build stage"
        }
    }

    def "Should set delivery pipeline trigger"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addDeliveryPipelineTrigger(["foo", "bar"])

        then:
        builder.job.node.'publishers'[0]
                .'au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger'[0]
                .downstreamProjectNames[0].value() == "foo, bar"
    }

    def "Should configure pretested integration"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addPretestedIntegration()

        then:
        def wraps = builder.job.node.'buildWrappers'[0].'org.jenkinsci.plugins.pretestedintegration.PretestedIntegrationBuildWrapper'[0]
        wraps.'scmBridge'[0].'branch'[0].value() == 'master'
        wraps.'scmBridge'[0].'repoName'[0].value() == 'origin'

    }
}
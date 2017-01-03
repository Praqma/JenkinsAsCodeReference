package job

import spock.lang.*

import static job.SupportTestHelper.getJobParent
import javaposse.jobdsl.dsl.jobs.*
class JobsHelperTest extends Specification {

    def "Should create a job"() {
        when:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        then:
        builder.job.name == "testjob"
        builder.job.getClass() == FreeStyleJob
    }

    def "Should create a pipeline job"() {
        when:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob", "pipeline")

        then:
        builder.job.name == "testjob"
        builder.job.getClass() == WorkflowJob
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

    def "Should add content of the file as a pipeline script"(){
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob", "pipeline")

        when:
        builder.addPipelineDefinitionFile('file.sh')

        then:
        builder.job.node.definition.script.toString().contains('#!/usr/bin/env bash\n' +
                'echo "Hello from file!"')
    }

    def "should add Log Rotator to a job"(){
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

    def "Should add SCM poll trigger"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addScmPollTrigger()

        then:
        def trigger = builder.job.node.'triggers'[0].'hudson.triggers.SCMTrigger'[0]
        trigger.'spec'[0].value() == '* * * * *'
    }

    def "Should add SCM poll trigger with value different from default"() {
        given:
        JobBuilder builder = new JobBuilder(getJobParent(), "testjob")

        when:
        builder.addScmPollTrigger("other value")

        then:
        def trigger = builder.job.node.'triggers'[0].'hudson.triggers.SCMTrigger'[0]
        trigger.'spec'[0].value() == 'other value'
    }

}
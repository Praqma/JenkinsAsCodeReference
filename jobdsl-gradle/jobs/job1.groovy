import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

Job job1 = new JobBuilder(this as DslFactory, "JenkinsCI")
    .addDescriptionParam()
    .addShellStep("echo 'Hello!'")
    .addScmBlock("git@team.git", "master", "user1")
    .addPretestedIntegration()
    .build()
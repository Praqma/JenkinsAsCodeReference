import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory

new JobBuilder(this as DslFactory, "jenkins_as_code-review", "pipeline")
    .addLogRotator()
    .addScmPollTrigger("")
    .addPipelineDefinitionFile("jobdsl-gradle/src/jobs/resources/pipelines/jenkinsreview.groovy")
    .build()

import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory

new JobBuilder(this as DslFactory, "jenkins_as_a_code-pipeline", "pipeline")
    .addLogRotator()
    .addScmPollTrigger("@midnight")
    .addPipelineDefinitionFile("jobdsl-gradle/src/jobs/resources/pipelines/jenkinsdeploy.groovy")
    .build()

import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

new JobBuilder(this as DslFactory, "jenkins_as_a_code-pipeline", "pipeline")
    .addLogRotator()
    .addScmPollTrigger("@midnight")
    .addPipelineDefinitionFile("jobdsl-gradle/jobs/pipelines/jenkins-deploy.groovy")
    .build()

    
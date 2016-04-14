import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

Job review = new JobBuilder(this as DslFactory, "jenkins-review")
    .addLogRotator()
    .addShellStep("cd jobdsl-gradle; gradlew buildXml")
    .addScmBlock("git@github.com:Praqma/JenkinsAsCodeReference.git", "*/ready/**", "jenkins")
    .addScmPollTrigger()
    .addPretestedIntegration()
    .build()
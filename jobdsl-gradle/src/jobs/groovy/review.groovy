import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

Job review = new JobBuilder(this as DslFactory, "jenkins_as_a_code-review")
    .addLogRotator()
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew buildXml")
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew test")
    .addShellStep("cd \$WORKSPACE/dockerizeit; docker-compose build")
    .addScmBlock('$SOURCE_REPO', "*/ready/**", "jenkins")
    .addScmPollTrigger()
    .addPretestedIntegration()
    .build()
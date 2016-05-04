import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

// This is a tmp hack. We should replace this job with pipeline
// since pipeline jobs have better support for reading credentials id
// from env variables
Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
if (propertiesFile.exists() && !propertiesFile.isDirectory()) { 
  propertiesFile.withInputStream {
    properties.load(it)
  }
} else {
  // Assume we run gradle test
     properties.setProperty 'jenkinsSSHUserId', 'jenkins'
}

println "default credentials ${properties.jenkinsSSHUserId}"

Job review = new JobBuilder(this as DslFactory, "jenkins_as_a_code-review")
    .addLogRotator()
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew buildXml")
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew test")
    .addShellStep("cd \$WORKSPACE/dockerizeit; docker-compose build")
    .addScmBlock('$SOURCE_REPO', "*/ready/**", properties.jenkinsSSHUserId)
    .addScmPollTrigger()
    .addPretestedIntegration()
    .build()

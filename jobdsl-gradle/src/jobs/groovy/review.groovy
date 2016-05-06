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

// One more tmp hack - assume empty http_proxy/https_proxy/no_proxy
// First reason is this bug in docker-compose https://github.com/docker/compose/issues/3281
// Basically it returns None instead of empty string if variable isn't set
// This breakes proxy settings
// To work it out we set global variables in Jenkins to propagate proxy values even if they are empty
// but if use shell build step then Jenkins doesn't set empty env variables - seems to be another bug
// Pipeline works just fine though. So this should go away as soon as we will be able to migrate this
// job to pipeline or fix dumn docker-compose
build_script = '''
# Assume no proxy if it is not set
export http_proxy=${http_proxy:-}
export https_proxy=${https_proxy:-}
export no_proxy=${no_proxy:-}
env
cd $WORKSPACE/dockerizeit
docker-compose build
'''

println "default credentials ${properties.jenkinsSSHUserId}"

Job review = new JobBuilder(this as DslFactory, "jenkins_as_a_code-review")
    .addLogRotator()
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew buildXml")
    .addShellStep("cd \$WORKSPACE/jobdsl-gradle; ./gradlew test")
    .addShellStep("$build_script")
    .addScmBlock('$SOURCE_REPO', "*/ready/**", properties.jenkinsSSHUserId)
    .addScmPollTrigger()
    .addPretestedIntegration()
    .build()

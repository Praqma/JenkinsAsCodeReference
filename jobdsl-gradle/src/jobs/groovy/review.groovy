import job.JobBuilder
import job.Helpers

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

// This is a tmp hack. We should replace this job with pipeline
// since pipeline jobs have better support for reading credentials id
// and slave to run on from env variables
def default_credentials = Helpers.readEnvVariable("default_credentials", "jenkins")
def utilitySlave = Helpers.readEnvVariable("utility_slave", "utility-slave")

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

Job review = new JobBuilder(this as DslFactory, "jenkins_as_a_code-review")
    .addLogRotator()
    .addLabel(utilitySlave)
    .addGradleStep(["buildXMl"], "jobdsl-gradle")
    .addGradleStep(["test"], "jobdsl-gradle")
    .addShellStep("$build_script")
    .addShellStep("cd \$WORKSPACE/dockerizeit; ./generate-compose.py --debug --file docker-compose.yml --jmaster-image test-image --jmaster-version test-version --jslave-image test-image --jslave-version test-version && cat docker-compose.yml && git checkout HEAD docker-compose.yml")
    .addShellStep("cd \$WORKSPACE/dockerizeit/munchausen; cp ../docker-compose.yml .; docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t munchausen .")
    .addScmBlock('$default_repo', "*/ready/**", default_credentials)
    .addScmPollTrigger()
    .addPretestedIntegration('$default_branch')
    .build()

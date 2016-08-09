import job.JobBuilder

import javaposse.jobdsl.dsl.DslFactory
/**
 * Watch the JenkinsIssues!
 * Found the issue [JENKINS-33756](https://issues.jenkins-ci.org/browse/JENKINS-33756)
 * Job is run twice on first slave using label parameter. If the box "Run on all nodes matching the label"
 * is checked, the job will be triggered twice in the node it has been started. For example,
 * If we have 4 slaves are matching one label, we will have 5 running jobs instead of 4.
 * These issue is not resolved yet.
 * We don't use cluster of docker hosts, that is why we are running job on
 * all slaves matching "docker" label even they are running on the one host. We could run
 * clean up job just once, while we don't have a multihost cluster - it is another
 * possible solution. In this case we will not get the issue with double job running.
 **/
new JobBuilder(this as DslFactory, "jenkins_as_a_code-cleaup-docker")
    .addLogRotator()
    .addCronBuildTrigger('H 2-3 * * *')
    .addNodeLabelBuildParameter("Docker hosts", ["docker"])
    .addShellStep("docker-clean images")
    .build()
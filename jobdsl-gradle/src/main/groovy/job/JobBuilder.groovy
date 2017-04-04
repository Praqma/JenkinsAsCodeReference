package job

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.*
import javaposse.jobdsl.dsl.Job

public class JobBuilder {

    private DslFactory dslFactory
    /* default */ def job

    /**
     * Create job
     *
     * @String name   job name
     * @String type   job type (freestyle, pipeline)
     */
    public JobBuilder(DslFactory dslFactory, String name, String type = 'freestyle') {
        this.dslFactory = dslFactory;
        if ( type == 'freestyle' ) {
            this.job = this.dslFactory.job(name)
        } else if ( type == 'pipeline' ) {
            this.job = this.dslFactory.pipelineJob(name)
        } else {
            throw new Exception('Not supported job type')
        }
    }

    /**
     * Use this function to add shell step to the job configuration
     *
     * @String command  Add shell command
     * @boolean debug   debug mode
     */
    public JobBuilder addShellStep(String command, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.steps() {
            shell(command)
        }
        this
    }

    /**
     * Use this function to add shell step to the job configuration
     *
     * @String command  Add shell command
     * @boolean debug   debug mode
     */
    public JobBuilder addShellScript(String filepath, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.steps() {
            shell(dslFactory.readFileFromWorkspace(filepath))
        }
        this
    }


    /**
     * Use this function to add Gradle build step to the job configuration
     *
     * @String targets   Gradle task to execute
     * @String dir       Directory with Gradle files
     * @String otpions   Switches to use
     */
    public JobBuilder addGradleStep(List<String> targets, String dir, List<String> options = [], boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        } else {
            job.steps() {
                gradle {
                    targets.each() { tasks(it) }
                    options.each() { switches(it) }
                    rootBuildScriptDir(dir)
                }
            }
        }
        this
    }

    /**
     * Set an agent selector
     *
     * @String  nodeLabel Nodes to pick
     */
    public JobBuilder addLabel(String nodeLabel) {
        job.label(nodeLabel)
        this
    }

    /**
     * Use this function to add	pipeline script	definition as a file
     *
     * @String  script	Script to add
     * @boolean debug   debug mode
     */
    public JobBuilder addPipelineDefinitionFile(String filePath, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        } else {
            job.definition {
                cps { script(dslFactory.readFileFromWorkspace(filePath))
                      sandbox() }
            }
        }
        this
    }

    /**
     * Use this function to add more default parameters to the job configuration
     *
     * @boolean debug   debug mode
     */
    public JobBuilder addLogRotator(int num = 20, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.logRotator {
            numToKeep(num)
        }
        this
    }

    /**
     * Use this function to configure SCM block to job configuration
     *
     * @String repo         URL to Gitlab project
     * @String credential   Credentials for gitlab access
     * @String repoBranch   Specify branche
     * @boolean debug       debug mode
     */
    public JobBuilder addScmBlock(String repo, String repoBranch, String credential, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.scm {
            git {
                remote {
                    name('origin')
                    url(repo)
                    credentials(credential)
                }
                branch(repoBranch)
                extensions {
                    pruneBranches()
                }
            }
        }
        this
    }

    /**
     * Use this function to add SCM poll trigger
     *
     * @String schedule     Specify how often to poll - cron string
     * @boolean debug       debug mode
     */
    public JobBuilder addScmPollTrigger(String schedule = '* * * * *', boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        } else {
            job.triggers {
                scm(schedule)
            }
        }
        this
    }
    /**
     * Use this function to add Build cron trigger
     *
     * @String           A string you set the time range in cron format
     */
        public JobBuilder addCronBuildTrigger(String time, boolean debug = false) {
            if (debug) {
                // TODO: set something for the debug mode
                println 'Debug is set to true'
            } else {
                job.triggers {
                    cron(time)
                }
            }
            this
        }
    /**
     * Use this function to add GitLab trigger to the job configuration
     *
     * @param job Job instance
     * @boolean debug   debug mode
     */
    public JobBuilder addGitLabTrigger(boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.triggers {
            gitlabPush {
                buildOnMergeRequestEvents(true)
                buildOnPushEvents(true)
                enableCiSkip(false)
                setBuildDescription(true)
                addNoteOnMergeRequest(false)
                rebuildOpenMergeRequest('never')
                addVoteOnMergeRequest(false)
                useCiFeatures(false)
                acceptMergeRequestOnSuccess()
                allowAllBranches(false)
                includeBranches('')
                excludeBranches('')
            }
        }
        this
    }

    /**
     * Use this function to configure Slack block to the job configuration
     *
     * @String domain     Slack domain name
     * @String channel    Slack channel
     * @boolean debug      debug mode
     */
    public JobBuilder addSlackNotification(String domain, String channel, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.publishers {
            slackNotifications {
                teamDomain domain
                projectChannel channel
                notifyFailure true
                notifySuccess true
                notifyUnstable true
                notifyBackToNormal true
            }
        }
        this
    }

    /**
     * Use this function to configure Pretested Integration plugin
     *
     * @boolean debug       debug mode
     */
    public JobBuilder addPretestedIntegration(String branchName = "master", boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.configure { project ->
            project / publishers << 'org.jenkinsci.plugins.pretestedintegration.PretestedIntegrationPostCheckout' {}
            project / buildWrappers << 'org.jenkinsci.plugins.pretestedintegration.PretestedIntegrationBuildWrapper' {
                scmBridge('class': 'org.jenkinsci.plugins.pretestedintegration.scm.git.GitBridge') {
                    branch branchName
                    integrationStrategy('class': 'org.jenkinsci.plugins.pretestedintegration.scm.git.SquashCommitStrategy')
                    repoName 'origin'
                 }
                 rollbackEnabled false
            }
        }
        this
    }

    /**
     * Use this function to add delivery pipeline configuration
     *
     * @String buildStage
     * @String stepName
     * @boolean debug       debug mode
     */
    public JobBuilder addDeliveryPipelineConfiguration(String buildStage, String stepName, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.deliveryPipelineConfiguration(buildStage, stepName)
        this
    }

    /**
     * Use this function to add delivery pipeline trigger
     *
     * @ArrayList jobs
     * @boolean debug  debug mode
     */
    public JobBuilder addDeliveryPipelineTrigger(ArrayList<String> jobs, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.publishers {
            buildPipelineTrigger(jobs.join(", "))
        }
        this
    }
    /**
     * Use this method to parameterize your build using NodeLabel parameter plugin,
     * see more details https://jenkinsci.github.io/job-dsl-plugin/#path/job-parameters-nodeParam
     * and https://wiki.jenkins-ci.org/display/JENKINS/NodeLabel+Parameter+Plugin
     * There is a reference to issue [JENKINS-33756](https://issues.jenkins-ci.org/browse/JENKINS-33756)
     * "Run on all nodes matching the label" or allNodes in DSL flag schedules one more build
     * on the node job has been started.
     *
     * @String paramName                  the paramater name
     * @ArrayList<String> nodesLabels     list of labels of nodes you want to run the job
     *
     */
    public JobBuilder addNodeLabelBuildParameter(String paramName, ArrayList<String> nodesLabels, boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.parameters {
            labelParam (paramName) {
                defaultValue(nodesLabels.join(", "))
                allNodes('allCases', 'IgnoreOfflineNodeEligibility')
            }
        }
        this
    }

    /**
     *
     * @return ?
     */
    public Job build() {
        job
    }
}

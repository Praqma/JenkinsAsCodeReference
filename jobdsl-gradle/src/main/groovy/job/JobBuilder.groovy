package job

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

public class JobBuilder {

    private DslFactory dslFactory
    /* default */ Job job

    /**
     * Use this function to add GitLab trigger to the job configuration
     *
     * @boolean debug   debug mode
     */
    public JobBuilder(DslFactory dslFactory, String name) {
        this.dslFactory = dslFactory;
        this.job = this.dslFactory.job(name)
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
     * Use this function to add more default parameters to the job configuration
     *
     * @boolean debug   debug mode
     */
    public JobBuilder addDescriptionParam(boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.logRotator {
            numToKeep(20)
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
    public JobBuilder addPretestedIntegration(boolean debug = false) {
        if (debug) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.configure { project ->
            project / publishers << 'org.jenkinsci.plugins.pretestedintegration.PretestedIntegrationPostCheckout' {}
            project / buildWrappers << 'org.jenkinsci.plugins.pretestedintegration.PretestedIntegrationBuildWrapper' {
                scmBridge('class': 'org.jenkinsci.plugins.pretestedintegration.scm.git.GitBridge') {
                    branch 'master'
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
     *
     * @return ?
     */
    public Job build() {
        job
    }
}

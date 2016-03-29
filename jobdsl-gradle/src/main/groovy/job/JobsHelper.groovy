package job

/** **************************************************************************************************************************/
/*  Created by alexsedova 2016/03/21
/*
/*  JobsHelper class contains triggers/configurations and so on call repeatedly by jobs
/***************************************************************************************************************************/

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class JobsHelper {

    /*
     * Use this function to add GitLab trigger to the job configuration
     *
     * @param job     Job instance
     * @param debug   debug mode
     */
    public static Job createJob(DslFactory dslFactory, String name, boolean debug=false) {
        if (debug == true) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        def Job job = dslFactory.job(name)
        job
    }
    /*
       * Use this function to add shell step to the job configuration
       *
       * @param job     Job instance
       * @param command Add shell command
       * @param debug   debug mode
       */
    public static Job addShellStep(Job job, String command, boolean debug=false) {
        if (debug == true) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.steps() {
            shell(command)
        }
        job
    }
    /*
       * Use this function to add shell step to the job configuration
       *
       * @param job     Job instance
       * @param path    Path to the shell script file
       * @param debug   debug mode
       */
   /* public static Job addShellScriptStep(Job job, String path, boolean debug=false) {
        if (debug == true) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.steps() {
            shell(readFileFromWorkspace(path))
        }
        job
    }*/
    /*
       * Use this function to add more default parameters to the job configuration
       *
       * @param job     Job instance
       * @param debug   debug mode
       */
    public static Job addDescriptionParam(Job job, boolean debug=false) {
        if (debug == true) {
            // TODO: set something for the debug mode
            println 'Debug is set to true'
        }
        job.logRotator {
            numToKeep(20)
        }
        job
    }
    /*
    * Use this function to configure SCM block to job configuration
    *
    * @param job          Job instance
    * @param repo         URL to Gitlab project
    * @param credential   Credentials for gitlab access
    * @param repoBranch   Specify branche
    * @param isRecursive  add recursive
    * @param debug        debug mode
    */
    public static Job addScmBlock(Job job, String repo, String repoBranch, String credential, boolean debug=false) {
        if (debug == true) {
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
        job
    }
    /*
    * Use this function to add GitLab trigger to the job configuration
    *
    * @param job     Job instance
    * @param debug   debug mode
    */
    public static Job addGitLabTrigger(Job job, boolean debug=false) {
        if (debug == true) {
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
        job
    }
    /*
   * Use this function to configure Slack block to the job configuration
   *
   * @param job        Job instance
   * @param domain     Slack domain name
   * @param channel    Slack channel
   * @param debug      debug mode
   */

    static void addSlackNotification(Job job, String domain, String channel, boolean debug=false) {
        if (debug == true) {
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
    }
    /*
    * Use this function to configure Pretested Integration plugin
    *
    * @param context     Job instance
    * @param debug       debug mode
    */

    public static void addPretestedIntegration(Job job, boolean debug=false) {
        if (debug == true) {
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
    }
}

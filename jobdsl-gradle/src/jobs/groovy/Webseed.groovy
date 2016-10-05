def githubUrl = 'https://github.com/Praqma/job-dsl-collection'
def branchName = "master" //"\${BRANCH}"
def releasePraqmaCredentials = '100247a2-70f4-4a4e-a9f6-266d139da9db'
def dockerHostLabel = 'GiJeLiSlave'
//def project_name = '2git'
//def repo_name = 'Praqma/2git'
def cred_id = "100247a2-70f4-4a4e-a9f6-266d139da9db"
def jobName = "Web_Seed"
def displayName = "Web_Pipeline"
def descriptionString = "Web pipeline job"


  job(jobName) {

  displayName(displayName)

	label(dockerHostLabel)

  description(descriptionString)

    triggers {
      githubPush()
    }

    logRotator {
          daysToKeep(90)
    }

    scm {
      git {

        remote {
          url(githubUrl)
          credentials(releasePraqmaCredentials)
        }

        branch(branchName)

        configure {
          node ->
          node / 'extensions' << 'hudson.plugins.git.extensions.impl.CleanBeforeCheckout' {}
        }
      }
    }

    steps {

             dsl {
                 external "web-pipeline-dsl/web_pipeline_dsl.groovy"
                 removeAction("DELETE")
                 removeViewAction("DELETE")
             }
 }


    }







//#########################################################################################################

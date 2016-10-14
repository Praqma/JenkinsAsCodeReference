  def githubUrl = 'https://github.com/Praqma/job-dsl-collection'
  def branchName = "master" //"\${BRANCH}"
  def releasePraqmaCredentials = '100247a2-70f4-4a4e-a9f6-266d139da9db'
  def dockerHostLabel = 'GiJeLiSlave'
  def cred_id = 'jenkins'
  def jobName = 'Web_Seed'
  def descriptionString = 'Web pipeline job'
  def externalScriptPath = 'web-pipeline-dsl/web_pipeline_dsl.groovy'


  job(jobName) {

  	label(dockerHostLabel)

    description(descriptionString)

    triggers {
         githubPush()
    }

    logRotator {
         daysToKeep(90)
    }

    environmentVariables {
        keepBuildVariables(true)
        keepSystemVariables(true)
    }

   jdk('(System)')

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
           external "$externalScriptPath"
           removeAction("DELETE")
           removeViewAction("DELETE")
           }
   }

}

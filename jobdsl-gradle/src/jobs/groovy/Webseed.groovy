  def githubUrl = 'https://github.com/Praqma/job-dsl-collection'
  def branchName = "master" //"\${BRANCH}"
  def releasePraqmaCredentials = 'jenkins'
  def dockerHostLabel = 'GiJeLiSlave'
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

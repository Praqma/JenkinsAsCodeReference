import job.Helpers

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

def defaultCredentials = Helpers.readEnvVariable("default_credentials", "")
def defaultBranch = Helpers.readEnvVariable("default_branch", "")
def defaultRepo = Helpers.readEnvVariable("default_repo", "")

DslFactory dslFactory = this
dslFactory.multibranchPipelineJob( "jenkins_as_a_code-review-pipeline" ) {

    branchSources {
        github {
            buildOriginBranchWithPR ( false )
            buildOriginPRMerge ( true )
            checkoutCredentialsId ( defaultCredentials )
            // Exclude default branch from review builds - that one will be built by jenkins_as_a_code-pipeline instead.
            if ( defaultBranch != "" ) {
                excludes ( defaultBranch )
            }
            // Parse out repository and organisation from default_repo
            repoTokens = defaultRepo.split(':')[-1].split('/')
            if (repoTokens.length > 2) {
                repoName = '/'.join(repoTokens[1..-1])
                repoOwner( ${repoTokens[0]} )
            } else if (repoTokens.length == 2 ) {
                repoName = repoTokens[1]
                repoOwner( ${repoTokens[0]} )
            } else {
                repoName = repoTokens[0]
            }
            repository ( repoName )

            scanCredentialsId ( "" )
        }
    }

    triggers {
        // Scan MB-pipeline every 4hours.
        periodic ( 240 )
    }

    // remove dead branches and logs
    orphanedItemStrategy {
        discardOldItems {
            numToKeep( 0 )
            daysToKeep( 1 )
        }
    }
}

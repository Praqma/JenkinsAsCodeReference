#!groovy
/**
* Pipeline configuration for JenkinsAsCode review-jobs
*/


/* global definitions */
def integrationBranch = 'env.default_branch'

/**
* begin pipeline
*/

if (env.BRANCH_NAME == integrationBranch) {
    currentBuild.result = 'NOT_BUILT'
    return
}

currentBuild.result = 'SUCCESS'
try {
    node( env.utility_slave ) {
        ansiColor('xterm') {
            // execute tests using the job-dsl-plugin for private branches
            stage ( 'Checkout' ) {
                scm checkout
            }
            stage ( 'Build XML' ) {
                dir ('jobdsl-gradle') {
                    sh script: '''
                        ./gradlew --no-daemon buildXMl
                    '''.stripIndent().trim()
                }
            }
            stage ( 'test' ) {
                dir ('jobdsl-gradle') {
                    sh script: '''
                        ./gradlew --no-daemon test
                    '''.stripIndent().trim()
                }
            }
            stage ( 'Build Docker' ) {
                dir ('dockerizeit') {
                    sh script: '''
                        # Assume no proxy if it is not set
                        export http_proxy=${http_proxy:-}
                        export https_proxy=${https_proxy:-}
                        export no_proxy=${no_proxy:-}
                        env
                        docker-compose build
                    '''.stripIndent().trim()
                }
            }
            stage ( 'Generate compose yml' ) {
                dir ('dockerizeit') {
                    sh script: '''
                        ./generate-compose.py \
                                            --debug \
                                            --file docker-compose.yml \
                                            --jmaster-image test-image \
                                            --jmaster-version test-version \
                                            --jslave-image test-image \
                                            --jslave-version test-version \
                                            && cat docker-compose.yml \
                                            && git checkout HEAD docker-compose.yml
                    '''.stripIndent().trim()
                }
            }
            stage ( 'Build Munchausen' ) {
                dir ('dockerizeit/munchausen') {
                    sh script: '''
                        cp ../docker-compose.yml .
                        docker build \
                                    --build-arg http_proxy \
                                    --build-arg https_proxy \
                                    --build-arg no_proxy \
                                    -t munchausen \
                                    .
                    '''.stripIndent().trim()
                }
            }
        }
    }
} catch ( e ) {
   currentBuild.result = 'FAILURE'
   throw e
}
finally {
   println ( "done" )
}
/**
* end pipeline
*/

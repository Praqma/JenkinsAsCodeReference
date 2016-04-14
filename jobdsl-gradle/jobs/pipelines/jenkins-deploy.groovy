node {
   stage 'Checkout'
   checkout([$class: 'GitSCM',
             branches: [[name: '*/master']],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'CleanBeforeCheckout']],
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:Praqma/JenkinsAsCodeReference.git']]])

   stage 'Verify JobDSL'
   sh 'cd jobdsl-gradle; ./gradlew buildXml'
   sh 'cd jobdsl-gradle; ./gradlew test'

   stage 'Build master Docker image'
   sh 'cd dockerizeit/master; docker build -t registry:5000/jmaster:$(git describe --tags) .; docker tag registry:5000/jmaster:$(git describe --tags) registry:5000/jmaster:latest'

   stage 'Build slave Docker image'
   sh 'cd dockerizeit/slave; docker build -t registry:5000/jslave:$(git describe --tags) .; docker tag registry:5000/jslave:$(git describe --tags) registry:5000/jslave:latest'

   stage 'Push to Artifactory'
   sh 'docker push registry:5000/jmaster:$(git describe --tags) registry:5000/jmaster:latest registry:5000/jslave:$(git describe --tags) registry:5000/jslave:latest'
   
   stage 'Deploy'
}
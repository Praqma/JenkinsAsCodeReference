node {
   stage 'Checkout'
   checkout([$class: 'GitSCM',
             branches: [[name: '*/master']],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'CleanBeforeCheckout']],
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: 'jenkins', url: 'git@github.com:Praqma/JenkinsAsCodeReference.git']]])

   stage 'Verify JobDSL'
   sh 'cd jobdsl-gradle && ./gradlew buildXml'
   sh 'cd jobdsl-gradle && ./gradlew test'

   stage 'Build master Docker image'
   sh 'cd dockerizeit/master && docker build -t localhost:5000/reference/jmaster:$(git describe --tags) .'
   sh 'docker tag localhost:5000/reference/jmaster:$(git describe --tags) localhost:5000/reference/jmaster:latest'

   stage 'Build slave Docker image'
   sh 'cd dockerizeit/slave && docker build -t localhost:5000/reference/jslave:$(git describe --tags) .'
   sh 'docker tag localhost:5000/reference/jslave:$(git describe --tags) localhost:5000/reference/jslave:latest'

   stage 'Push to registry'
   parallel pushMaster: {
        sh 'docker push localhost:5000/reference/jmaster:$(git describe --tags)'
        sh 'docker push localhost:5000/reference/jmaster:latest'
    }, pushSlave: {
        sh 'docker push localhost:5000/reference/jslave:$(git describe --tags)'
        sh 'docker push localhost:5000/reference/jslave:latest'
    },
    failFast: true

   stage 'Deploy'
   sh 'cd dockerizeit/munchausen && docker build -t munchausen . && docker run -d -v /var/run/docker.sock:/var/run/docker.sock munchausen $(git describe --tags)'
}
node {
   stage 'Checkout'
   checkout([$class: 'GitSCM',
             branches: [[name: "*/${env.default_branch}"]],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'CleanBeforeCheckout']],
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: env.default_credentials, url: env.default_repo]]])

   stage 'Verify JobDSL'
   sh 'cd jobdsl-gradle && ./gradlew buildXml'
   sh 'cd jobdsl-gradle && ./gradlew test'

   stage 'Build master Docker image'
   sh 'cd dockerizeit/master && docker build --build-arg jenkins_image_version=localhost:5000/reference/jmaster:$(git describe --tags) --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t localhost:5000/reference/jmaster:$(git describe --tags) .'
   sh 'docker tag localhost:5000/reference/jmaster:$(git describe --tags) localhost:5000/reference/jmaster:latest'

   stage 'Build slave Docker image'
   sh 'cd dockerizeit/slave && docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t localhost:5000/reference/jslave:$(git describe --tags) .'
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
   sh './dockerizeit/generate-compose.py --debug --file dockerizeit/docker-compose.yml --jmaster-image localhost:5000/reference/jmaster --jmaster-version $(git describe --tags) --jslave-image localhost:5000/reference/jslave --jslave-version $(git describe --tags)'
   sh 'cp docker-compose.yml dockerizeit/munchausen/'
   sh 'cd dockerizeit/munchausen && docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t munchausen . && docker run -d -v /var/run/docker.sock:/var/run/docker.sock munchausen $(git describe --tags)'
   archive 'docker-compose.yml'
}
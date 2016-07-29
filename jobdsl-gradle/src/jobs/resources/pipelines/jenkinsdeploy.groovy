// Pipeline doesn't resolve env outside of node so to read node name
// we have to do the trick below
def nodeName = ""
node() { nodeName = env.utility_slave }

node(nodeName) {
   stage 'Checkout'
   checkout([$class: 'GitSCM',
             branches: [[name: "*/${env.default_branch}"]],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'CleanBeforeCheckout']],
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: env.default_credentials, url: env.default_repo]]])

   stage 'Verify JobDSL'
   dir('jobdsl-gradle'){
     sh './gradlew --daemon buildXml'
     sh './gradlew --daemon test'
   }

   stage 'Build master Docker image'
   dir('dockerizeit/master'){
     sh 'docker build --build-arg master_image_version=${master_image_name}:$(git describe --tags) --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy --build-arg JAVA_OPTS -t ${master_image_name}:$(git describe --tags) .'
   }
   sh 'docker tag ${master_image_name}:$(git describe --tags) ${master_image_name}:latest'

   stage 'Build slave Docker image'
   dir('dockerizeit/slave'){
     sh 'docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t ${slave_image_name}:$(git describe --tags) .'
   }
   sh 'docker tag ${slave_image_name}:$(git describe --tags) ${slave_image_name}:latest'

   stage 'Push to registry'
   parallel pushMaster: {
        sh 'docker push ${master_image_name}:$(git describe --tags)'
        sh 'docker push ${master_image_name}:latest'
    }, pushSlave: {
        sh 'docker push ${slave_image_name}:$(git describe --tags)'
        sh 'docker push ${slave_image_name}:latest'
    },
    failFast: true

   stage 'Deploy'
   sh './dockerizeit/generate-compose.py --debug --file dockerizeit/docker-compose.yml --jmaster-image ${master_image_name} --jmaster-version $(git describe --tags) --jslave-image ${slave_image_name} --jslave-version $(git describe --tags)'
   sh 'cp docker-compose.yml dockerizeit/munchausen/'
   dir('dockerizeit/munchausen'){
     sh 'docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t munchausen . && docker run -d -v /var/run/docker.sock:/var/run/docker.sock munchausen $(git describe --tags)'
   }
   archive 'docker-compose.yml'
}

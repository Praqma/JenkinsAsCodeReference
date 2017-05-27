node(env.utility_slave) {
   stage('Checkout') {
       checkout([$class: 'GitSCM',
             branches: [[name: "*/${env.default_branch}"]],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'CleanBeforeCheckout']],
             submoduleCfg: [],
             userRemoteConfigs: [[credentialsId: env.default_credentials, url: env.default_repo]]])
   }

   stage('Verify JobDSL') {
       sh 'cd jobdsl-gradle && ./gradlew buildXml'
       sh 'cd jobdsl-gradle && ./gradlew test'
   }

   stage('Build master Docker image') {
       // docker pipeline plugin build command does not implement to take arguments yet
       sh 'cd dockerizeit/master && docker build --build-arg master_image_version=${master_image_name}:$(git describe --tags) --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy --build-arg JAVA_OPTS -t ${master_image_name}:$(git describe --tags) .'
   }
   
    // This have to be outside of stages to be available for other stages
    def masterImg = docker.image('${master_image_name}:$(git describe --tags) ')

    stage('Tag master Docker image') {
       masterImg.tag('$(git describe --tags)')
       masterImg.tag('latest')
    }

    stage('Build slave Docker image') {
       sh 'cd dockerizeit/slave && docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t ${slave_image_name}:$(git describe --tags) .'
    }

    // This have to be outside of stages to be available for other stages
    def slaveImg = docker.image('${slave_image_name}:$(git describe --tags)')

    stage ('Tag slave Docker image') {
       slaveImg.tag('$(git describe --tags)')
       slaveImg.tag('latest')
    }

    stage('Push to registry') {
        parallel(
            pushMaster: {
                docker.withRegistry(env.default_registry_url, env.default_registry_credId) {
                    masterImg.push('$(git describe --tags)')
                    masterImg.push('latest')}
            },
            pushSlave: {
                docker.withRegistry(env.default_registry_url, env.default_registry_credId) {
                slaveImg.push('$(git describe --tags)')
                slaveImg.push('latest')}
            },
            failFast: true)
    }

    stage('Deploy') {
        sh './dockerizeit/generate-compose.py --debug --file dockerizeit/docker-compose.yml --jmaster-image ${master_image_name} --jmaster-version $(git describe --tags) --jslave-image ${slave_image_name} --jslave-version $(git describe --tags)'
        sh 'cp docker-compose.yml dockerizeit/munchausen/'
        sh 'cd dockerizeit/munchausen && docker build --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t munchausen . && docker run -d -v /var/run/docker.sock:/var/run/docker.sock munchausen $(git describe --tags)'
        archive 'docker-compose.yml'
    }
}


node('utility-slave') {
    stage('Checkout') {
        checkout([$class: 'GitSCM',
            branches: [[name: "*/ready/**"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[$class: 'CleanBeforeCheckout'], 
                pretestedIntegration(gitIntegrationStrategy: accumulated(), integrationBranch: "${env.default_branch}", repoName: 'origin')],
            submoduleCfg: [],
            userRemoteConfigs: [[credentialsId: env.default_credentials, url: env.default_repo]]])
    }

    def compose_prefix = 'dockerizeit'

    stage('Verify JobDSL') {
        proxyHTTP = ""
        if(env.http_proxy) {
            def tokens = env.http_proxy.replace("http://", "").replace("https://", "").split(':')
            def host = tokens[0]
            def port = tokens[1]
            proxyHTTP = "-Dhttp.proxyHost=${host} -Dhttp.proxyPort=${port}"
        }

        proxyHTTPS = ""
        if(env.https_proxy) {
            def tokens = env.https_proxy.replace("http://", "").replace("https://", "").split(':')
            def host = tokens[0]
            def port = tokens[1]
            proxyHTTPS = "-Dhttps.proxyHost=${host} -Dhttps.proxyPort=${port}"
        }

        def nonProxy = ""
        if ( env.no_proxy ) {
            def hostList = env.no_proxy.replace(',','\\|')
            nonProxy = "-Dhttp.nonProxyHosts=${hostList} -Dhttps.nonProxyHosts=${hostList}".replace('\\|.','\\|*.')
        }

        sh "cd jobdsl-gradle && ./gradlew ${proxyHTTP} ${proxyHTTPS} ${nonProxy} buildXml"
        sh "cd jobdsl-gradle && ./gradlew ${proxyHTTP} ${proxyHTTPS} ${nonProxy} test"
    }

    stage('Build master Docker image') {
        // docker pipeline plugin build command does not implement to take arguments yet
        sh 'cd dockerizeit/master && docker build --build-arg master_image_version=${master_image_name}:$(git describe --tags) --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy --build-arg JAVA_OPTS -t ${master_image_name}:$(git describe --tags) .'
    }
   
    stage('Build slave Docker image') {
       sh 'cd dockerizeit/slave && docker build --build-arg JAVA_PROXY --build-arg http_proxy --build-arg https_proxy --build-arg no_proxy -t ${slave_image_name}:$(git describe --tags) .'
    }

    stage('Publish') {
        pretestedIntegrationPublisher()
    }

}

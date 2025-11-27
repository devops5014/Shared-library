def call(String gitUrl, String gitToken) {
    pipeline {
        agent { label 'qa-server' }

        stages {
            stage('Checkout') {
                steps {
                    script {
                        echo "Cloning repository: ${gitUrl}"
                        checkout([
                                $class: 'GitSCM',
                                branches: [[name: '*/main']],
                                userRemoteConfigs: [[
                                                            url: gitUrl,
                                                            credentialsId: gitToken
                                                    ]]
                        ])
                    }
                }
            }

            stage('Build') {
                steps {
                    sh 'mvn clean install'
                }
            }

            stage('Test') {
                steps {
                    sh 'mvn test'
                }
            }

            stage('Deploy to Artifactory') {
                steps {
                    configFileProvider([
                            configFile(
                                    fileId: 'da662eac-7a53-4268-beec-69db67ad3a65',
                                    variable: 'MAVEN_SETTINGS'
                            )
                    ]) {
                        sh 'mvn deploy -s $MAVEN_SETTINGS'
                    }
                }
            }
        }
    }
}

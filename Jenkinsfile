pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Chrome (for Selenium)') {
            steps {
                sh '''
                    apt-get update -qq
                    apt-get install -y -qq wget gnupg
                    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub \
                        | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg
                    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
                        > /etc/apt/sources.list.d/google-chrome.list
                    apt-get update -qq
                    apt-get install -y -qq google-chrome-stable
                '''
            }
        }

        stage('Unit & Integration Tests') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Coverage Report') {
            steps {
                sh 'mvn -B jacoco:report'
                archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
            }
        }

        stage('Selenium System Tests') {
            steps {
                sh 'mvn -B verify'
            }
            post {
                always {
                    junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished — see the archived JUnit and JaCoCo reports above.'
        }
        failure {
            echo 'Build failed — check the Unit/Integration or Selenium stage logs for the failing test.'
        }
    }
}

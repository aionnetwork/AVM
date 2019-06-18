properties([[$class: 'jenkins.model.BuildDiscarderProperty', strategy:
            [$class: 'LogRotator', numToKeepStr: '50', artifactNumToKeepStr: '10']
            ]])
            
pipeline {
    agent any

    environment {
        JAVA_HOME = "${env.JAVA_HOME}"
        ANT_HOME = "${env.ANT_HOME}"
        SYSTEM_TESTS_HOME = "test"
        GIT_BRANCH = "${env.BRANCH_NAME}"
        PATH = "${JAVA_HOME}:${JAVA_HOME}/bin:${PATH}"
    }

    stages {
        stage('Build') {
            steps {
                echo "Building branch: ${env.BRANCH_NAME}"
                sh "export JAVA_HOME"
                echo "JAVA_HOME: ${JAVA_HOME}"
                sh "export PATH"
                echo "PATH: ${PATH}"
                sh "ant" 
            }
        }
        
        stage('Unit test') {
            steps {
                timeout(30) {
                    sh "ant test" 
                }
            }
        }

        stage('Archive build output') {
            when {
                expression {
                    GIT_BRANCH == 'master'
                }
            }

            steps {
                sh "ant package"
                archiveArtifacts artifacts: 'embedding/*.jar'
            }
        }
    }

    post {
        success {
            slackSend channel: '#ci',
                color: 'good',
                message: "The pipeline ${currentBuild.fullDisplayName} completed successfully. Grab the generated builds at ${env.BUILD_URL}"
        } 

        failure {
            slackSend channel: '#ci',
                color: 'danger', 
                message: "The pipeline ${currentBuild.fullDisplayName} failed at ${env.BUILD_URL}"
        }
    }
}

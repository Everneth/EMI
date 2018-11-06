pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk 'Java'
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building artifacts...'
                sh 'mvn -Dmaven.test.failure.ignore=true package'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/*.jar', onlyIfSuccessful: true
        }
    }

}

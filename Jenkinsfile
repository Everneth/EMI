pipline {
    agent any
    tools {
        maven 'Maven 3.0.5'
        jdk 'jdk8'
    }
    stages {
        stage('Build) {
            steps {
                echo 'Building artifacts...'
                sh 'mvn -Dmaven.test.failure.ignore=true
            }
        }
    }
}

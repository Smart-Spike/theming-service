pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh sbt test
            }
        }
        stage('Deploy') {
            steps {
                sh sbt docker:deploy
            }
        }
    }
}
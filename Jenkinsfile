pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git 'https://github.com/MNCuong/demohelm.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }
    }
}
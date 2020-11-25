pipeline {
    agent any

    stages {
        stage('deploy') {
            steps {
                script{
                    deploy("123")
                }
            }
        }
    }
}

def deploy(String build) {
    sh "echo deploy ${build}"
}

def clean() {
    sh 'echo clean'
}
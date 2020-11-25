def deploy(){
    sh 'deploy'
}

def clean(){
    sh 'clean'
}

def call(Map<String, String> pipelineConfig) {
    pipeline{
        agent any

        stages{
           stage('Update repository'){
               sh 'remote file'
           }
        }
    }
}

return this;
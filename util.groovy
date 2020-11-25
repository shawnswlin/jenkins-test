def deploy(string build) {
    sh "echo deploy ${build}"
}

def clean() {
    sh 'echo clean'
}

return this;
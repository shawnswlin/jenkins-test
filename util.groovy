def deploy(string build) {
    sh "echo deploy ${build}"
    clean()
}

def clean() {
    sh 'echo clean'
}

return this;
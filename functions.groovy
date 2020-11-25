def execute(String stage, String version) {
    sh "aws s3 cp s3://network-security-static-ui-rnd-us-east-1/$version s3://network-security-static-ui-${stage}-us-east-1/_network --recursive --acl bucket-owner-full-control"
}

def success(String stage, String version) {
    sh "aws s3api put-bucket-tagging --bucket network-security-static-ui-${stage}-us-east-1 --tagging 'TagSet=[{Key=version, Value=${version}}]'"
}

def rollback(String stage, String version) {
    old_version = sh(script:"aws s3api get-bucket-tagging --bucket network-security-static-ui-${stage}-us-east-1  | grep -A1 \"Key.*version\" | grep -v \"Key.*version\" | cut -d: -f2 | xargs", returnStdout: true).trim()
    echo "roll back to version ${old_version}"
    execute(stage, old_version)
}
return this;
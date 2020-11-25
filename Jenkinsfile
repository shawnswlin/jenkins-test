@Library('ms-pipeline-lib@v2.3') _

import com.trendmicro.tp.Util

def ticket = 'TIP-NA'

pipeline {
    agent any

    parameters {
        string name: 'service', description: 'Microservice to deploy', trim: true
        string name: 'version', description: 'Version of microservice to deploy', trim: true
        choice name: 'stage', choices: ['develop'], description: 'Deployment stage'
        string name: 'pipelineSlackChannel', description: 'Slack channel for target pipeline to post notifications to', defaultValue: '', trim: true
    }

    options {
        withAWS credentials: "aws-c1ns-${params.stage}-infra-deploy"
        buildDiscarder logRotator(numToKeepStr: '50')
    }
    
    environment {
        C1NS_STAGE          = "${params.stage}"
        AWS_DEFAULT_REGION  = 'us-east-1'
        SLACK_CHANNEL       = "c1-network-${params.stage}-release"
        CHECKOUT_DIR        = "${params.stage}-${params.service}-${params.version}"
        GIT_TAG             = "v${params.version}"

        // Deprecated env var
        c1net_env = "${C1NS_STAGE}"
    }
    
    stages {
        stage('Clone') {
            steps {
                buildName "${params.service} ${params.version} ${params.stage}"

                checkout poll: false, scm: [
                    $class: 'GitSCM',
                    branches: [[name: GIT_TAG]],
                    extensions: [
                        [
                            $class: 'RelativeTargetDirectory',
                            relativeTargetDir: CHECKOUT_DIR
                        ]
                    ],
                    userRemoteConfigs: [
                        [
                            credentialsId: env.GIT_SSH_CREDS_VAR,
                            url: "git@dsgithub.trendmicro.com:tp-network-protect/${params.service}.git"
                        ]
                    ]
                ]
            }
        }
        
        stage('Execute') {
            steps {
                rollback(params.stage, params.version)
            }    
        }
    }
    
    post {
        always {
            dir(CHECKOUT_DIR) {
                script {
                    def util = new Util()

                    def athere = currentBuild.result != null && currentBuild.result != 'SUCCESS'
                    def commitMessage = gitCommitSubject commit: GIT_TAG
                    commitMessage = util.formatCommitMessageForSlack(commitMessage, params.service)
                    echo commitMessage
                }
            }
        }
        success {
            success(params.stage, params.version)
        }
        unsuccessful {
            rollback(params.stage, params.version)
        }
        cleanup {
            cleanWs()
        }
    }
}


def execute(String stage, String version) {
    sh "aws s3 cp s3://network-security-static-ui-rnd-us-east-1/$version s3://network-security-static-ui-${stage}-us-east-1/_network --recursive --acl bucket-owner-full-control"
}

def success(String stage, String version) {
    sh "aws s3api put-bucket-tagging --bucket network-security-static-ui-${stage}-us-east-1 --tagging 'TagSet=[{Key=version, Value=${version}}]'"
}

def rollback(String stage, String version) {
    old_version = sh(script:"aws s3api get-bucket-tagging --bucket network-security-static-ui-${stage}-us-east-1  | grep -A1 \"Key.*version\" | grep -v \"Key.*version\" | cut -d: -f2 | xargs", returnStdout: true).trim()
    sh "roll back to version ${old_version}"
    execute(stage, old_version)
}
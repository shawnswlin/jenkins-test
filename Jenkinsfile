@Library('ms-pipeline-lib@v2.3') _

import com.trendmicro.tp.Util

def functions;
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

        stage('Load Functions') {
            steps {
                script {
                    functions = load("${env.CHECKOUT_DIR}/cli-pipeline.groovy")
                }
            }
        }
        
        stage('Execute') {
            steps {
                script{
                    functions.execute(params.stage, params.version)
                }
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
                    slackNotify(SLACK_CHANNEL, currentBuild.result, "\n${commitMessage}", athere)
                    if (params.pipelineSlackChannel != '') {
                        slackNotify(params.pipelineSlackChannel, currentBuild.result, "\n${commitMessage}", athere)
                    }
                }
            }
        }
        success {
            script{
                functions.success(params.stage, params.version)
            }
        }
        unsuccessful {
            script{
                functions.rollback(params.stage, params.version)
            }
        }
        cleanup {
            cleanWs()
        }
    }
}

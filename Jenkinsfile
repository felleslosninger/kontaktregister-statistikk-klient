import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

import static java.time.ZonedDateTime.now

def verificationHostName = 'eid-test-docker01.dmz.local'
def verificationHostUser = 'jenkins'
def verificationHostSshKey = 'ssh.git.difi.local'
def verificationStackName = 'krr'
def productionHostName = 'eid-prod-docker01.dmz.local'
def productionHostUser = 'jenkins'
def productionHostSshKey = 'ssh.git.difi.local'
def productionStackName = 'krr'
def gitSshKey = 'ssh.github.com'

pipeline {
    agent none
    options {
        timeout(time: 5, unit: 'DAYS')
        disableConcurrentBuilds()
        ansiColor('xterm')
        timestamps()
    }
    stages {
        stage('Check build') {
            when { expression { env.BRANCH_NAME.matches(/(work|feature|bugfix)\/(\w+-\w+)/) } }
            agent any
            steps {
                script {
                    currentBuild.description = "Building from commit " + readCommitId()
                    env.MAVEN_OPTS = readProperties(file: 'Jenkinsfile.properties').MAVEN_OPTS
                    if (readCommitMessage() == "ready!") {
                        env.verification = 'true'
                    }
                }
                sh "mvn clean verify -B"
            }
        }
        stage('Wait for code reviewer to start') {
            when { expression { env.BRANCH_NAME.matches(/(work|feature|bugfix)\/(\w+-\w+)/) && env.verification == 'true' } }
            steps {
                script {
                    retry(count: 1000000) {
                        if (issueStatus(issueId(env.BRANCH_NAME)) != env.ISSUE_STATUS_CODE_REVIEW) {
                            sleep 10
                            error("Issue is not yet under code review")
                        }
                    }
                }
            }
        }
        stage('Wait for verification slot') {
            when { expression { env.BRANCH_NAME.matches(/(work|feature|bugfix)\/(\w+-\w+)/) && env.verification == 'true' } }
            agent any
            steps {
                script {
                    sshagent([gitSshKey]) {
                        retry(count: 1000000) {
                            sleep 10
                            sh 'pipeline/git/available-verification-slot'
                        }
                    }
                }
            }
        }
        stage('Create code review') {
            when { expression { env.BRANCH_NAME.matches(/(work|feature|bugfix)\/(\w+-\w+)/) && env.verification == 'true' } }
            environment {
                crucible = credentials('crucible')
            }
            agent any
            steps {
                script {
                    version = DateTimeFormatter.ofPattern('yyyy-MM-dd-HHmm').format(now(ZoneId.of('UTC'))) + "-" + readCommitId()
                    sshagent([gitSshKey]) {
                        verifyRevision = sh returnStdout: true, script: "pipeline/git/create-verification-revision ${version}"
                    }
                    sh "pipeline/create-review ${verifyRevision} ${env.crucible_USR} ${env.crucible_PSW}"
                }
            }
            post {
                failure { sshagent([gitSshKey]) { sh "git push origin --delete verify/\${BRANCH_NAME}" }}
                aborted { sshagent([gitSshKey]) { sh "git push origin --delete verify/\${BRANCH_NAME}" }}
            }
        }
        stage('Build artifacts') {
            when { expression { env.BRANCH_NAME.matches(/verify\/(work|feature|bugfix)\/(\w+-\w+)/) } }
            environment {
                nexus = credentials('nexus')
            }
            agent any
            steps {
                script {
                    version = versionFromCommitMessage()
                    currentBuild.description = "Building ${version} from commit " + readCommitId()
                    env.MAVEN_OPTS = readProperties(file: 'Jenkinsfile.properties').MAVEN_OPTS
                    sh "mvn versions:set -B -DnewVersion=${version}"
                    sh "mvn deploy -B -s settings.xml -Ddocker.release.username=${env.nexus_USR} -Ddocker.release.password=${env.nexus_PSW} -DdeployAtEnd=true"
                }
            }
            post {
                failure { sshagent([gitSshKey]) { sh "git push origin --delete \${BRANCH_NAME}" }}
                aborted { sshagent([gitSshKey]) { sh "git push origin --delete \${BRANCH_NAME}" }}
            }
        }
        stage('Wait for code reviewer to finish') {
            when { expression { env.BRANCH_NAME.matches(/verify\/(work|feature|bugfix)\/(\w+-\w+)/) } }
            steps {
                script {
                    env.codeApproved = "false"
                    env.jobAborted = "false"
                    try {
                        retry(count: 1000000) {
                            if (issueStatus(issueId(env.BRANCH_NAME)) == env.ISSUE_STATUS_CODE_REVIEW) {
                                sleep 10
                                error("Issue is still under code review")
                            }
                        }
                        if (issueStatus(issueId(env.BRANCH_NAME)) == env.ISSUE_STATUS_CODE_APPROVED)
                            env.codeApproved = "true"
                    } catch (FlowInterruptedException e) {
                        env.jobAborted = "true"
                    }
                }
            }
        }
        stage('Integrate code') {
            when { expression { env.BRANCH_NAME.matches(/verify\/(work|feature|bugfix)\/(\w+-\w+)/) } }
            agent any
            steps {
                script {
                    if (env.jobAborted == "true") {
                        error("Job was aborted")
                    } else if (env.codeApproved == "false") {
                        error("Code was not approved")
                    }
                    sshagent([gitSshKey]) {
                        sh 'git push origin HEAD:master'
                    }
                }
            }
            post {
                always {
                    sshagent([gitSshKey]) { sh "git push origin --delete \${BRANCH_NAME}" }
                }
                success {
                    sshagent([gitSshKey]) { sh "git push origin --delete \${BRANCH_NAME#verify/}" }
                }
            }
        }
        stage('Wait for tester to start') {
            when { branch 'master' }
            steps {
                script {
                    env.jobAborted = 'false'
                    try {
                        input message: "Ready to perform manual behaviour verification?", ok: "Yes"
                    } catch (Exception ignored) {
                        env.jobAborted = 'true'
                    }
                }
            }
        }
        stage('Deploy for manual verification') {
            when { branch 'master' }
            environment {
                nexus = credentials('nexus')
            }
            agent {
                dockerfile {
                    dir 'docker'
                    args '-v /var/jenkins_home/.ssh/known_hosts:/root/.ssh/known_hosts -u root:root'
                }
            }
            steps {
                script {
                    if (env.jobAborted == 'true') {
                        error('Job was aborted')
                    }
                    version = versionFromCommitMessage()
                    currentBuild.description = "Deploying ${version} to manual verification environment"
                    DOCKER_HOST = sh(returnStdout: true, script: 'pipeline/docker/define-docker-host-for-ssh-tunnel')
                    sshagent([verificationHostSshKey]) {
                        sh "DOCKER_HOST=${DOCKER_HOST} pipeline/docker/create-ssh-tunnel-for-docker-host ${verificationHostUser}@${verificationHostName}"
                    }
                    sh "DOCKER_TLS_VERIFY= DOCKER_HOST=${DOCKER_HOST} docker/run ${env.nexus_USR} ${env.nexus_PSW} ${verificationStackName} ${version}"
                }
            }
            post {
                always {
                    sh "pipeline/docker/cleanup-ssh-tunnel-for-docker-host"
                }
            }
        }
        stage('Wait for tester to approve manual verification') {
            when { branch 'master' }
            steps {
                script {
                    env.jobAborted = 'false'
                    try {
                        input message: "Approve manual verification?", ok: "Yes"
                    } catch (Exception ignored) {
                        env.jobAborted = 'true'
                    }
                }
            }
        }
        stage('Deploy for production') {
            when { branch 'master' }
            environment {
                nexus = credentials('nexus')
            }
            agent {
                dockerfile {
                    dir 'docker'
                    args '-v /var/jenkins_home/.ssh/known_hosts:/root/.ssh/known_hosts -u root:root'
                }
            }
            steps {
                script {
                    if (env.jobAborted == 'true') {
                        error('Job was aborted')
                    }
                    version = versionFromCommitMessage()
                    currentBuild.description = "Deploying ${version} to production environment"
                    DOCKER_HOST = sh(returnStdout: true, script: 'pipeline/docker/define-docker-host-for-ssh-tunnel')
                    sshagent([productionHostSshKey]) {
                        sh "DOCKER_HOST=${DOCKER_HOST} pipeline/docker/create-ssh-tunnel-for-docker-host ${productionHostUser}@${productionHostName}"
                    }
                    sh "DOCKER_TLS_VERIFY= DOCKER_HOST=${DOCKER_HOST} docker/run ${env.nexus_USR} ${env.nexus_PSW} ${productionStackName} ${version}"
                }
            }
            post {
                always {
                    sh "pipeline/docker/cleanup-ssh-tunnel-for-docker-host"
                }
            }
        }
    }
    post {
        success {
            echo "Success"
            notifySuccess()
        }
        unstable {
            echo "Unstable"
            notifyUnstable()
        }
        failure {
            echo "Failure"
            notifyFailed()
        }
        aborted {
            echo "Aborted"
            notifyFailed()
        }
        always {
            echo "Build finished"
        }
    }
}

String versionFromCommitMessage() {
    return readCommitMessage().tokenize(':')[0]
}

def notifyFailed() {
    emailext (
            subject: "FAILED: '${env.JOB_NAME}'",
            body: """<p>FAILED: Bygg '${env.JOB_NAME} [${env.BUILD_NUMBER}]' feilet.</p>
            <p><b>Konsoll output:</b><br/>
            <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
            recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']]
    )
}

def notifyUnstable() {
    emailext (
            subject: "UNSTABLE: '${env.JOB_NAME}'",
            body: """<p>UNSTABLE: Bygg '${env.JOB_NAME} [${env.BUILD_NUMBER}]' er ustabilt.</p>
            <p><b>Konsoll output:</b><br/>
            <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
            recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']]
    )
}

def notifySuccess() {
    if (isPreviousBuildFailOrUnstable()) {
        emailext (
                subject: "SUCCESS: '${env.JOB_NAME}'",
                body: """<p>SUCCESS: Bygg '${env.JOB_NAME} [${env.BUILD_NUMBER}]' er oppe og snurrer igjen.</p>""",
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']]
        )
    }
}

boolean isPreviousBuildFailOrUnstable() {
    if(!hudson.model.Result.SUCCESS.equals(currentBuild.rawBuild.getPreviousBuild()?.getResult())) {
        return true
    }
    return false
}

static def issueId(def branchName) {
    return branchName.tokenize('/')[-1]
}

String issueStatus(def issueId) {
    return jiraGetIssue(idOrKey: issueId, site: 'jira').data.fields['status']['id']
}

def readCommitId() {
    return sh(returnStdout: true, script: 'git rev-parse HEAD').trim().take(7)
}

def readCommitMessage() {
    return sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
}

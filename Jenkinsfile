#!groovy
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static java.time.ZonedDateTime.now

String version = DateTimeFormatter.ofPattern('yyyy-MM-dd-HHmm').format(now(ZoneId.of('UTC')))
String featureBranch = /feature\/(\w+-\w+)/

stage('Build') {
    node {
        checkout scm
        env.commitId = readCommitId()
        env.commitMessage = readCommitMessage()
        stash includes: 'pipeline/*', name: 'pipeline'
        currentBuild.description = "Commit: " + env.commitId.take(6) + "\n" + "Feature: " + readChange();
        if (isDeployBuild()) {
            currentBuild.displayName = "#${currentBuild.number}: Deploy version ${version}"
            currentBuild.description = "Commit: ${env.commitId}"
            sh "pipeline/build.sh deliver ${version}"
        }
        else if (env.BRANCH_NAME.matches(featureBranch)) {
            jiraId = (env.BRANCH_NAME =~ featureBranch)[0][1]
            currentBuild.displayName = "#${currentBuild.number}: Build for feature ${jiraId}"
            currentBuild.description = "Feature: ${jiraId} Commit: ${env.commitId}"
            sh "pipeline/build.sh verify"
        }
    }
}

if (isDeployBuild()) {
    stage('Staging deploy') {
        node {
            unstash 'pipeline'
            sh "ssh 'eid-test-docker01.dmz.local' bash -s -- < pipeline/application.sh update ${version}"
        }
    }
}

boolean isDeployBuild() {
    return env.BRANCH_NAME.matches('master')
}

String readChange() {
    return sh(returnStdout: true, script: 'pipeline/branch.sh change').trim()
}

String readCommitId() {
    return sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
}

String readCommitMessage() {
    return sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
}
#!groovy
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static java.time.ZonedDateTime.now

String version = DateTimeFormatter.ofPattern('yyyy-MM-dd-HHmm').format(now(ZoneId.of('UTC')))
String deployBranch = 'master'
String featureBranch = /feature\/(\w+-\w+)/

stage('Build') {
    node {
        checkout scm
        def commitId = commitId()
        stash includes: 'pipeline/*', name: 'pipeline'
        if (env.BRANCH_NAME.matches(deployBranch)) {
            currentBuild.displayName = "#${currentBuild.number}: Deploy version ${version}"
            currentBuild.description = "Commit: ${commitId}"
            sh "pipeline/build.sh deliver ${version}"
        } else if (env.BRANCH_NAME.matches(featureBranch)) {
            jiraId = (env.BRANCH_NAME =~ featureBranch)[0][1]
            currentBuild.displayName = "#${currentBuild.number}: Build for feature ${jiraId}"
            currentBuild.description = "Feature: ${jiraId} Commit: ${commitId}"
            sh "pipeline/build.sh verify"
        }
    }
}

def commitId() {
    sh 'git rev-parse HEAD > commit'
    return readFile('commit').trim()
}
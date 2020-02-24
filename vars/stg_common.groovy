#!groovy

// preflight stage -- common across pipelines
def preflight() {
    stage ('Preflight') {
        try {
            echo "Set limit to Discard old builds. Keep last 10 builds. Further, disallow concurrent builds."
            properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
        }
        catch (err) {
            def failureMessage = 'While cleaning up the workspace, something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }
}

// notify stage -- common across pipelines
def notify(emailRecp) {
    stage ('Notify') {
        try {
            echo "Send email"
            emailext attachLog: true, body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: "${emailRecp}"
        }
        catch (err) {
            def failureMessage = 'While notifying, something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }
}
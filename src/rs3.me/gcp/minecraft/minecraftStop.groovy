#!groovy

/* ===============================================

                    PIPELINE

=============================================== */

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Set Build Variables
    def gProject = 'mc-server'
    def gZone = 'us-central1-f'
    def emailRecp = 'richard.staehler@gmail.com'
    def slackNotifyChannel = '#08-gaming'

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight("${slackNotifyChannel}")
    }

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        try {
            def isOffline = mc_helpers.checkUp("${gZone}")
            
            if (isOffline == "TERMINATED") {
                echo "Nothing to do here."
            }
            else {
                mc_helpers.stopMinecraft("${gProject}", "${gZone}")
            }
        }
        catch (err) {
            def failureMessage = 'While stopping the server something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Verify Termination
    stage ('Verify') {
        try {
            def isOffline = mc_helpers.checkUp("${gZone}")
            
            if (isOffline == "TERMINATED") {
                echo "Your server is indeed terminated."
            }
            else {
                throw new Exception("Your server is not in a TERMINATED state. Check on your server!")
            }
        }
        catch (Exception e) {
            def failureMessage = "${e}"
            echo "${failureMessage}" + ": " + Exception
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail("${emailRecp}", "${slackNotifyChannel}")
        common_stages.notifySlack("${slackNotifyChannel}")
    }

}
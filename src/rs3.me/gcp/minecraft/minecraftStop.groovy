#!groovy

/* ===============================================

                    PIPELINE

=============================================== */

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    mc_variables.envVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.startSlack("${SLACK_NOTIFY_CHANNEL}")
        common_stages.preflight("${SLACK_NOTIFY_CHANNEL}")
    }

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        try {
            def isOffline = mc_helpers.checkUp("${G_ZONE}")
            if (isOffline == "TERMINATED") {
                echo "Nothing to do here."
            }
            else {
                mc_helpers.stopMinecraft("${G_PROJECT}", "${G_ZONE}")
            }
        }
        catch (err) {
            def failureMessage = 'While stopping the server something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${SLACK_NOTIFY_CHANNEL}", "${failureMessage}", err)
            throw err
        }
    }

    // Verify Termination
    stage ('Verify') {
        try {
            def isOffline = mc_helpers.checkUp("${G_ZONE}")
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
            common_helpers.notifySlackFail("${SLACK_NOTIFY_CHANNEL}", "${failureMessage}", err)
            throw err
        }
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail("${EMAIL_RECP}", "${SLACK_NOTIFY_CHANNEL}")
        common_stages.notifySlack("${SLACK_NOTIFY_CHANNEL}")
    }
}
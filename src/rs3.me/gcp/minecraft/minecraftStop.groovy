#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    mc_variables.envVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.startSlack()
        common_stages.startDiscord()
        common_stages.preflight()
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
            common_helpers.catchMe("${failureMessage}", err)
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
        catch (Exception err) {
            def failureMessage = "${err}"
            common_helpers.catchMe("${failureMessage}", err)
        }
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail("${EMAIL_RECP}")
        common_stages.notifyDiscord()
        common_stages.notifySlack()
    }
}
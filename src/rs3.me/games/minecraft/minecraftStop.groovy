#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    common_variables.envVariables()
    def isOffline = 'null'

    // Preflight Stage
    stage ('Preflight') {
        common_stages.actSA("${G_KEY}", "${G_INSTANCE}")
        isOffline = mc_helpers.checkUp("${G_ZONE}")
        if (isOffline == "TERMINATED") {
            currentBuild.result = 'SUCCESS'
            return
        }
        else {
            common_stages.startSlack()
            common_stages.startDiscord()
            common_stages.preflight()
        }
    }

    // Break pipeline if instance is already shut down
    if (currentBuild.result == 'SUCCESS') {
        echo "The instance is already shut down. No action."
        return
    }

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        common_stages.stopMCS("${G_ZONE}", "${G_PROJECT}", "${G_INSTANCE}", "${G_SERV_ACCT}")
    }

    // Verify Termination
    stage ('Verify') {
        common_stages.verifyMCSOffline("${G_ZONE}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.setMcsStatus("${G_ZONE}")
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
    }
}

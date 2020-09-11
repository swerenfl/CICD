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

    // Preflight Stage
    stage ('Preflight') {
        common_stages.startSlack()
        common_stages.startDiscord()
        common_stages.preflight()
        common_stages.actSA("${G_KEY}")
    }

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        common_stages.stopMCS("${G_ZONE}", "${G_PROJECT}")
    }

    // Verify Termination
    stage ('Verify') {
        common_stages.verifyMCSOffline("${G_ZONE}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
        build job: 'Minecraft_CHECK', wait: false
    }
}
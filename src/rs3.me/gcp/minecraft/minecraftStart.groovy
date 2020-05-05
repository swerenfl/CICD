#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
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

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail("${EMAIL_RECP}")
        common_stages.notifyDiscord()
        common_stages.notifySlack()
    }
}
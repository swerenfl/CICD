#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    common_variables.envVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.startSlack()
        common_stages.startDiscord()
        common_stages.actSA("${G_KEY}", "${G_INSTANCE}")
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
        build job: 'Minecraft_STATUS', wait: false
    }
}
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
        common_stages.preflight([parameters([choice(name: 'START_MODE', choices: "0\n1", description: '0 = server.jar, 1 = fabric.jar')])])
        common_stages.selectStartMode(binding.hasVariable('params') ? params?.START_MODE : null)
        common_stages.actSA("${G_KEY}", "${G_INSTANCE}")
        common_stages.startSlack()
        common_stages.startDiscord()
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}", "${env.START_MODE}")
    }

    // Wait until the server is listening before notifying users
    stage ('Wait Ready') {
        common_stages.waitForMcsReady("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
        build job: 'Minecraft_STATUS'
    }
}

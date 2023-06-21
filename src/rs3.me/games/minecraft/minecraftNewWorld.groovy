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
        common_stages.preflight()
        common_stages.actSA("${G_KEY}", "${G_INSTANCE}")
    }

    // Start Minecraft Stage. Need to turn on server at least to update server.properties
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Do changes
    stage ('Generate New World') {
        common_stages.newWorldOrder("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Restart server. Decided against using build job: Minecraft_RESTART as doing so would result
    // in preflight stages, notifications, etc., executing again. And it's just not needed.
    stage ('Restart Minecraft') {
        common_stages.stopMCS("${G_ZONE}", "${G_PROJECT}", "${G_INSTANCE}", "${G_SERV_ACCT}") // Stop
        common_stages.verifyMCSOffline("${G_ZONE}") // Verify
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}") // Start
    }

    // Put minecraft back into the state it was when this job started
    stage ('Post-Processing') {
        if (initialCheck == "RUNNING") {
            echo "Since the server was online when the new world process started, the server was left on."
            return
        }
        else {
            echo "Since the server was offline when the new world process started, the server was terminated."
            common_stages.stopMCS("${G_ZONE}", "${G_PROJECT}", "${G_INSTANCE}", "${G_SERV_ACCT}")
            common_stages.verifyMCSOffline("${G_ZONE}")
        }
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
        build job: 'Minecraft_STATUS'
    }
}
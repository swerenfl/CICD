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
        //common_stages.startDiscord("${discordWebURL}")
        common_stages.preflight("${SLACK_NOTIFY_CHANNEL}")
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}", "${SLACK_NOTIFY_CHANNEL}")
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail("${EMAIL_RECP}", "${SLACK_NOTIFY_CHANNEL}")
        common_stages.notifySlack("${SLACK_NOTIFY_CHANNEL}")
        //common_stages.notifyDiscord("${discordWebURL}")
    }
}
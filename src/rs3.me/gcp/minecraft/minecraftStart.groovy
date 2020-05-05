#!groovy

/* ===============================================

                    PIPELINE

=============================================== */

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Set Build Variables
    def gProject = "mc-server"
    def gInstance = "minecraft-project-2019-11-03"
    def gZone = "us-central1-f"
    def gServiceAcct = "jenkins"
    def emailRecp = "${EMAIL_RECP}" // masking since GitHub is public.
    def slackNotifyChannel = "#08-gaming"
    def discordWebURL = "https://discordapp.com/api/webhooks/707044633816989787/NqD89TdUZmJBSwcKv1PyYMrEiv1uzlglPMxz2tcd43HPZ2PhB595HzGG-Hw6S0dNxbJ2"

    // More Variables
    mc_variables.envVariables()

    // Preflight Stage
    stage ('Preflight') {
        echo sh(script: 'env|sort', returnStdout: true)
        sh 'sleep 120'

        common_stages.startSlack("${slackNotifyChannel}")
        //common_stages.startDiscord("${discordWebURL}")
        common_stages.preflight("${slackNotifyChannel}")
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${slackNotifyChannel}")
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail("${emailRecp}", "${slackNotifyChannel}")
        common_stages.notifySlack("${slackNotifyChannel}")
        //common_stages.notifyDiscord("${discordWebURL}")
    }
}
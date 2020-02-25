#!groovy

/* ===============================================

                    PIPELINE

=============================================== */

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Set Build Variables
    def gProject = 'mc-server'
    def gInstance = 'minecraft-project-2019-11-03'
    def gZone = 'us-central1-f'
    def gServiceAcct = 'jenkins'
    def emailRecp = 'richard.staehler@gmail.com'
    def slackNotifyChannel = '#08-gaming'

    // Preflight Stage
    stage ('Preflight') {
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
    }
}
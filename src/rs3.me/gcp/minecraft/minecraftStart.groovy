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

    // Preflight Stage
    stage ('Preflight') {
        stg_common.preflight()
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        stg_common.startMCS(gInstance, gZone, gServiceAcct, gProject)
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        stg_common.notifyEmail(emailRecp)
    }
}
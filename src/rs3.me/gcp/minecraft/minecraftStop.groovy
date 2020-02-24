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
    def emailRecp = 'richard.staehler@gmail.com'

    // Preflight Stage
    stg_common.preflight()

    // Stop Minecraft Stage
    mc_helpers.stopMinecraft()

    // Notify users of the build using the emailext plugin.
    stg_common.notify(emailRecp)

}
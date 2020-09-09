#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Load Website Variables
    variables.lhVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Deploy LH to the bucket
    stage ('Deploy') {
        common_stages.deployWebsite(xxxxx)
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
    }
}

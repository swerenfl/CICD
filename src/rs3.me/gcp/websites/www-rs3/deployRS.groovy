#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {
    
    // Load Env Variables
    common_variables.rsVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Checkout RS3 Repo
    stage ('Checkout') {
        git branch: 'main', credentialsId: '46384ba0-4e05-4e9b-aa38-97b82212c811', url: 'https://github.com/swerenfl/www-rs3'
    }

    // Deploy LH to Firebase
    stage ('Deploy') {
        common_stages.wwwFBDeployStage("${FB_CREDENTIALS}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifySlack()
    }
}

#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {
    
    // Load Env Variables
    common_variables.xxyyzzVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Checkout XXYYZZ Repo
    stage ('Checkout') {
        git branch: 'main', credentialsId: '8a3f258f-2372-4519-a069-4733a16c672e', url: 'https://github.com/swerenfl/www-xxyyzz'
    }

    // Deploy XXYYZZ to Firebase
    stage ('Deploy') {
        common_stages.wwwFBDeployStage("${FB_CREDENTIALS}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifySlack()
    }
}

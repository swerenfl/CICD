#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {
    
    // Load Env Variables
    common_variables.lhVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
        common_stages.actSA("${G_KEY}")
    }

    // Checkout LH Repo
    stage ('Checkout') {
        git credentialsId: '46384ba0-4e05-4e9b-aa38-97b82212c811', url: 'https://github.com/swerenfl/www-lasthope'
    }

    // Deploy LH to the bucket (rsync)
    stage ('Deploy') {
        common_stages.wwwDeployStage("${G_BUCKET_URL}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifySlack()
    }
}

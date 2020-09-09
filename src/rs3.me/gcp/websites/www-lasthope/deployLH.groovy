#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Checkout LH Repo
    stage ('Checkout') {
        git credentialsId: '46384ba0-4e05-4e9b-aa38-97b82212c811', url: 'https://github.com/swerenfl/www-lasthope'
    }
    
    // Deploy LH to the bucket
    stage ('Deploy') {
        googleStorageUpload bucket: 'gs://lasthopeguild.com', credentialsId: 'lasthope-www-2020-09-08', pattern: '**/', sharedPublicly: true
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
    }
}

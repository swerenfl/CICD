#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    mc_variables.lhVariables()

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Checkout LH Repo
    stage ('Checkout') {
        git credentialsId: '46384ba0-4e05-4e9b-aa38-97b82212c811', url: 'https://github.com/swerenfl/www-lasthope'
    }

    // Deploy LH to the bucket (delete then load)
    stage ('Deploy') {
        withCredentials([file(credentialsId: '11a6106f-2563-461f-8f47-58c722f26025', variable: 'GC_KEY')]) {
            sh("gcloud auth activate-service-account --key-file=${GC_KEY}")
            sh("gsutil rm ${G_BUCKET_URL}/**")
            sh("gsutil -m rsync -x '.git' -r \${WORKSPACE} ${G_BUCKET_URL}")
        }
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifySlack()
        common_stages.notifyEmail()
    }
}

#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    mc_variables.envVariables()

    stage ('Preflight') {
        common_stages.preflight()
    }

    // Check Status and Return
    stage ('Check') {
        try {
            isOffline = mc_helpers.checkUp("${G_ZONE}")
            echo "The current status of the server is: ${isOffline}"
            if (isOffline == 'RUNNING' || isOffline == 'REPAIRING') { // Green
                slackColor = '#7ed321'
                discordColor = "SUCCESS"
            }
            else if (isOffline == 'PROVISIONING' || isOffline == 'STAGING') { // Yellow
                slackColor = '#ffd806'
                discordColor = "UNSTABLE"
            }
            else { // Red
                slackColor = '#ff3366'
                discordColor = "FAILURE"
            }
            slackSend channel: "${SLACK_NOTIFY_CHANNEL}", color: "${slackColor}", message: "The status of the server is ${isOffline}"
            discordSend description: "The status of the server is ${isOffline}", footer: '', image: '', link: "${env.BUILD_URL}", result: "${discordColor}", thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${DISCORD_WEBHOOK}"
        }
        catch (err) {
            def failureMessage = 'While executing the online check something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }
    }
}

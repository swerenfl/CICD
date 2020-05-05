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
            if (isOffline == 'RUNNING' || isOffline == 'REPAIRING') {
                slackColor = '#7ed321' // Green
            }
            else if (isOffline == 'PROVISIONING' || isOffline == 'STAGING') {
                slackColor = '#ffd806' // Yellow
            }
            else {
                slackColor = '#ff3366' // Red
            }
            slackSend channel: "${SLACK_NOTIFY_CHANNEL}", color: "${slackColor}", message: "The status of the server is ${isOffline}"
        }
        catch (err) {
            def failureMessage = 'While executing the online check something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }
    }
}

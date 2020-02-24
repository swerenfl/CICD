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
    def gZone = 'us-central1-f'
    def emailRecp = 'richard.staehler@gmail.com'

    // Preflight Stage
    stg_common.preflight()

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        mc_helpers.stopMinecraft(gProject, gZone)
    }

    // Verify Termination
    stage ('Verify') {
        try {
            // Assign a variable to whatever the status of the compute instance is
            def checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
            def onlineCheck = checkStatus.trim()
            echo "The value retrieved is: ${onlineCheck}"
            
            if (onlineCheck == "TERMINATED") {
                echo "Your server is indeed terminated."
            }
            else {
                throw new Exception("Your server is not in a TERMINATED state. Check on your server!")
            }
        }
        catch (Exception e) {
            def failureMessage = "${e}"
            echo "${failureMessage}" + ": " + Exception
            currentBuild.result = 'FAILURE'
            throw err
        }
    }

    // Notify users of the build using the emailext plugin.
    stg_common.notify(emailRecp)

}
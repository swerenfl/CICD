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
    stage ('Preflight') {
        stg_common.preflight()
    }

    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        try {
            mc_helpers.checkUp()
            
            if (onlineCheck == "TERMINATED") {
                echo "Nothing to do here."
            }
            else {
                mc_helpers.stopMinecraft(gProject, gZone)
            }
        }
        catch (err) {
            def failureMessage = 'While stopping the server something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }

    // Verify Termination
    stage ('Verify') {
        try {
            mc_helpers.checkUp()
            
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
    stage ('Notify') {
        stg_common.notifyEmail(emailRecp)
    }

}
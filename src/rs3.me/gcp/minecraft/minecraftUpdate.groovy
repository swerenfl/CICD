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
    def gInstance = 'minecraft-project-2019-11-03'
    def gZone = 'us-central1-f'
    def gServiceAcct = 'jenkins'
    def emailRecp = 'richard.staehler@gmail.com'

    def manifestURL = 'https://launchermeta.mojang.com/mc/game/version_manifest.json'
    def firstURLClean = 'null'
    def latestVersionClean = 'null'

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }

    // Is server online or offline?
    stage ('Online Check') {
        try {
            // Assign a variable to whatever the status of the compute instance is
            def checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
            def onlineCheck = checkStatus.trim()
            echo "The value retrieved is: ${onlineCheck}"
            
            // If it's running we can proceed
            if (onlineCheck == "RUNNING" && ) {
                echo "Your server is running and can proceed with the update."
            }
            // If it's any other status, just run the startMCS method which has a bunch of logic.
            else {
                common_stages.startMCS(gInstance, gZone, gServiceAcct, gProject)
            }
        }
        catch (Exception e) {
            def failureMessage = "${e}"
            echo "${failureMessage}" + ": " + Exception
            currentBuild.result = 'FAILURE'
            throw err
        }
         catch (err) {
            def failureMessage = 'While downloading something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }

    // Start Minecraft Stage
    stage ('Version Check') {
        try {
            def latestVersion = sh returnStdout: true, script: """curl -sSL '${manifestURL}' | jq -r '.latest.release'"""
            latestVersionClean = latestVersion.trim()
            echo "The current latest version is: ${latestVersionClean}."

            def firstURL = sh returnStdout: true, script: """curl -sSL '${manifestURL}' | jq -r '.versions[] | select( .id == ("${latestVersionClean}"))' | jq -r '.url'"""
            firstURLClean = firstURL.trim()
            echo "The current URL for the latest version is: ${firstURLClean}."

            def secondURL = sh returnStdout: true, script: """curl -sSL '${firstURLClean}' | jq -r '.downloads.server.url'"""
            secondURLClean = secondURL.trim()
            echo "The download link is: ${secondURLClean}."
        }
        catch (err) {
            def failureMessage = 'While downloading something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }

    // Download the latest jar
    stage ('Prep') {
        // Kill the Java process
        try {
            def javaProc = mc_helpers.countJava("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            def javaProcClean = javaProc.trim()
            int javaInt = javaProcClean.toInteger()
            echo "The amount of Java processes open is ${javaInt}"

            if (javaInt > 0) {
                mc_helpers.killJava("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${latestVersionClean}")
            }
            else {
                echo "No Java processes are running. Skipping"
            }
        }
        catch (err) {
            def failureMessage = 'While killing Java something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            throw err
        }
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail(emailRecp)
    }

}

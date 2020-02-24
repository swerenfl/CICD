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
    stg_common.preflight()

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
            int javaProc = mc_helpers.countJava("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            echo "The amount of Java processes open is ${javaProc}"

            if (javaProc > 1) {
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
}

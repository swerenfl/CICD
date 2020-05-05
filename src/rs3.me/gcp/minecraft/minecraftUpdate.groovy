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

    // Set Build Variables
    def firstURLClean = 'null'
    def secondURLClean = 'null'
    def latestVersionClean = 'null'
    def installedVersionClean = 'null'
    def isOffline = 'null'

    // Preflight Stage
    stage ('Preflight') {
        common_stages.startSlack()
        common_stages.startDiscord()
        common_stages.preflight()
    }

    // Is server online or offline?
    stage ('Online Check') {
        try {
            isOffline = mc_helpers.checkUp("${G_ZONE}")
            if (isOffline == "RUNNING") { // If running, then make sure the drive is mounted
                def mountProc = mc_helpers.checkMounted("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
                def mountProcClean = mountProc.trim()
                int mountInt = mountProcClean.toInteger()
                echo "The amount of minecraft drives mounted is: ${mountInt}"

                if (mountInt > 0) { // If running, and drive is mounted, we're good 
                    echo "Your server is running and can proceed with the update!"
                }
                else { // If running, and the drive is not mounted, then run the startup sequence
                    common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
                }
            }
            else { // Run the startup sequence because the server is not running
                common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
            }
        }
         catch (err) {
            def failureMessage = 'While executing the online check something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }
    }

    // Discover the latest version of Minecraft
    stage ('Version Check') {
        try {
            // What is the latest version?
            latestVersionClean = sh(returnStdout: true, script: """curl -sSL '${MC_MANIFEST_URL}' | jq -r '.latest.release'""").trim()
            echo "The current latest version is: ${latestVersionClean}."

            // What version do we have installed?
            def installedVersion = mc_helpers.versionCk("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
            installedVersionClean = installedVersion.trim()
            echo "The version we have installed is ${installedVersionClean}"

            // Convert the versions into ints for comparison
            int installedVersionInt = installedVersionClean.replace(".", "").toInteger()
            int latestVersionInt = latestVersionClean.replace(".", "").toInteger()

            // Compare the versions
            if (latestVersionInt == installedVersionInt) {
                echo "Installed version " + installedVersionInt + " is equal to " + latestVersionInt + "."
                currentBuild.result = 'SUCCESS'
                return
            }
            else {
                echo "Installed version " + installedVersionInt + " is less than " + latestVersionInt + ". We need to upgrade!"
            }

            // Parse the URL associated with the latest version
            firstURLClean = sh(returnStdout: true, script: """curl -sSL '${MC_MANIFEST_URL}' | jq -r '.versions[] | select( .id == ("${latestVersionClean}"))' | jq -r '.url'""").trim
            echo "The current URL for the latest version is: ${firstURLClean}."

            // Obtain the download link from the URL
            secondURLClean = sh(returnStdout: true, script: """curl -sSL '${firstURLClean}' | jq -r '.downloads.server.url'""").trim()
            echo "The download link is: ${secondURLClean}."
        }
        catch (err) {
            def failureMessage = 'While version checking something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }
    }

    // If versions match then exit the pipeline
    if (currentBuild.result == 'SUCCESS') {
        if (isOffline == "RUNNING") {
            common_helpers.noUpdates()
            return
        }
        else {
            common_stages.stopMCS("${G_ZONE}", "${G_PROJECT}")
            common_stages.verifyMCSOffline("${G_ZONE}")
            common_helpers.noUpdates()
            return
        }
    }

    // Versions didn't match. Time to prep the intance for upgrade
    stage ('Prep') {
        try { // Kill the Java process
            def javaProc = mc_helpers.countJava("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
            def javaProcClean = javaProc.trim()
            int javaInt = javaProcClean.toInteger()
            echo "The amount of Java processes open is ${javaInt}"

            if (javaInt > 0) {
                mc_helpers.killJava("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}", "${latestVersionClean}")
            }
            else {
                echo "No Java processes are running. Skipping"
            }
        }
        catch (err) {
            def failureMessage = 'While killing Java something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }

        try { // Backup the drives
            echo "Backup the old server.jar"
            mc_helpers.backupMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
        }
        catch (err) {
            def failureMessage = 'While backing up something went wrong. Review logs for further details'
            common_helpers.catchMe("${failureMessage}", err)
        }
    }

    // Fetch the file and put it in its place
    stage ('Upgrade') {
        try {
            echo "Get the latest server.jar"
            mc_helpers.getLatest("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}", "${secondURLClean}")
        }
        catch (err) {
            common_helpers.catchMe("${failureMessage}", err)
        }
    }

    // Start Minecraft after the upgrade
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail("${EMAIL_RECP}")
        common_stages.notifyDiscord()
        common_stages.notifySlack()
    }
}
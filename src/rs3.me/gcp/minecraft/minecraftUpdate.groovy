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
    def slackNotifyChannel = '#08-gaming'

    def manifestURL = 'https://launchermeta.mojang.com/mc/game/version_manifest.json'
    def firstURLClean = 'null'
    def latestVersionClean = 'null'
    def installedVersionClean = 'null'

    // What is the latest version?
    def latestVersion = sh returnStdout: true, script: """curl -sSL '${manifestURL}' | jq -r '.latest.release'"""
    latestVersionClean = latestVersion.trim()
    echo "The current latest version is: ${latestVersionClean}."

    // What version do we have installed?
    def installedVersion = mc_helpers.versionCk("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
    installedVersionClean = installedVersion.trim()
    echo "The version we have installed is ${installedVersionClean}"

    // Convert the versions into ints for comparison
    int installedVersionInt = installedVersionClean.replace(".", "").toInteger()
    int latestVersionInt = latestVersionClean.replace(".", "").toInteger()

    if (latestVersionInt <= installedVersionInt) {
        currentBuild.result = 'SUCCESS'
        return
    }

    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight("${slackNotifyChannel}")
    }

    // Is server online or offline?
    stage ('Online Check') {
        try {
            def isOffline = mc_helpers.checkUp("${gZone}")

            if (isOffline == "RUNNING") { // If running, then make sure the drive is mounted
                def mountProc = mc_helpers.checkMounted("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                def mountProcClean = mountProc.trim()
                int mountInt = mountProcClean.toInteger()
                echo "The amount of minecraft drives mounted is ${mountInt}"

                if (mountInt > 0) { // If running, and drive is mounted, we're good 
                    echo "Your server is running and can proceed with the update."
                }
                else { // If running, and the drive is not mounted, then run the startup sequence
                    common_stages.startMCS("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${slackNotifyChannel}")
                }
            }
            else { // Run the startup sequence because the server is not running
                common_stages.startMCS("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${slackNotifyChannel}")
            }
        }
         catch (err) {
            def failureMessage = 'While executing the online check something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Discover the latest version of Minecraft
    stage ('Version Check') {
        try {
            // Parse the URL associated with the latest version
            def firstURL = sh returnStdout: true, script: """curl -sSL '${manifestURL}' | jq -r '.versions[] | select( .id == ("${latestVersionClean}"))' | jq -r '.url'"""
            firstURLClean = firstURL.trim()
            echo "The current URL for the latest version is: ${firstURLClean}."

            // Obtain the download link from the URL
            def secondURL = sh returnStdout: true, script: """curl -sSL '${firstURLClean}' | jq -r '.downloads.server.url'"""
            secondURLClean = secondURL.trim()
            echo "The download link is: ${secondURLClean}."
        }
        catch (err) {
            def failureMessage = 'While downloading something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Prep the intance
    stage ('Prep') {
        try { // Kill the Java process
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
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }

        try { // Backup the drives
            echo "Backup the old server.jar"
            mc_helpers.backupMCS("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
        }
        catch (err) {
            def failureMessage = 'While backing up something went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Fetch the file and put it in its place
    stage ('Upgrade') {
        try {
            echo "Get the latest server.jar"
            mc_helpers.getLatest("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${secondURLClean}")
        }
        catch (err) {
            def failureMessage = 'While upgrading went wrong. Review logs for further details'
            echo "${failureMessage}" + ": " + err
            currentBuild.result = 'FAILURE'
            common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
            throw err
        }
    }

    // Start Minecraft after the upgrade
    stage ('Start Minecraft') {
        common_stages.startMCS("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}", "${slackNotifyChannel}")
    }

    // Notify users of the build using the emailext plugin.
    stage ('Notify') {
        common_stages.notifyEmail("${emailRecp}", "${slackNotifyChannel}")
        common_stages.notifySlack("${slackNotifyChannel}")
    }

}

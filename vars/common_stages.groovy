#!groovy

/* =============================================== */
/*                PREFLIGHT STAGES                 */
/* =============================================== */ 
// Preflight
def preflight() {
    try {
        echo "Set limit to Discard old builds. Keep last 10 builds. Further, disallow concurrent builds."
        properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
    }
    catch (err) {
        def failureMessage = 'While cleaning up the workspace, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Activate Service Account
def actSA(gKey) {
    try {
        echo "Activating Service Account"
        withCredentials([file(credentialsId: $gKey, variable: 'GC_KEY')]) {
            sh "gcloud auth activate-service-account --key-file=$GC_KEY"
        }
    }
    catch (err) {
        def failureMessage = 'Could not activate service account. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}


/* =============================================== */
/*                  START STAGES                   */
/* =============================================== */ 
// Start via Slack
def startSlack() {
    try {
        echo "Notify Slack that the build is starting"
        common_helpers.notifySlackStart()
    }
    catch (err) {
        def failureMessage = 'While trying to notify Slack at the start of the build, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Start via Discord
def startDiscord() {
    try {
        echo "Notify Discord that the build is starting"
        common_helpers.notifyDiscordStart()
    }
    catch (err) {
        def failureMessage = 'While trying to notify Discord at the start of the build, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}


/* =============================================== */
/*                 NOTIFY STAGES                   */
/* =============================================== */ 
// Notify status of pipeline via email 
def notifyEmail() {
    try {
        echo "Notify successful completion of the pipeline to email"
        common_helpers.notifyEmailSuccess()
    }
    catch (err) {
        def failureMessage = 'While trying to notify Email, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Notify status of pipeline via Slack 
def notifySlack() {
    try {
        echo "Notify successful completion of the pipeline to Slack"
        common_helpers.notifySlackSuccess()
    }
    catch (err) {
        def failureMessage = 'While trying to notify Slack, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Notify status of pipeline via Discord
def notifyDiscord() {
    try {
        echo "Notify successful completion of the pipeline to Discord"
        common_helpers.notifyDiscordSuccess()
    }
    catch (err) {
        def failureMessage = 'While trying to notify Discord, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}


/* =============================================== */
/*                GENERAL STAGES                   */
/* =============================================== */ 
// Start Minecraft
def startMCS(gInstance, gZone, gServiceAcct, gProject) {
    try {
        // Assign a variable to whatever the status of the compute instance is
        def onlineCheck = mc_helpers.checkUp("${gZone}")
        echo "The value retrieved is: ${onlineCheck}"
        
        // If the compute instance is offine, then start it and then run the startup sequence
        if (onlineCheck == "TERMINATED") {
            echo "The status of the server is: ${onlineCheck}"
            sh "gcloud compute instances start ${gProject} --zone=${gZone}"
            sh "sleep 15"

            // Check if online for sure.
            onlineCheck = mc_helpers.checkUp("${gZone}")
            echo "The value retrieved is: ${onlineCheck}"

            // If still not started, wait another 60 secs then start again
            if (onlineCheck == "PROVISIONING" || onlineCheck == "STARTING") {
                sh "sleep 60"

                // Check again just to make sure
                onlineCheck = mc_helpers.checkUp("${gZone}")
                echo "The value retrieved is: ${onlineCheck}"

                // If still starting, exit as it's been too long, else if instance is running then run the startup sequence
                if (onlineCheck == "PROVISIONING" || onlineCheck == "STARTING") {
                    throw new Exception("Your server has been in a provisioning or starting stage for too long. Check on your server!")
                }
                else if (onlineCheck == "RUNNING") {
                    mc_helpers.startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }
                else {
                    throw new Exception("Unknown error. Check on your server!")
                }
            }

            // If it has started then run the startup sequence
            else if (onlineCheck == "RUNNING") {
                mc_helpers.startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            }

            // If it's in some other state, then throw an exception
            else {
                throw new Exception("Unknown error. Check on your server!")
            }
        }

        // If compute instance is RUNNING
        else if (onlineCheck == "RUNNING") {
            def mcsRun = mc_helpers.mcsRunning("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            def mcsRunClean = mcsRun.trim()
            echo "Is the minecraft screen running? ${mcsRunClean}"

            // If the instance is RUNNING, and the Minecraft Screen is running
            if (mcsRunClean == "yes") {
                echo "The minecraft server is already running."
            }

            // If the instance is RUNNING, and the Minecraft Screen is NOT running, then we have to see if we actually mounted the drive
            else {
                echo "The status of the server is: ${onlineCheck}"
                def isMounted = mc_helpers.checkMounted("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                def isMountedClean = isMounted.trim()
                echo "The value of mounted is: ${isMountedClean}"

                // If the drive is mounted, then run the commands to start the screen without mounting
                if (isMountedClean == "1") {
                    mc_helpers.startMinecraftNoMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }

                // If the drive is NOT mounted, then run the commands to mount and start the screen
                else if (isMountedClean == "0") {
                    mc_helpers.startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }

                // If anything else, then throw an error
                else {
                    throw new Exception("Unknown error. Try again later!")
                }
            }
        }

        // If the instance is in a stopping stage, just throw an exception as there is nothing we can do 
        else if (onlineCheck == "STOPPING") {
            echo "The status of the server is: ${onlineCheck}"
            throw new Exception("Your server is in the middle of stopping. Try again later!")
        }

        // If the instance is in any other state except for the ones aforementioned, then throw a general error
        else {
            throw new Exception("Unknown error. Try again later!")
        }
    }
    catch (err) {
        def failureMessage = 'While connecting and starting, something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Stop Minecraft
def stopMCS(gZone, gProject) {
    try {
        def isOffline = mc_helpers.checkUp("${gZone}")
        if (isOffline == "TERMINATED") {
            echo "Nothing to do here."
        }
        else {
            mc_helpers.stopMinecraft("${gProject}", "${gZone}")
        }
    }
    catch (err) {
        def failureMessage = 'While stopping the server something went wrong. Review logs for further details'
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Verify Minecraft is offline
def verifyMCSOffline(gZone) {
    try {
        def isOffline = mc_helpers.checkUp("${gZone}")
        if (isOffline == "TERMINATED") {
            echo "Your server is indeed terminated."
        }
        else {
            throw new Exception("Your server is not in a TERMINATED state. Check on your server!")
        }
    }
    catch (Exception err) {
        def failureMessage = "${err}"
        common_helpers.catchMe("${failureMessage}", err)
    }
}


/* =============================================== */
/*                   WWW STAGES                    */
/* =============================================== */
// Deploy with a GCS Bucket setup
def wwwDeployStage(gBucketURL) {
    try {
        www_helpers.wwwDeploy("${gBucketURL}")
    }
    catch (err) {
        def failureMessage = "While deploying code to ${gBucketURL} something went wrong. Review logs for further details"
        common_helpers.catchMe("${failureMessage}", err)
    }
}

// Deploy with a Firebase setup
def wwwFBDeployStage(fbCredentials) {
    try {
        www_helpers.fbDeploy("${fbCredentials}")
    }
    catch (err) {
        def failureMessage = "While deploying code to Firebase something went wrong. Review logs for further details"
        common_helpers.catchMeNoDiscord("${failureMessage}", err)
    }
}

return this

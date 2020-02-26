#!groovy

// preflight stage -- common across pipelines
def preflight(slackNotifyChannel) {
    try {
        echo "Set limit to Discard old builds. Keep last 10 builds. Further, disallow concurrent builds."
        properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
    }
    catch (err) {
        def failureMessage = 'While cleaning up the workspace, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
        throw err
    }
}

// start stage -- Slack -- common across pipelines
def startSlack(slackNotifyChannel) {
    try {
        echo "Notify Slack"
        common_helpers.notifySlackStart("${slackNotifyChannel}")
    }
    catch (err) {
        def failureMessage = 'While trying to notify Slack at the start of the build, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
        throw err
    }
}

// notify stage -- common across pipelines
def notifyEmail(emailRecp, slackNotifyChannel) {
    try {
        echo "Send email"
        emailext attachLog: true, body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: "${emailRecp}"
    }
    catch (err) {
        def failureMessage = 'While trying to notify Email, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
        throw err
    }
}

// notify stage -- Slack -- common across pipelines
def notifySlack(slackNotifyChannel) {
    try {
        echo "Notify Slack"
        common_helpers.notifySlackSuccess("${slackNotifyChannel}")
    }
    catch (err) {
        def failureMessage = 'While trying to notify Slack, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
        throw err
    }
}

// start minecraft -- common, needs 5 inputs.
def startMCS(gInstance, gZone, gServiceAcct, gProject, slackNotifyChannel) {
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
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        common_helpers.notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
        throw err
    }
}

return this
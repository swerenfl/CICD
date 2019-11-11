#!groovy

/* ===============================================


                    PIPELINE


=============================================== */

// Start Pipeline
echo "Pipeline Start"

// Set Props Variables
def props = [:]
    props['version'] = env.BUILD_NUMBER
    props['emails'] = 'richard.staehler@gmail.com'
    props['jobPath'] = env.JOB_NAME.split('/')
    props['repo'] = props['jobPath'][-2]
    props['branch'] = props['jobPath'][-1]


// Set Build Variables
def gProject = 'mc-server'
def gInstance = 'minecraft-project-2019-11-03'
def gZone = 'us-central1-f'
def gServiceAcct = 'jenkins'

stage ('Cleanup') {
    try {
        echo "Set limit to Discard old builds. Keep last 10 builds. Further, disallow concurrent builds."
        properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '10', daysToKeepStr: '', numToKeepStr: '10')), disableConcurrentBuilds()])
    }
    catch (err) {
        def failureMessage = 'While cleaning up the workspace, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        throw err
    }
}
stage ('Start Minecraft') {
    try {
        // Assign a variable called isOnline to whatever the status of the compute instance is
        def checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
        def onlineCheck = checkStatus.trim()
        echo "The value retrieved is: ${onlineCheck}"
        
        // If the compute instance is offine, then start it and then start Minecraft
        if (onlineCheck == "TERMINATED") {
            echo "The status of the server is: ${onlineCheck}"

            // Turn it on
            sh "gcloud compute instances start ${gProject} --zone=${gZone}"

            // Wait 15 seconds to make sure it's online
            sh "sleep 15"

            // Check if online for sure. If still not started, wait another 60 secs then start again
            checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
            onlineCheck = checkStatus.trim()
            echo "The value retrieved is: ${onlineCheck}"
            if (onlineCheck == "PROVISIONING" || onlineCheck == "STARTING") {
                sh "sleep 60"

                // Check again just to make sure and if still starting, then exit if stalled
                checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
                onlineCheck = checkStatus.trim()
                echo "The value retrieved is: ${onlineCheck}"
                if (onlineCheck == "PROVISIONING" || onlineCheck == "STARTING") {
                    throw new Exception("Your server has been in a provisioning or starting stage for too long. Check on your server!")
                }
                else {
                    startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }
            }
            else {
                startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            }
        }
        else if (onlineCheck == "RUNNING") {
            def mcsRun = mcsRunning("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
            def mcsRunClean = mcsRun.trim()
            echo "Is the minecraft screen running? ${mcsRunClean}"
            if (mcsRunClean == "yes") {
                echo "The minecraft server is already running."
            }
            else {
                echo "The status of the server is: ${onlineCheck}"
                def isMounted = checkMounted("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                def isMountedClean = isMounted.trim()
                echo "The value of mounted is: ${isMountedClean}"
                if (isMountedClean == "1") {
                    startMinecraftNoMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }
                else if (isMounted == "0") {
                    startMinecraftMount("${gInstance}", "${gZone}", "${gServiceAcct}", "${gProject}")
                }
                else {
                    throw new Exception("Unknown error. Try again later!")
                }
            }
        }
        else if (onlineCheck == "STOPPING") {
            echo "The status of the server is: ${onlineCheck}"
            throw new Exception("Your server is in the middle of stopping. Try again later!")
        }
        else {
            throw new Exception("Unknown error. Try again later!")
        }
    }
    catch (err) {
        def failureMessage = 'While connecting and starting, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        throw err
    }
}
stage ('Notify') {
    try {
        echo "Send email"
        emailext attachLog: true, body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: 'richard.staehler@gmail.com'
    }
    catch (err) {
        def failureMessage = 'While executing, something went wrong. Review logs for further details'
        echo "${failureMessage}" + ": " + err
        currentBuild.result = 'FAILURE'
        throw err
    }
}

/* ===============================================


                    HELPERS


=============================================== */
def mcsRunning (gInstance, gZone, gServiceAcct, gProject) {
    def runMCS = sh returnStdout:true, script: """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            if sudo screen -list | grep -q "mcs"; then echo "yes"; else echo "no"; fi' """
    echo "The value of runMCS: ${runMCS}"
    return runMCS
}


def checkMounted(gInstance, gZone, gServiceAcct, gProject) {
    def checkIfExists = sh returnStdout:true, script: """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash 
            find /home/minecraft/server.jar -maxdepth 1 -type f | wc -l' """
    echo "The value is: ${checkIfExists}"
    return checkIfExists
}

def startMinecraftMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            sudo mount /dev/disk/by-id/google-minecraft-disk /home/minecraft;
            cd /home/minecraft && sudo screen -d -m -S mcs java -Xms1G -Xmx3G -d64 -jar server.jar nogui'
    """
}

def startMinecraftNoMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            cd /home/minecraft && sudo screen -d -m -S mcs java -Xms1G -Xmx3G -d64 -jar server.jar nogui'
    """
}
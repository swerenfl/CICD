#!groovy

// mcsRunning -- expecting 4 inputs
def mcsRunning (gInstance, gZone, gServiceAcct, gProject) {
    def runMCS = sh returnStdout:true, script: """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            if sudo screen -list | grep -q "mcs"; then echo "yes"; else echo "no"; fi' """
    echo "The value of runMCS: ${runMCS}"
    return runMCS
}

// checkMounted -- expecting 4 inputs
def checkMounted(gInstance, gZone, gServiceAcct, gProject) {
    def checkIfExists = sh returnStdout:true, script: """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash 
            find /home/minecraft/server.jar -maxdepth 1 -type f | wc -l' """
    echo "The value is: ${checkIfExists}"
    return checkIfExists
}

// startMinecraftMount -- expecting 4 inputs
def startMinecraftMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            sudo mount /dev/disk/by-id/google-minecraft-disk /home/minecraft;
            cd /home/minecraft && sudo screen -d -m -S mcs java -Xms1G -Xmx3G -d64 -jar server.jar nogui'
    """
}

// startMinecraftNoMount -- expecting 4 inputs
def startMinecraftNoMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" -- '#!/bin/bash
            cd /home/minecraft && sudo screen -d -m -S mcs java -Xms1G -Xmx3G -d64 -jar server.jar nogui'
    """
}

// stopMinecraftServer -- expecting 1 input
def stopMinecraft(gProject, gZone) {
    def checkStatus = sh returnStdout: true, script: 'gcloud compute instances list --filter="${gZone}" --format="value(status.scope())"'
    def onlineCheck = checkStatus.trim()
    echo "The value retrieved is: ${onlineCheck}"
            
    if (onlineCheck == "TERMINATED") {
        echo "Nothing to do here."
    }
    else {
        sh """
            gcloud compute instances stop "${gProject}" --zone "${gZone}"
        """
    }
}

def countJava(gInstance, gZone, gServiceAcct, gProject) {
    int returnJava = sh returnStdout: true, script: """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" \
        --command='ps -ef | grep java | wc -l' """
    echo "Java is open this many times: " + returnJava
    return returnJava
}

// killJava -- expecting 4 inputs
def killJava(gInstance, gZone, gServiceAcct, gProject, latestVersionClean) {
    sh """
        gcloud compute ssh --project "${gInstance}" --zone "${gZone}" "${gServiceAcct}"@"${gProject}" \
        --command='sudo screen -S mcs -p 0 -X stuff "say ATTENTION: Server will shutdown in 30 seconds to update to version ${latestVersionClean}.\015"; sleep 30; sudo pkill java'
    """
}
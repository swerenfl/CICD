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
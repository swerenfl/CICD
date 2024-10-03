#!groovy

// mcsRunning -- expecting 4 inputs
def mcsRunning (gInstance, gZone, gServiceAcct, gProject) {
    def runMCS = sh returnStdout:true, script: """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='if sudo screen -list | grep -q "mcs"; then echo "yes"; else echo "no"; fi' """
    echo "The value of runMCS: ${runMCS}"
    return runMCS
}

// checkMounted -- expecting 4 inputs
def checkMounted(gInstance, gZone, gServiceAcct, gProject) {
    def checkIfExists = sh returnStdout:true, script: """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='find /home/minecraft/server.jar -maxdepth 1 -type f | wc -l' """
    echo "The value is: ${checkIfExists}"
    return checkIfExists
}

// startMinecraftMount -- expecting 4 inputs
def startMinecraftMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='sudo mount /dev/disk/by-id/google-mc-attached-24 /home/minecraft; \
        cd /home/minecraft && sudo screen -d -m -S mcs java -Xms4G -Xmx6G -jar server.jar nogui'
    """
}

// startMinecraftNoMount -- expecting 4 inputs
def startMinecraftNoMount(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='cd /home/minecraft && sudo screen -d -m -S mcs java -Xms4G -Xmx6G -jar server.jar nogui'
    """
}

// backup MCS -- expecting 4 inputs
def backupMCS(gInstance, gZone, gServiceAcct, gProject) {
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='cd /home && sudo rm -rf minecraft.bak && sudo cp -avr /home/minecraft/ /home/minecraft.bak/ && \
        sudo cp /home/minecraft/server.jar /home/minecraft/server.jar.bak && \
        sudo rm -rf /home/minecraft/server.jar'
    """
}

// fetch latest -- expecting 5 inputs
def getLatest(gInstance, gZone, gServiceAcct, gProject, secondURLClean) {
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='cd /home/minecraft && \
        sudo wget ${secondURLClean} && \
        sudo chmod 644 /home/minecraft/server.jar'
    """
}

def versionCk(gInstance, gZone, gServiceAcct, gProject) {
    def versionCheck = sh returnStdout: true, script: """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='cd /home/minecraft && \
        sudo unzip -p server.jar version.json | jq -r .name' """
        echo "The version we have installed is: ${versionCheck}"
    return versionCheck
}

// checkUp -- checks the status of the server 
def checkUp(gZone) {
    def checkStatus = sh returnStdout: true, script: """gcloud compute instances list --filter=${gZone} --format='value(status.scope())' """
    def upCheck = checkStatus.trim()
    echo "The value retrieved from checkUp method is: ${upCheck}"
    return upCheck
}

// stopMinecraftServer -- expecting 2 inputs
def stopMinecraft(gZone, gProject) {
    sh """
        gcloud compute instances stop ${gProject} --zone ${gZone}
    """
}

// countJava -- count the amount of Java Processes running -- expecting 4 inputs
def countJava(gInstance, gZone, gServiceAcct, gProject) {
    def returnJava = sh returnStdout: true, script: """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='ps -e --no-headers | grep java |  wc -l' """
    echo "Java is open this many times: ${returnJava}"
    return returnJava
}

// killJava -- expecting 5 inputs
def killJava(gInstance, gZone, gServiceAcct, gProject, latestVersionClean) {
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='sudo screen -S mcs -p 0 -X stuff "say ATTENTION: Server will shutdown in 30 seconds to update to version ${latestVersionClean}.\015"; sleep 30; sudo pkill java'
    """
}

// generate new world -- expecting 5 inputs
def newWorld(gInstance, gZone, gServiceAcct, gProject, newName) {
    sleep 15
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command="cd /home/minecraft; sudo sed -i 's/^level-name.*/level-name=${newName}/' server.properties; cat server.properties"
    """
}

// generate new name for new world -- expecting 0 inputs
def newWorldName() {
    def now = new Date()
    def nowFormatted = now.format('yyyy-MM-dd-HHmmss')
    def newWorldDateName = nowFormatted + "-world"
    echo "The world name will be: ${newWorldDateName}."
    return newWorldDateName
}

// sendMessage -- expecting 4 inputs
def sendMessage(gZone, gProject, gInstance, gServiceAcct) {
    def buildCause = currentBuild.getBuildCauses()[0].shortDescription
    echo "Current build was caused by: ${buildCause}\n"
    if (buildCause == "Started by timer") { // Cron
        message = "ATTENTION: Server will shutdown within the next minute for the evening. Thank you for playing today!"
    }
    else { // User or other means
        message = "ATTENTION: Server will shutdown within the next minute for maintenance. See you soon!"
    }
    sh """
        gcloud compute ssh --project ${gInstance} --zone ${gZone} ${gServiceAcct}@${gProject} \
        --command='sudo screen -S mcs -p 0 -X stuff "say ${message}\015"; sleep 10'
    """
}

return this

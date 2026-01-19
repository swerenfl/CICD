#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {

    // Load Env Variables
    common_variables.envVariables()
    def startMode = "0"

    // Preflight Stage
    stage ('Preflight') {
        properties([parameters([choice(name: 'START_MODE', choices: "0\n1", description: '0 = server.jar, 1 = fabric.jar')])])
        startMode = common_stages.selectStartMode(params?.START_MODE)
        common_stages.actSA("${G_KEY}", "${G_INSTANCE}")
        common_stages.preflight()
        common_stages.startSlack()
        common_stages.startDiscord()
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        common_stages.startMCS("${G_INSTANCE}", "${G_ZONE}", "${G_SERV_ACCT}", "${G_PROJECT}", "${startMode}")
    }

    // Notify users that things have finished
    stage ('Notify') {
        common_stages.notifyEmail()
        common_stages.notifyDiscord()
        common_stages.notifySlack()
        build job: 'Minecraft_STATUS'
    }
}

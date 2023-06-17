#!groovy

/* =============================================== */
/*                    PIPELINE                     */
/* =============================================== */ 

// Load Library
@Library('CICD')_

// Start Pipeline
node {
    
    // Preflight Stage
    stage ('Preflight') {
        common_stages.preflight()
    }
    
    // Stop Minecraft Stage
    stage ('Stop Minecraft') {
        build job: 'Minecraft_STOP'
    }

    // Start Minecraft Stage
    stage ('Start Minecraft') {
        build job: 'Minecraft_START'
    }
}

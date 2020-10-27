#!groovy

/* -------------------------------------------------------
                    GLOBAL VARS
------------------------------------------------------- */

def envVariables() {
    env.MC_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#08-gaming"
    env.G_KEY = "48f02094-ee5b-4e1d-a9e1-99566a54da84"
    env.G_PROJECT = "mc-server"
    env.G_INSTANCE = "minecraft-project-2019-11-03"
    env.G_ZONE = "us-central1-f"
    env.G_SERV_ACCT = "jenkins"
}

def lhVariables() {
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#09-websites"
    env.FB_CREDENTIALS = "41aad29c-b842-44dd-a95e-e98a9a74d482"
}

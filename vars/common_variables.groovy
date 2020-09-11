#!groovy

/* -------------------------------------------------------
                    GLOBAL VARS
------------------------------------------------------- */

def envVariables() {
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.G_PROJECT = "mc-server"
    env.G_INSTANCE = "minecraft-project-2019-11-03"
    env.G_ZONE = "us-central1-f"
    env.G_SERV_ACCT = "jenkins"
    env.SLACK_NOTIFY_CHANNEL = "#08-gaming"
    env.MC_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
}

def lhVariables() {
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#09-websites"
    env.G_BUCKET_URL = "gs://lasthopeguild.com"
}
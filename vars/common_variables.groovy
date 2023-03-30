#!groovy

/* -------------------------------------------------------
                    GLOBAL VARS
------------------------------------------------------- */

def envVariables() {
    env.MC_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#08-gaming"
    env.G_KEY = "cd38590b-317c-4eb7-a58f-39a52d9234e4"
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

def rsVariables() {
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#09-websites"
    env.FB_CREDENTIALS = "c04f87a4-c027-4404-93db-40435e1f50b1"
}

def xxyyzzVariables() {
    env.GENERAL_MESSAGE = "Job Name: $JOB_NAME | Build Number: $BUILD_NUMBER | URL: $BUILD_URL"
    env.SLACK_NOTIFY_CHANNEL = "#09-websites"
    env.FB_CREDENTIALS = "xxx"
}

def itemChkVariables(itemCheck) {
    env.GENERAL_MESSAGE = "Your item is in stock! Proceed to purchase! The URL is <${itemCheck}|here>."
    env.GENERAL_MESSAGE_UNSTABLE = "Unknown return code. Evaluate the item <${itemCheck}|here>."
    env.SLACK_NOTIFY_CHANNEL = "#07-shopping"
}
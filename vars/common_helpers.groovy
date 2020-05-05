#!groovy

// NOTE: GENERAL_MESSAGE is defined in Manage Jenkins > Configure System > Global Properties
/* -------------------------------------------------------
                    SLACK NOTIFIERS
------------------------------------------------------- */
def notifySlackSuccess(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! ${generalMessage}"
}

def notifySlackUnstable(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! ${generalMessage}"
}

def notifySlackFail(channel = "${SLACK_NOTIFY_CHANNEL}", message, error, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | ${generalMessage}"
}

def notifySlackStart(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#4a90e2', message: "STARTED! ${generalMessage}"
}


// NOTE: GENERAL_MESSAGE is defined in Manage Jenkins > Configure System > Global Properties
/* -------------------------------------------------------
                    DISCORD NOTIFIERS
------------------------------------------------------- */
def notifyDiscordSuccess(discordWebURL = "${DISCORD_WEBHOOK}", generalMessage = "${GENERAL_MESSAGE}") {
    discordSend description: "SUCCESS! ${generalMessage}", footer: '', image: '', link: "${env.BUILD_URL}", result: 'SUCCESS', thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${discordWebURL}"
}

def notifyDiscordUnstable(discordWebURL = "${DISCORD_WEBHOOK}", generalMessage = "${GENERAL_MESSAGE}") {
    discordSend description: "UNSTABLE! ${generalMessage}", footer: '', image: '', link: "${env.BUILD_URL}", result: 'UNSTABLE', thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${discordWebURL}"
}

def notifyDiscordFail(discordWebURL = "${DISCORD_WEBHOOK}", message, error, generalMessage = "${GENERAL_MESSAGE}") {
    discordSend description: "FAILED! Error: ${message} | Reason: ${error} | ${generalMessage}", footer: '', image: '', link: "${env.BUILD_URL}", result: 'FAILURE', thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${discordWebURL}"
}

def notifyDiscordStart(discordWebURL = "${DISCORD_WEBHOOK}", generalMessage = "${GENERAL_MESSAGE}") {
    discordSend description: "STARTED! ${generalMessage}", footer: '', image: '', link: "${env.BUILD_URL}", result: 'ABORTED', thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${discordWebURL}"
}


/* -------------------------------------------------------
                    GENERAL CATCH
------------------------------------------------------- */
def catchMe(failureMessage, discordWebURL = "${DISCORD_WEBHOOK}", channel = "${SLACK_NOTIFY_CHANNEL}") {
    echo "${failureMessage}" + ": " + err
    currentBuild.result = 'FAILURE'
    notifySlackFail("${slackNotifyChannel}", "${failureMessage}", err)
    notifyDiscordFail("${discordWebURL}", "${failureMessage}", err)
    throw err
}

return this
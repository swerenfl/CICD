#!groovy

// NOTE: GENERAL_MESSAGE is defined in Manage Jenkins > Configure System > Global Properties
/* -------------------------------------------------------
                    SLACK NOTIFIERS
------------------------------------------------------- */
def notifySlackSuccess(channel, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! ${generalMessage}"
}

def notifySlackUnstable(channel, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! ${generalMessage}"
}

def notifySlackFail(channel, message, error, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | ${generalMessage}"
}

def notifySlackStart(channel, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#4a90e2', message: "STARTED! ${generalMessage}"
}


// NOTE: GENERAL_MESSAGE is defined in Manage Jenkins > Configure System > Global Properties
/* -------------------------------------------------------
                    DISCORD NOTIFIERS
------------------------------------------------------- */
def notifyDiscordSuccess(channel, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! ${generalMessage}"
}

def notifyDiscordUnstable(channel, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! ${generalMessage}"
}

def notifyDiscordFail(channel, message, error, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | ${generalMessage}"
}

def notifyDiscordStart(discordWebURL, generalMessage = "${GENERAL_MESSAGE}") {
    // Reason for ABORTED: ABORTED just means a grey color in the left hand side
    discordSend description: "STARTED! ${generalMessage}", link: 'env.BUILD_URL', result: 'ABORTED', title: 'MC - GCP STARTED', webhookURL: "${discordWebURL}"
}

return this
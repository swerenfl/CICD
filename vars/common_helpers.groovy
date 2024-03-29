#!groovy

/* =============================================== */
/*                 SLACK NOTIFIERS                 */
/* =============================================== */ 
def notifySlackSuccess(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! ${generalMessage}"
}

def notifySlackUnstable(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE_UNSTABLE}") {
    slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! ${generalMessage}"
}

def notifySlackFail(channel = "${SLACK_NOTIFY_CHANNEL}", message, error, generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | ${generalMessage}"
}

def notifySlackStart(channel = "${SLACK_NOTIFY_CHANNEL}", generalMessage = "${GENERAL_MESSAGE}") {
    slackSend channel: "${channel}", color: '#4a90e2', message: "STARTED! ${generalMessage}"
}


/* =============================================== */
/*                 DISCORD NOTIFIERS               */
/* =============================================== */ 
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


/* =============================================== */
/*                 EMAIL NOTIFIERS               */
/* =============================================== */ 
def notifyEmailSuccess(emailRecp = "${EMAIL_RECP}") {
    emailext replyTo: '$DEFAULT_REPLYTO', attachLog: true, body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: "${emailRecp}"
}

def notifyEmailFailure(emailRecp = "${EMAIL_RECP}") {
    emailext replyTo: '$DEFAULT_REPLYTO', attachLog: true, body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: "${emailRecp}"
}


/* =============================================== */
/*                 GENERAL CATCH                   */
/* =============================================== */ 
def catchMe(failureMessage, err, discordWebURL = "${DISCORD_WEBHOOK}", channel = "${SLACK_NOTIFY_CHANNEL}", emailRecp = "${EMAIL_RECP}") {
    echo "${failureMessage}" + ": " + err
    currentBuild.result = 'FAILURE'
    notifySlackFail("${channel}", "${failureMessage}", err)
    notifyDiscordFail("${discordWebURL}", "${failureMessage}", err)
    notifyEmailFailure("${emailRecp}")
    throw err
}

/* =============================================== */
/*            GENERAL CATCH NO DISCORD             */
/* =============================================== */ 
def catchMeNoDiscord(failureMessage, err, channel = "${SLACK_NOTIFY_CHANNEL}", emailRecp = "${EMAIL_RECP}") {
    echo "${failureMessage}" + ": " + err
    currentBuild.result = 'FAILURE'
    notifySlackFail("${channel}", "${failureMessage}", err)
    notifyEmailFailure("${emailRecp}")
    throw err
}

/* =============================================== */
/*     GENERAL CATCH NO SLACK AND NO DISCORD       */
/* =============================================== */ 
def catchMeNoDisOrSlack(failureMessage, err, emailRecp = "${EMAIL_RECP}") {
    echo "${failureMessage}" + ": " + err
    currentBuild.result = 'FAILURE'
    notifyEmailFailure("${emailRecp}")
    throw err
}


/* =============================================== */
/*                   NO UPDATES                    */
/* =============================================== */ 
def noUpdates(extraMessage, discordWebURL = "${DISCORD_WEBHOOK}", channel = "${SLACK_NOTIFY_CHANNEL}", emailRecp = "${EMAIL_RECP}", generalMessage = "${GENERAL_MESSAGE}") {
    emailext replyTo: '$DEFAULT_REPLYTO', attachLog: true, body: "\$DEFAULT_CONTENT\n\nNo updates conducted. Build passed without issue. ${extraMessage}", subject: '$DEFAULT_SUBJECT No Updates', to: "${emailRecp}"
    discordSend description: "NO UPDATES! ${extraMessage} ${generalMessage}", footer: '', image: '', link: "${env.BUILD_URL}", result: 'ABORTED', thumbnail: '', title: "${env.JOB_BASE_NAME}", webhookURL: "${discordWebURL}"
    slackSend channel: "${channel}", color: '#CDCDCD', message: "NO UPDATES! ${extraMessage} ${generalMessage}"
}


/* ================================================ */
/*                    EXTRA NOTES                   */
/* ================================================ */ 
/* GENERAL_MESSAGE, DISCORD_WEBHOOK, and EMAIL_RECP */
/* are defined in Manage Jenkins > Configure System */
/* > Global Properties section since Git is public. */

return this


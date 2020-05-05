#!groovy

def generalMessage = "Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"

// Slack Notifiers
def notifySlackSuccess(channel, generalMessage) {
  slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! ${generalMessage}"
}

def notifySlackUnstable(channel) {
  slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackFail(channel, message, error) {
  slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackStart(channel) {
  slackSend channel: "${channel}", color: '#4a90e2', message: "STARTED! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}


// Discord Notifiers
def notifyDiscordSuccess(channel) {
  slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifyDiscordUnstable(channel) {
  slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifyDiscordFail(channel, message, error) {
  slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifyDiscordStart(discordWebURL) {
  discordSend description: "STARTED! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}", footer: '', image: '', link: 'https://yahoo.com', result: 'ABORTED', thumbnail: '', title: 'Hello World', webhookURL: 'https://discordapp.com/api/webhooks/707044633816989787/NqD89TdUZmJBSwcKv1PyYMrEiv1uzlglPMxz2tcd43HPZ2PhB595HzGG-Hw6S0dNxbJ2'
}

return this
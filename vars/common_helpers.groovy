#!groovy

def notifySlackSuccess(channel) {
  slackSend channel: "${channel}", color: '#7ed321', message: "SUCCESS! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackUnstable(channel, jobFix) {
  slackSend channel: "${channel}", color: '#ffd806', message: "UNSTABLE! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackFail(channel, message, error) {
  slackSend channel: "${channel}", color: '#ff3366', message: "FAILED! Error: ${message} | Reason: ${error} | Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackStart(channel) {
  slackSend channel: "${channel}", color: '#4a90e2', message: "STARTED! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}


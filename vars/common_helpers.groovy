#!groovy

def notifySlackSuccess(channel) {
  slackSend channel: "${channel}", color: 'good', message: "SUCCESS! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackUnstable(channel, jobFix) {
  slackSend channel: "${channel}", color: 'warning', message: "UNSTABLE! Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

def notifySlackFail(channel, message, error) {
  slackSend channel: "${channel}", color: 'danger', message: "FAILED! Error: ${message} | Reason: ${error} | Job Name: ${env.JOB_NAME} | Build Number: ${env.BUILD_NUMBER} | URL: ${env.BUILD_URL}"
}

return this
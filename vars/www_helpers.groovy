#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gBucketURL) {
    sh """
        gsutil -h "Cache-Control:no-cache,max-age=0" \
            -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil acl ch -u AllUsers:R ${gBucketURL}
    """
}

// fbDeploy -- expecting 1 inputs
def fbDeploy(gcpCredentialsId) {
    nodejs('NodeJS') {
        withCredentials([file(credentialsId: "${gcpCredentialsId}", variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
            sh "firebase deploy --only hosting --non-interactive"
        }
    }
}

return this

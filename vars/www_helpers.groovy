#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gBucketURL) {
    sh """
        gsutil -h "Cache-Control:no-cache,max-age=0" \
            -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil acl ch -u AllUsers:R ${gBucketURL}
    """
}

// fbDeploy -- expecting 0 inputs
def fbDeploy(fbCredentials) {
    nodejs('NodeJS') {
        withCredentials([string(credentialsId: "${fbCredentials}", variable: "FB_KEY")]) {
            sh "firebase deploy --token ${FB_KEY}"
        }
    }
}

return this

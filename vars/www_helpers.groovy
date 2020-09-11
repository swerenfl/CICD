#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gcKey, gBucketURL) {
    sh """
        gcloud auth activate-service-account --key-file=${gcKey}
        gsutil -m rm ${gBucketURL}/**
        gsutil -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil iam ch allUsers:objectViewer ${gBucketURL}
    """
}

return this
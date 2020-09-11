#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gBucketURL) {
    sh """
        gsutil -h "Cache-Control:no-cache,max-age=0" \
            -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil acl ch -u AllUsers:R ${gBucketURL}
    """
}

return this

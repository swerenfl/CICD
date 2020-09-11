#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gcKey, gBucketURL) {
    sh """
        gcloud auth activate-service-account --key-file=${gcKey}
        gsutil -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil acl ch -u AllUsers:R ${gBucketURL}
        gsutil setmeta -h "Content-Type:text/html" \
            -h "Cache-Control:private, max-age=0, no-transform" ${gBucketURL}/*.html
    """
}
//        gsutil -m rm ${gBucketURL}/**
return this
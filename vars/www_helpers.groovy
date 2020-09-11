#!groovy

// wwwDeploy -- expecting 2 inputs
def wwwDeploy(gcKey, gBucketURL) {
    sh """
        gcloud auth activate-service-account --key-file=${gcKey}
        gsutil -h "Cache-Control:no-cache,max-age=0" \
            -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
        gsutil acl ch -u AllUsers:R ${gBucketURL}
    """
}
//Cache-Control:no-cache,max-age=0
//        gsutil -m rm ${gBucketURL}/**
return this

//gsutil -h "Cache-Control:public,max-age=3600" cp -a public-read \
//       -r photos gs://bucket/photos
//        //gsutil -m rsync -x '.git' -r ${WORKSPACE} ${gBucketURL}
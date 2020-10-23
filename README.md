# CICD #

This README describes the CICD repo and its various functions.

### What is this repository for? ###

* Quick Summary
* Layout of Repo
* Platform / Tools required
* Deployable Apps

### Quick Summary ###

This repo contains Jenkins pipelines, written in Groovy, to deploy or standup various items in GCP (Google Cloud Platform)

### Layout of Repo ###

As of this writing the repo contains three folders which best described [here](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure).
+ resources
    * A resources directory allows the libraryResource step to be used from an external library to load associated non-Groovy files. As of this writing, there are no files in this directory.
+ src
    * The src directory should look like standard Java source directory structure. This directory is added to the classpath when executing Pipelines. Because this is Jenkins best practice, underneath `src` is `rs3.me` which is the project. Going deeper it splits off into `aws` and `gcp`. As of today, there is nothing in the `AWS` directory.
+ vars
    * The vars directory hosts script files that are exposed as a variable in Pipelines. The name of the file is the name of the variable in the Pipeline. `vars` directory is heavily used to keep pipeline files minimal. 

### Platform / Tools required ###

1. Minecraft project
    * Compute Engine (GCP)
    * Mounted Drive
    * Minecraft service
    * Jenkins
2. Website(s)
    * Bucket (GCP)
    * Load Balancers to redirect bucket and also to redirect 80 --> 443
    * Jenkins

### Deployable Apps ###
1. Minecraft project
    * It is recommended to follow this [tutorial](https://cloud.google.com/solutions/gaming/minecraft-server).
    * Going through the tutorial, you'll realize that running a Minecraft server in the cloud is not cheap, so you'll want to make the Compute preemptive. This means the server will shut down from time to time if there is no activity.
        * The START pipeline checks to see if the Compute is on, and if it isn't then turn it on. Once that step is complete, it'll then check to see if the drive is mounted. If the drive is not mounted, it'll mount it, and finally it will check and see if the Minecraft instance is running. If it isn't it'll start it.
        * The STOP pipeline stops the instance if you want kill it before it gets to its preemptive timer.
        * The UPDATE pipeline will check for the latest version of Minecraft and update if necessary. There is checks that if the instance is offline to start it first.
        * The STATUS pipeline just shows status
    * These can all be triggered from Slack/Jenkins depending on how you have your Jenkins job configuration setup
2. Website(s)
    * As of this writing, only [Last Hope Guild](https://lasthopeguild.com) is up and running. It is recommended to follow this [tutorial](https://cloud.google.com/storage/docs/hosting-static-website).
    * Going through this, you'll come across the problem of how in the world do you deploy your changes.
        * First, setup a webhook in GitHub and your Jenkins. This means that when you commit to GitHub, it'll trigger what you set it up to do in Jenkins.
        * The DEPLOY pipeline(s) call method `common_stages.wwwDeployStage("${G_BUCKET_URL}")`. You only pass in the bucket URL. In doing this it'll rsync your files. The deploy takes roughly 5 seconds and even dumps the cache.


Fun projects -- what else should I do?
-Rich
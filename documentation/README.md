# SSL on Jenkins #

This README describes how to install SSL on Jenkins via nginx and certbot

### Prerequisites ###

* A Debian-based distro
* Jenkins Server

### Installation ###

1. Step 1: Install NGINX
    * `sudo apt-get update`
    * `sudo apt-get upgrade`
    * `sudo apt install -y nginx vim`

2. Step 2: Install Certbot and Verify
    * sudo apt-get update
    * sudo apt-get upgrade
    * sudo apt-get install -y certbot
    * sudo apt-get install -y python3-certbot-nginx
    * certbot --version

3. Configure NGINX
    * sudo vim /etc/nginx/conf.d/jenkins.conf
    * Insert the following code and replace jenkins.example.com with your Jenkins server domain name
```
################################################
# Jenkins Nginx Proxy configuration
#################################################
upstream jenkins {
  server 127.0.0.1:8080 fail_timeout=0;
}

server {
  listen 80;
  server_name jenkins.example.com;

  location / {
    proxy_set_header        Host $host:$server_port;
    proxy_set_header        X-Real-IP $remote_addr;
    proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header        X-Forwarded-Proto $scheme;
    proxy_pass              http://jenkins;
    # Required for new HTTP-based CLI
    proxy_http_version 1.1;
    proxy_request_buffering off;
    proxy_buffering off; # Required for HTTP-based CLI to work over SSL
  }
}
```


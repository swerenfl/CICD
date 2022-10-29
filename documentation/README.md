# SSL on Jenkins #

This README describes how to install SSL on Jenkins via nginx and certbot

### Prerequisites ###

* A Debian-based distro
* Jenkins Server
* Working DNS name. In this tutorial we'll use `jenkins.example.com`

### Installation ###

1. Install NGINX
    * `sudo apt-get update`
    * `sudo apt-get upgrade`
    * `sudo apt install -y nginx vim`

2. Install Certbot and Verify
    * `sudo apt-get update`
    * `sudo apt-get upgrade`
    * `sudo apt-get install -y certbot`
    * `sudo apt-get install -y python3-certbot-nginx`
    * `certbot --version`

3. Configure NGINX
    * `sudo vim /etc/nginx/conf.d/jenkins.conf`
    * Insert the following code and replace `jenkins.example.com` with your Jenkins server domain name
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
4. Validate your NGINX configuration
    * `sudo nginx -t`
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

NOTE: If you run into the following error: `nginx: [emerg] bind() to [::]:80 failed (98: Address already in use)` run: `sudo apachectl stop`

5. Start NGINX if no errors
    * `sudo systemctl enable --now nginx`
    * `sudo systemctl restart nginx`

6. Configure Firewall
    * `sudo apt-get update`
    * `sudo apt-get upgrade`
    * `sudo apt-get install -y ufw`
    * `sudo ufw allow proto tcp from any to any port 80,443`
    * `sudo ufw status`

7. Get Let's Encrypt Certificate
    * `export DOMAIN="jenkins.example.com"`
    * `export ALERTS_EMAIL="webmaster@example.com"`
    * `sudo certbot --nginx --redirect -d $DOMAIN --preferred-challenges http --agree-tos -n -m $ALERTS_EMAIL --keep-until-expiring`

Sample Output
```
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator nginx, Installer nginx
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for jenkins.example.com
Waiting for verification...
Cleaning up challenges
Deploying Certificate to VirtualHost /etc/nginx/conf.d/jenkins.conf
Redirecting all traffic on port 80 to ssl in /etc/nginx/conf.d/jenkins.conf

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Congratulations! You have successfully enabled
https://jenkins.example.com

You should test your configuration at:
https://www.ssllabs.com/ssltest/analyze.html?d=jenkins.example.com
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/jenkins.example.com/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/jenkins.example.com/privkey.pem
   Your cert will expire on 2021-10-05. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot again
   with the "certonly" option. To non-interactively renew *all* of
   your certificates, run "certbot renew"
 - Your account credentials have been saved in your Certbot
   configuration directory at /etc/letsencrypt. You should make a
   secure backup of this folder now. This configuration directory will
   also contain certificates and private keys obtained by Certbot so
   making regular backups of this folder is ideal.
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le
```
8. Verify NGINX Config Auto-Changes
    * `sudo vim /etc/nginx/conf.d/jenkins.conf`
    * Verify `certbot` changes are littered throughout

9. Set certbot to auto-renew
    * `sudo crontab -e`
    * `0 12 * * * /usr/bin/certbot renew --quiet`





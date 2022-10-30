# Timezone on Jenkins #

This README describes how to correct your timezone on Jenkins. In Jenkins you can modify your user settings for timezone, but you may want to correct it at the server level.

### Prerequisites ###

* A Debian-based distro
* Jenkins Server

### Steps if Server Time is off ###

1. Check Current Time
    * Run `timedatectl`. It'll spit out:
```
    Local time: Fri 2020-04-03 19:23:29 UTC
    Universal time: Fri 2020-04-03 19:23:29 UTC
    RTC time: Fri 2020-04-03 19:23:29
    Time zone: UTC (UTC, +0000)
    System clock synchronized: no
    NTP service: inactive
    RTC in local TZ: no
```
Note that the timezone is `UTC`. We need to correct that. At the time of this writing, CT (America Central Time) is the current time zone

2. Correct timezone
    * `sudo timedatectl set-timezone America/Chicago`

3. Verify timezone
    * Run `timedatectl` again to verify changes

4. Restart Jenkins and verify
    * Run `sudo systemctl restart jenkins`
    * Once Jenkins is back up, navigate to `https://jenkins_server/systemInfo`

5. Clap your hands because you are all done.
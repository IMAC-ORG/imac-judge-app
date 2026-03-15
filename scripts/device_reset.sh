#!/bin/sh

#
# The following fixes some ownership errors that some devices had, removes all old contest data, and performs a device upgrade
#

sudo chown judge.judge .judge_last_release
sudo chown -R judge.judge /var/opt/judge
sudo chmod -R go-w /var/opt/judge/

cd /var/opt/judge
rm *.log *.dat *.json *.zip
rm pilots/scores/*

cd ~
rm .judge_last_release  
wget -O fetch_update.sh https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/main/scripts/fetch_update.sh
chmod u+x fetch_update.sh
./fetch_update.sh

# removes this script so it doesn't get accidentally run again
rm device_reset.sh
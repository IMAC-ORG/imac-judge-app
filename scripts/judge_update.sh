#!/bin/bash
sudo systemctl stop judge.service
sudo systemctl stop kiosk.service

sudo wget -O /var/opt/judge/bin/judge.jar https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/main/scripts/judge.java
sudo mv /home/judge/judge.jar /var/opt/judge/bin/judge.jar
sudo chmod 777 -R /var/opt/judge

sudo systemctl start judge.service
sudo systemctl start kiosk.service

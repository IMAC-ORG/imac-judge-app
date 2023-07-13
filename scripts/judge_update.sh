#!/bin/bash
sudo systemctl stop judge.service
sudo systemctl stop kiosk.service

sudo wget -O /var/opt/judge/bin/judge.jar https://github.com/IMAC-ORG/imac-judge-app/releases/latest/download/judge.jar
sudo chmod 777 -R /var/opt/judge

sudo systemctl start judge.service
sudo systemctl start kiosk.service

#!/bin/bash
systemctl stop judge.service
systemctl stop kiosk.service

sudo wget -O /var/opt/judge/bin/judge.jar http://judge.jar

sudo chmod 777 -R /var/opt/judge

systemctl start judge.service
systemctl start kiosk.service

#!/bin/bash
systemctl stop judge.service
systemctl stop kiosk.service

mv judge-0.0.1-SNAPSHOT.jar /var/opt/judge/bin/judge.jar

systemctl start judge.service
systemctl start kiosk.service

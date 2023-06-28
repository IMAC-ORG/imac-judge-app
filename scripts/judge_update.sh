#!/bin/bash
sudo systemctl stop judge.service
sudo systemctl stop kiosk.service

sudo wget -O /var/opt/judge/bin/judge.jar https://drive.google.com/file/d/1dkYaoU-6ueR7LCX1nh9Kpic7iX_rgmsH/view?usp=drive_link

sudo chmod 777 -R /var/opt/judge

sudo systemctl start judge.service
sudo systemctl start kiosk.service

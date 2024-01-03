#!/bin/bash

sudo systemctl stop judge.service
sudo systemctl stop kiosk.service


case $1 in
        "std")
        sudo wget -O /var/opt/judge/bin/judge.jar https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/alternate_rounds/scripts/judge.std;;

        "alt")
        sudo wget -O /var/opt/judge/bin/judge.jar https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/alternate_rounds/scripts/judge.alt;;

        *)
        sudo wget -O /var/opt/judge/bin/judge.jar https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/alternate_rounds/scripts/judge.std;;
esac

sudo chmod 777 -R /var/opt/judge

sudo systemctl start judge.service
sudo systemctl start kiosk.service
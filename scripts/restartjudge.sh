#!/bin/bash

echo Restarting services....
sudo systemctl start judge.service
sudo systemctl start kiosk.service

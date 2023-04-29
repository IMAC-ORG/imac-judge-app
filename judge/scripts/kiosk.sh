#!/bin/bash

xset s noblank
xset s off
xset -dpms

unclutter -idle 0.5 -root &

sed -i 's/"exited_cleanly":false/"exited_cleanly":true/' /home/judge/.config/chromium/Default/Preferences
sed -i 's/"exit_type":"Crashed"/"exit_type":"Normal"/' /home/judge/.config/chromium/Default/Preferences

sleep 20
/usr/bin/chromium-browser --enable-speech-dispatcher --noerrdialogs --disable-infobars --kiosk http://localhost:8080

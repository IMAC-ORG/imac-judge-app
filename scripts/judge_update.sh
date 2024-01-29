#!/bin/bash

if [ ! -d /var/opt/judge ]; then
   echo Creating judge folder...
   sudo mkdir -p /var/opt/judge
fi

if [ "$(stat -c '%U' /var/opt/judge)" != "judge" ]; then
   echo Changing owner of judge folder...
   sudo chown -R judge.judge /var/opt/judge
   echo Changing judge service to use judge owner...
   sudo sed -i 's/User=root/User=judge/g' /lib/systemd/system/judge.service
   sudo systemctl daemon-reload
fi

if [ ! -f ".judge_last_release" ]; then
  touch ".judge_last_release"
fi

last_release=$(cat .judge_last_release)

latest_release=$(curl --silent --fail -G https://api.github.com/repos/IMAC-ORG/imac-judge-app/releases/latest)
if [ $? -ne 0 ]; then
   echo "Error fetching latest release (no internet?)!" >&2
   exit 1
fi

latest_tag=$(echo $latest_release | grep -oP '"tag_name": "(.*?)"' | cut -d' ' -f2 | tr -d [\"])
if [ $? -ne 0 ]; then
   echo "Error parsing feed!" >&2
   exit 1
fi

if [ "$latest_tag" != "$last_release" ]; then
    echo "New release found: $latest_tag"
    echo "Previous release: $last_release"

    echo $latest_tag > .judge_last_release

    echo "Downloading assets..."
    assets_url=$(echo $latest_release | grep -oP '"assets_url": "(.*?)"' | cut -d' ' -f2 | tr -d [\"])
    if [ $? -ne 0 ]; then
       echo "Error parsing for assets url!" >&2
       exit 1
    fi

    assets=$(curl --silent --fail -G $assets_url | grep download_url | tr -d [\"] | cut -d' ' -f6)
    if [ $? -ne 0 ]; then
       echo "Error fetching assets feed!" >&2
       exit 1
    fi

    for asset_url in $assets
    do
        echo found asset=$asset_url
        curl --silent --fail -OL $asset_url
        if [ $? -ne 0 ]; then
           echo "Error fetching asset!" >&2
           exit 1
        fi
    done

    echo Stopping services....
    sudo systemctl stop judge.service
    sudo systemctl stop kiosk.service

    if [ -f judge.jar ]; then
        mv judge.jar /var/opt/judge/bin/judge.jar
        echo Installed/Upgraded judge to version $latest_tag
    fi

    if [ -f figures.zip ]; then
        unzip -qu figures.zip -d /var/opt/judge
        rm figures.zip
        echo Installed/Upgraded judge figures to version $latest_tag
    fi

    echo Starting services....
    sudo systemctl start judge.service
    sudo systemctl start kiosk.service
else
    echo "Latest version already installed"
fi
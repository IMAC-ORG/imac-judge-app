#!/bin/sh
# =============================================================================
# judge_update.sh - AeroJudge Update Script
# =============================================================================
# This script checks for and installs updates from GitHub releases.
# It is fetched and executed by fetch_update.sh on the device.
#
# Exit Codes:
#   0 = No update needed (already running latest version)
#   1 = Error occurred during update
#   2 = Update successfully applied
# =============================================================================

# Function to compare semantic versions in format v#.# or v#.#.#
# Returns: 0 if version1 > version2, 1 otherwise
compare_versions() {
    local version1=$1
    local version2=$2
    
    # Remove 'v' prefix if present
    version1=$(echo "$version1" | sed 's/^v//')
    version2=$(echo "$version2" | sed 's/^v//')
    
    # Compare versions numerically using awk
    # awk handles version comparison by splitting on '.' and comparing numerically
    result=$(awk -v v1="$version1" -v v2="$version2" 'BEGIN {
        split(v1, a, ".")
        split(v2, b, ".")
        for (i = 1; i <= 3; i++) {
            if (a[i] == "") a[i] = 0
            if (b[i] == "") b[i] = 0
            if (a[i]+0 > b[i]+0) { print 0; exit }
            if (a[i]+0 < b[i]+0) { print 1; exit }
        }
        print 1
    }')
    
    return "$result"
}

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

echo "Current version: $last_release"
echo "Latest version: $latest_tag"

# Check if new version is greater than last release
if [ -z "$last_release" ] || compare_versions "$latest_tag" "$last_release"; then
    echo "New release found: $latest_tag"

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
        rm -rf /var/opt/judge/figures
        unzip -qo figures.zip -d /var/opt/judge
        rm figures.zip
        echo Installed/Upgraded judge figures to version $latest_tag
    fi

    echo Starting services....
    sudo systemctl start judge.service
    sudo systemctl start kiosk.service

    echo "Update complete!"
    UPDATE_APPLIED=true
else
    echo "Latest version already installed"
    UPDATE_APPLIED=false
fi

#now checking for volume service and install if not found
if [ ! -d /var/opt/volume_service ]; then
    if [ -f volume_service.zip ]; then
      echo "Installing and starting volume service..."

      echo Creating volume_service folder and extracting files...
      sudo mkdir -p /var/opt/volume_service
      sudo chown judge.judge /var/opt/volume_service
      unzip -quoj volume_service.zip -d /var/opt/volume_service/
      rm volume_service.zip

      echo Starting volume service...
      chmod +x /var/opt/volume_service/volume.service
      sudo mv /var/opt/volume_service/volume.service /etc/systemd/system
      sudo systemctl enable volume
      sudo systemctl start volume

    else
      echo Latest release does not include the volume service
    fi

fi

#check for xset added to .bashrc that disables key repeat (after reboot)
if ! grep -q "xset r off" /home/judge/.bashrc; then
    echo "export DISPLAY=:0" >> /home/judge/.bashrc
    echo "xset r off" >> /home/judge/.bashrc
    #now implement the change
    export DISPLAY=:0
    xset r off
fi

# Exit with appropriate code
if [ "$UPDATE_APPLIED" = true ]; then
    exit 2  # Update successfully applied
else
    exit 0  # No update needed (already running latest version)
fi
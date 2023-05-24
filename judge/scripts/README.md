Using the Raspberry Pi Imager write the Raspberry Pi OS (32-bit) to the SD card


Once the image has complete, remove and re-insert the SD into your PC

on the bootfs volume rename the file wpa_supplicant.conf and open the file for editing

Remember to replace <Country Code>, <SSID>, and <PASSWORD> with your own country code (US), WiFi SSID and password.

again on the bootfs volume eidt the settings.josn and update with the relivant details

judge_id = (1-x) We''ll use this id to post the score to Score as judge 1-x
score_host = This is the IP address that score will be running on, it's advisable to set a reservation on your AP for the Score Laptop so the IP never changes
score_http_port = The port you'll confgigure Score to listen on
line_number = This is the flight line if you are running multiple flight lines ( Leave it as 1 for now)
language = This will be used to determine which audio files to use. (Currently on being used)

Once complete insert the SD in to the rPI and boot.

echo "deb http://mirror.ox.ac.uk/sites/archive.raspbian.org/archive/raspbian bullseye main contrib non-free rpi" >> /etc/apt/sources.list
sudo apt update
sudo apt upgrade -y

sudo apt purge wolfram-engine scratch scratch2 nuscratch sonic-pi idle3 -y
sudo apt purge smartsim java-common minecraft-pi libreoffice* lxplug-updater -y
sudo apt clean
sudo apt autoremove -y

sudo raspi-config
# 1 System Options, then S5 Boot / Auto Login, and finally B4 Desktop Autologin — Desktop GUI, automatically logged in as ‘pi’ user:

sudo apt install vim openjdk-17-jre xdotool unclutter sed locate vim -y
sudo apt clean
sudo apt autoremove -y

vi /boot/cmdline.txt
now just add ” ipv6.disable=1″ to the end of the line

git clone https://github.com/waveshare/LCD-show.git
cd LCD-show/
chmod +x LCD35-show
./LCD35-show
sudo ./LCD35-show 180

sudo apt install xinput-calibrator

/etc/X11/xorg.conf.d/99-calibration.conf
Section "InputClass"
		Identifier		"calibration"
		MatchProduct	"ADS7846 Touchscreen"
		Option	"Calibration" "277 3995 3849 212"
EndSection


dtoverlay=disable-bt
dtparam=speed=42000000
dtparam=fps=30 
# gkio-key
dtoverlay=gpio-key,gpio=22,active_low=1,gpio_pull=up,keycode=2  # Not Observer
dtoverlay=gpio-key,gpio=13,active_low=1,gpio_pull=up,keycode=3  # -0.5
dtoverlay=gpio-key,gpio=6,active_low=1,gpio_pull=up,keycode=4   # -1
dtoverlay=gpio-key,gpio=26,active_low=1,gpio_pull=up,keycode=5  # encoder up/previous
dtoverlay=gpio-key,gpio=5,active_low=1,gpio_pull=up,keycode=6   # zero
dtoverlay=gpio-key,gpio=19,active_low=1,gpio_pull=up,keycode=7  # encoder down/next
dtoverlay=gpio-key,gpio=27,active_low=1,gpio_pull=up,keycode=8  # break
dtoverlay=gpio-key,gpio=2,active_low=1,gpio_pull=up,keycode=9   # +0.5
dtoverlay=gpio-key,gpio=3,active_low=1,gpio_pull=up,keycode=10  # +1
dtoverlay=gpio-key,gpio=4,active_low=1,gpio_pull=up,keycode=11  #speak
	
#define KEY_1			2
#define KEY_2			3
#define KEY_3			4
#define KEY_4			5
#define KEY_5			6
#define KEY_6			7
#define KEY_7			8
#define KEY_8			9
#define KEY_9			10
#define KEY_0			11


sudo mkdir /var/opt/judge
sudo mkdir /var/opt/judge/bin

sudo mv judge.sh /var/opt/judge/bin
sudo chmod +x /var/opt/judge/bin/judge.sh
sudo mv judge.service /lib/systemd/system/
sudo systemctl enable judge.service

sudo mv kiosk.sh /var/opt/judge/bin
sudo chmod +x /var/opt/judge/bin/kiosk.sh
sudo mv kiosk.service /lib/systemd/system/ 
sudo systemctl enable kiosk.service

sudo chmod 777 -R /var/opt/judge

sudo ln -s /boot/settings.json /var/opt/judge/settings.json


sudo dphys-swapfile swapoff
sudo vi /etc/dphys-swapfile
512GB
sudo dphys-swapfile setup
sudo dphys-swapfile swapon

sudo systemctl start judge.service
sudo systemctl start kiosk.service

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


git clone https://github.com/waveshare/LCD-show.git
cd LCD-show/
chmod +x LCD35-show
./LCD35-show
sudo ./LCD35-show 180

sudo apt purge wolfram-engine scratch scratch2 nuscratch sonic-pi idle3 -y
sudo apt purge smartsim java-common minecraft-pi libreoffice* -y

sudo apt clean
sudo apt autoremove -y

sudo apt update
sudo apt upgrade

sudo apt install xdotool unclutter sed


sudo raspi-config
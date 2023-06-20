# SA-rPI-Score Setup From Pre Build SD.

1. Using the Raspberry Pi Imager write the Raspberry Pi OS (32-bit) to the SD card.
   <br>
2. Once the image has complete, remove and re-insert the SD into your PC on the bootfs volume rename the file **wpa_supplicant.conf.bak** to  **wpa_supplicant.conf** and open the file for editing.
   <br>
3. Replace \<Country Code>, \<SSID>, and \<PASSWORD> with your own country code (US), WiFi SSID and password.
   <br>
4. On the bootfs volume eidt the settings.josn and update with the relivant details:
   - judge_id = (1-x) We''ll use this id to post the score to Score as judge 1-x
   - score_host = This is the IP address that score will be running on, it's advisable to set a reservation on your AP for the Score Laptop so the IP never changes
   - score_http_port = The port you'll confgigure Score to listen on
   - line_number = This is the flight line if you are running multiple flight lines ( Leave it as 1 for now)
   - language = This will be used to determine which audio files to use. (Currently on being used)
  <br>
5. Once complete insert the SD in to the rPI and boot.


# SA-rPI-Score Setup From Scratch.

1. Using the Raspberry Pi Imager write the Raspberry Pi OS (32-bit) to the SD card. Confiugre the relivant WiFi details and enable SSH
	- username : judge
	- password : <*password*>
   <br>
2. Once the image has complete, remove and re-insert the SD into your PC.
   <br>
3. If you did not confugre the wifi in the prevous step on the bootfs volume create a filr called **wpa_supplicant.conf** with the following infromation replace **\<Country Code>**, **\<SSID>**, and **\<PASSWORD>** with your own country code **(US)**, WiFi SSID and password.
	```
	ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
	update_config=1
	country=<Country Code>

	network={
		ssid="<SSID>"
		psk="<PASSWORD>"
		scan_ssid=1
	}
	```
	<br>
4. On the bootfs volume creae a settings.josn and add this information ith the relivant details
	```
	{
		"judge_id":1,
		"line_number":1,	
		"score_host":"192.168.1.4",
		"score_http_port":8181,
		"language":"en"
	}
	```

    - judge_id = (1-x) We''ll use this id to post the score to Score as judge 1-x
	- score_host = This is the IP address that score will be running on, it's advisable to set a reservation on your AP for the Score Laptop so the IP never changes
	- score_http_port = The port you'll confgigure Score to listen on
	- line_number = This is the flight line if you are running multiple flight lines (Leave it as 1 for now)
	- language = This will be used to determine which audio files to use. (Currently on being used)
	<br>
5. Once complete insert the SD in to the rPI and boot.
   <br>
6. SSH to the rPI and configure the system swapfile for improved performace
    ```
	sudo dphys-swapfile swapoff
	sudo vi /etc/dphys-swapfile

	Update the swap to 512MB

	sudo dphys-swapfile setup
	sudo dphys-swapfile swapon
	```
	<br>
7. SSH to the rPI and run the the folllowing to update the OS
	```    
	echo "deb http://mirror.ox.ac.uk/sites/archive.raspbian.org/archive/raspbian bullseye main contrib non-free rpi" | sudo tee -a /etc/apt/sources.list
	sudo apt update
	sudo apt upgrade -y
	```
8. Remove some un-used package to improve perforamce
   ```
   sudo apt purge wolfram-engine scratch nuscratch sonic-pi idle3 -y
   sudo apt purge smartsim java-common libreoffice* lxplug-updater -y
   sudo apt clean
   sudo apt autoremove -y
   ```
9. Configure the boot options
   ```
   sudo raspi-config
   # 1 System Options --> S5 Boot / Auto Login --> B4 Desktop Autologin — Desktop GUI --> automatically logged in as ‘judge’ user:
   ```
10. Install useful packages
    ```
	sudo apt install vim openjdk-17-jre xdotool unclutter sed locate vim -y
	sudo apt clean
	sudo apt autoremove -y
	```
	```
	echo "set mouse-=a" | sudo tee -a /root/.vimrc
	echo "set mouse-=a" | sudo tee -a /home/judge/.vimrc
	```
11. Disable IPV6
    ```
	sudo vi /boot/cmdline.txt

	now just add ”ipv6.disable=1″ to the end of the line
	```
12. For WaveShare 3.5" Screens
    ```
	git clone https://github.com/waveshare/LCD-show.git
	cd LCD-show/
	#If 3.5inch RPI IPS LCD (B) rev2.0
	chmod +x LCD35B-show-V2
	./LCD35B-show-V2
	./LCD35B-show-V2 180

	#If 3.5inch RPI IPS LCD (B) rev1.0
	chmod +x LCD35B-show
	./LCD35B-show

    # Refrence here https://www.waveshare.com/wiki/Main_Page#Display-LCD-OLEDs
	```
	After a reboot run the following
	```
	sudo ./LCD35-show 180
	```
	
13. Configre and touch calbrations
    ```
	sudo apt install xinput-calibrator
	```
14. Run the calibations from the dekstop and update the calibariont.conf
    sudo vi /etc/X11/xorg.conf.d/99-calibration.conf
    ```
		Section "InputClass"
			Identifier		"calibration"
			MatchProduct	"ADS7846 Touchscreen"
			Option	"Calibration" "277 3995 3849 212"
		EndSection
	```
15. Setup LCD FPS performance improvments edit the config.txt file on the boofs partition,
    sudo vi /boot/config.txt
    ```
	dtoverlay=disable-bt
	dtparam=speed=41000000
	dtparam=fps=30
	```
16. Configure GPIO to Keyboard Mappings edit the config.txt file on the boofs partition,
    sudo vi /boot/config.txt
    ```
	\# gkio-key
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
	```
	```
	These are the mapapings NUM_KEY to keycode
	\#define KEY_1			2
	\#define KEY_2			3
	\#define KEY_3			4
	\#define KEY_4			5
	\#define KEY_5			6
	\#define KEY_6			7
	\#define KEY_7			8
	\#define KEY_8			9
	\#define KEY_9			10
	\#define KEY_0			11
	```
17. SA-PI-SCORE Button layout
    ```
	\# gkio-key
	dtoverlay=gpio-key,gpio=19,active_low=1,gpio_pull=up,keycode=2  # Not Observer
	dtoverlay=gpio-key,gpio=4,active_low=1,gpio_pull=up,keycode=3   # -0.5
	dtoverlay=gpio-key,gpio=27,active_low=1,gpio_pull=up,keycode=4  # -1
	dtoverlay=gpio-key,gpio=5,active_low=1,gpio_pull=up,keycode=5   # encoder up/previous
	dtoverlay=gpio-key,gpio=3,active_low=1,gpio_pull=up,keycode=6   # zero
	dtoverlay=gpio-key,gpio=22,active_low=1,gpio_pull=up,keycode=7  # encoder down/next
	dtoverlay=gpio-key,gpio=2,active_low=1,gpio_pull=up,keycode=8   # break
	dtoverlay=gpio-key,gpio=13,active_low=1,gpio_pull=up,keycode=9  # +0.5
	dtoverlay=gpio-key,gpio=6,active_low=1,gpio_pull=up,keycode=10  # +1
	dtoverlay=gpio-key,gpio=26,active_low=1,gpio_pull=up,keycode=11 #speak
	```
18. Install the Judging software. Copy the 2 .service file and 2 .sh file into your working directory in the pi
    ```
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
	```

19. Reboot the rPI
    ```
	sudo reboot
	```

Image Rasbian 32 bit to an SD card 8GB or bigger

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



sudo apt-get install espeak
espeak "Text you wish to hear back" 2>/dev/null
espeak "Hello World" 2>/dev/null

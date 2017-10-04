# Move to the directory and give permissions
cd /home/pi/Desktop/Enrollment
chmod -R u+rwx,g+rwx ./

# Install python libraries for the sensor
sudo bash	
wget -O - http://apt.pm-codeworks.de/pm-codeworks.de.gpg | apt-key add -
wget http://apt.pm-codeworks.de/pm-codeworks.list -P /etc/apt/sources.list.d/

apt-get update
apt-get install python-fingerprint --yes

# Configure the autostart on reboot
cat ./lxsession.autostart > /home/pi/.config/lxsession/LXDE-pi/autostart
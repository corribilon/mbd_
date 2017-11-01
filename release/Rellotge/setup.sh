cd /home/pi/Desktop/Rellotge
sudo chmod -R u+rwx,g+rwx ./


sudo wget -O - http://apt.pm-codeworks.de/pm-codeworks.de.gpg | apt-key add -
sudo wget http://apt.pm-codeworks.de/pm-codeworks.list -P /etc/apt/sources.list.d/

sudo apt-get update
sudo apt-get install python-fingerprint --yes



# Configure the autostart on reboot
cat ./lxsession.autostart > /home/pi/.config/lxsession/LXDE-pi/autostart


#Configure CronTab
echo "*/1 * * * * export DISPLAY=:0.0 && /home/pi/Desktop/Rellotge/watchdog.sh" >> mycron
echo "*/10 * * * * export DISPLAY=:0.0 && /home/pi/Desktop/SensorSync/sensorsync.sh" >> mycron
echo "0 15 * * * export DISPLAY=:0.0 && /home/pi/Desktop/SensorSync/readLocalDB.sh" >> mycron
echo "1 0 * * * export DISPLAY=:0.0 && /home/pi/Desktop/SensorSync/photodownload.sh" >> mycron
echo "0 0 * * * sudo reboot" >> mycron
#install new cron file
crontab mycron
rm mycron

echo "Finish setup for Rellotge successfully!!"
echo ""

echo "+-----------------------------+"
echo "+ DONT FORGET!                +"
echo "+-----------------------------+-----------------+"
echo "| - Change the config.rellotge.properties file. |"
echo "| - Change the presentationRellotge.html 'logo' |"
echo "|     variable (At the beginning of the file).  |"
echo "+-----------------------------------------------+"


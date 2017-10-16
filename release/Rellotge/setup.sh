cd /home/pi/Desktop/Rellotge
sudo chmod -R u+rwx,g+rwx ./




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

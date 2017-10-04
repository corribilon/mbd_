cd /home/pi/Desktop/Rellotge
sudo chmod -R u+rwx,g+rwx ./


echo "*/1 * * * * export DISPLAY=:0.0 && /home/pi/Desktop/Rellotge/watchdog.sh" >> mycron
echo "0 0 * * * sudo reboot" >> mycron
#install new cron file
crontab mycron
rm mycron

# Configure the autostart on reboot
cat ./lxsession.autostart > /home/pi/.config/lxsession/LXDE-pi/autostart

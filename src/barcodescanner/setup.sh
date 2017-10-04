cd /home/pi/Desktop/BarcodeScanner
sudo chmod -R u+rwx,g+rwx ./


echo "*/1 * * * * export DISPLAY=:0.0 && /home/pi/Desktop/BarcodeScanner/watchdog.sh" >> mycron
echo "0 0 * * * sudo reboot" >> mycron
#install new cron file
crontab mycron
rm mycron

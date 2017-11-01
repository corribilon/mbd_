cd /home/pi/Desktop/BarcodeScanner
sudo chmod -R u+rwx,g+rwx ./

echo "Did you change the lxsession.autostart file for the url ?? (/home/pi/Desktop/BarcodeScanner/lxsession.autostart)"
echo "(s/n)"
read input_variable


if echo "$input_variable" | grep -iq "^s" ;
then
    # Configure the autostart on reboot
	cat ./lxsession.autostart > /home/pi/.config/lxsession/LXDE-pi/autostart
	
	echo "*/1 * * * * export DISPLAY=:0.0 && /home/pi/Desktop/BarcodeScanner/watchdog.sh" >> mycron
	echo "0 0 * * * sudo reboot" >> mycron
	#install new cron file
	crontab mycron
	rm mycron
	echo "Finish setup for BarcodeScanner successfully!!"
	echo "Don't forget to change the configbarcodescanner.properties file."
else
	echo "Please change the url on the '/home/pi/Desktop/BarcodeScanner/lxsession.autostart' file."
	echo "Then execute again the setup.sh file."	
fi







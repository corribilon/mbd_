#!/bin/bash

for num in 1 2 3 4 5 6
do

	echo "$num running...";

	reinit=0
	varTest=""
	for file in /home/pi/Desktop/Rellotge/timers/*
	do
		# do something on $file
		oldnum=`cat "$file"`
		newnum=`expr $oldnum + 1`
		sed -i "s/$oldnum\$/$newnum/g" "$file" 
		varTest=$(cat "$file")
		echo "EX: $varTest"
		
		if [ "$varTest" -gt 8 ]
		then 
			reinit=1
		fi
		
	done


	if [ "$reinit" -eq 1 ]
	then	
		#kill $(jps -m | grep "jar" |  awk '{print $1}')
		#export DISPLAY=:0.0
		
		sudo reboot	
	fi

	sleep 10

done

#!/bin/bash

tA=0.97
ramDrive="/dev/shm"
CamPath=$ramDrive"/GetCamsJ"
UStatus=$(curl "http://127.0.0.1/cgi-bin/apcupsd/upsstats.cgi" 2>&1 /dev/null/ | sed -n 's/.*Status:\ //p')

appWalk2DB="timeout --kill-after=90 90 java -cp $ramDrive/asUtils asUtils.Walk2DBv4"

if [ -z "$UStatus" ]; then
	UStatus="ERROR"
fi

echo $UStatus > $CamPath/LUStatus.txt

flock -n $ramDrive/GetCamsJ/CamPusher.lock java -cp $ramDrive/asUtils asUtils.CamPusher
flock -n $ramDrive/FeedsM.lock java -cp $ramDrive/asUtils asUtils.Feeds "TwoMinute" &
flock -n $ramDrive/xs7r.lock timeout --kill-after=30 30 java -cp $ramDrive/asUtils asUtils.xs7 Wunder 2>&1 > /dev/shm/xs7.log &
flock -n $ramDrive/Walk2DB_Main.lock $appWalk2DB "Desktop"

ping -q -c2 192.168.1.1 > /dev/null
if [ $? -eq 0 ]
then 
	flock -n $ramDrive/Walk2DB_Router.lock $appWalk2DB "Router"
else
	echo "Host 192.168.1.1 / Router is down!" > $ramDrive/snmpwalkAsus.txt
fi

ping -q -c2 192.168.1.8 > /dev/null
if [ $? -eq 0 ]
then 
	flock -n $ramDrive/Walk2DB_Pi.lock $appWalk2DB "Pi"
else
	echo "Host 192.168.1.8 / Raspberry Pi is down!" > $ramDrive/snmpwalkPi.txt
fi

ping -q -c2 192.168.1.9 > /dev/null
if [ $? -eq 0 ]
then 
	flock -n $ramDrive/Walk2DB_Laptop.lock $appWalk2DB "Laptop"
else
	echo "Host 192.168.1.9 / Laptop is down!" > $ramDrive/snmpwalkLap.txt
fi

ping -q -c2 192.168.1.11 > /dev/null
if [ $? -eq 0 ]
then 
	flock -n $ramDrive/Walk2DB_Pi2.lock $appWalk2DB "Pi2"
else
	echo "Host 192.168.1.11 / Raspberry Pi 2 is down!" > $ramDrive/snmpwalkPi2.txt
fi

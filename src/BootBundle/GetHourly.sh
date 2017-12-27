#!/bin/bash

thisDoW=$(date +%u)
thisHour=$(date +"%H")
thisHourUTC=$(date -u +"%H")
xHoursAgoUTC=$(date -d "5 hours ago" -u +"%H")
ramDrive="/dev/shm"
#sectorList=($(echo "SELECT Sector FROM WxObs.GOES16SectorsRDO WHERE Active=1 ORDER BY Sector ASC;" | mysql -N))

#for thisSector in ${sectorList[@]}; do
#	java -cp /dev/shm/asUtils asUtils.G16Hourly $thisSector
#done

flock -n /dev/shm/FeedsH.lock java -cp $ramDrive/asUtils asUtils.Feeds "Hour" &
flock -n /dev/shm/xs7.lock timeout --kill-after=1800 1800 java -cp $ramDrive/asUtils asUtils.xs7 2>&1 > /dev/shm/xs7.log &
wait

flock -n /dev/shm/ModelsJ.lock timeout --kill-after=5400 5400 java -cp $ramDrive/asUtils asUtils.Models $xHoursAgoUTC &> /dev/shm/ModelsJ.log

if [ $thisHour -eq "02" ]; then
	bash /dev/shm/MakeMP4.sh
	if [ $thisDoW -eq "2" ]; then
		java -cp $ramDrive/asUtils asUtils.DBBackup WxObs
	fi
fi

if [ $thisHour -eq "06" ]; then
	java -cp $ramDrive/asUtils asUtils.GetDaily 1
fi

if [ $thisHour -eq "15" ]; then
	bash /dev/shm/kWhGet.sh
fi

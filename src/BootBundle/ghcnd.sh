#!/bin/bash

#For 1889 to 2017

tYear=$1
tMoDay=$2

nYear=$((tYear+1))
bDate=$tYear""$tMoDay
eDate=$nYear"0101"
#dPath="/home/astump/Downloads"
tPath="/dev/shm/ghcnd"
dPath=$tPath
wwwPath="/var/www/ASWebUI/Download"

mkdir -p $tPath

wget -O $dPath"/"$tYear".csv.gz" "ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/by_year/"$tYear".csv.gz"
echo $tYear": unpacking data..."
gunzip $dPath"/"$tYear".csv.gz"

tDate=$bDate
while [ $tDate != $eDate ]; do

	echo $tDate": processing data..."

	cat $dPath"/"$tYear".csv" | grep -e $tDate > $tPath"/"$tDate".csv"
	station=$(head -1 $tPath"/"$tDate".csv" | cut -d ',' -f 1)
	while [ ! -z $station ]; do

		cat $tPath"/"$tDate".csv" | grep -e $station > $tPath"/"$tDate"_"$station".csv"
		echo "\""$station"\":{" >> $tPath"/"$tDate."json"
		wxVar=$(cat $tPath"/"$tDate"_"$station".csv" | head -n 1 | cut -d ',' -f 3)
		wxVarData=$(cat $tPath"/"$tDate"_"$station".csv" | head -n 1 | cut -d ',' -f 4)
		while [ ! -z $wxVar ]; do
			echo "\""$wxVar"\":\""$wxVarData"\"," >> $tPath"/"$tDate."json"
			sed -i '/'$wxVar'/d' $tPath"/"$tDate"_"$station".csv"
			wxVar=$(head -1 $tPath"/"$tDate"_"$station".csv" | cut -d ',' -f 3)
			wxVarData=$(head -1 $tPath"/"$tDate"_"$station".csv" | cut -d ',' -f 4)
		done
		echo "}," >> $tPath"/"$tDate."json"
		rm $tPath"/"$tDate"_"$station".csv"
		sed -i '/'$station'/d' $tPath"/"$tDate".csv"
		station=$(head -1 $tPath"/"$tDate".csv" | cut -d ',' -f 1)
	done
	loadJSON=$(cat $tPath"/"$tDate."json")
	tJSONString=$(echo "{"$loadJSON"}" | sed -e 's/ //g' | sed -e 's/,}/}/g')
	echo $tDate": writing SQL outfile..."
	echo "INSERT INTO WxObs.GHCNDByYear VALUES ('"$tDate"','"$tJSONString"');" > $tPath"/"$tDate".sql"
	rm $tPath"/"$tDate".csv"
	rm $tPath"/"$tDate".json"
	echo $tDate": loading SQL into database..."
	mysql < $tPath"/"$tDate".sql"
	ls -s $tPath"/"$tDate".sql"
	rm $tPath"/"$tDate".sql"
	tDate=$(date -d "$tDate + 1 day" +"%Y%m%d")
	
done

rm -fr $tPath"/"$tYear".csv"

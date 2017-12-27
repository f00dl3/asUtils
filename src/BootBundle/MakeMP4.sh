#!/bin/bash

ramTmp="/dev/shm"
getYesterday=$(date -d "yesterday 12:00" +'%y%m%d')
radPath="/var/www/Get/Radar"
radTmp=$ramTmp"/Radar"
radList=($(echo "SELECT Site FROM WxObs.RadarList WHERE Active=1 ORDER BY Site ASC;" | mysql -N))
jClassPath=$ramTmp"/asUtils"

mkdir -p $radTmp
for thisRad in ${radList[@]}; do
  mkdir -p $radTmp/$thisRad
	mv $radPath/$thisRad/Archive/*.gif $radTmp/$thisRad
done
zip -9rv $radPath/MP4/$getYesterday.Archived.zip $radTmp
(ls $radPath/MP4/* -t | head -n 14; ls $radPath/MP4/*)|sort|uniq -u|xargs rm
chown -R www-data $radPath/MP4/
rm -fr $radTmp

mergeHolder=$ramTmp"/RedditDailyMerge"
mkdir -p "/var/www/rOut/Archive"
mkdir $mergeHolder
cd $mergeHolder
for thisArch in "/var/www/rOut/Reddit#20"$getYesterday??.zip; do
	echo "Unpacking: "$thisArch
	unzip $thisArch -d $mergeHolder
done
tar -zcvf "/var/www/rOut/Archive/MergedRD#"$getYesterday".tar.gz" -C $mergeHolder .
cd $ramTmp
rm -fr $mergeHolder

#java -cp $jClassPath asUtils.G16Nightly
java -cp $jClassPath asUtils.CamNightly

#!/bin/bash

cpcAO="ftp://ftp.cpc.ncep.noaa.gov/cwlinks/norm.daily.ao.index.b500101.current.ascii"
cpcAAO="ftp://ftp.cpc.ncep.noaa.gov/cwlinks/norm.daily.aao.index.b790101.current.ascii"
cpcNAO="ftp://ftp.cpc.ncep.noaa.gov/cwlinks/norm.daily.nao.index.b500101.current.ascii"
cpcPNA="ftp://ftp.cpc.ncep.noaa.gov/cwlinks/norm.daily.pna.index.b500101.current.ascii"

curl --retry 5 $cpcAO > /dev/shm/cpc_AO.txt
curl --retry 5 $cpcAAO > /dev/shm/cpc_AAO.txt
curl --retry 5 $cpcNAO > /dev/shm/cpc_NAO.txt
curl --retry 5 $cpcPNA > /dev/shm/cpc_PNA.txt

sed -i 's/  / 0/g' /dev/shm/cpc_AO.txt
sed -i 's/  / 0/g' /dev/shm/cpc_AAO.txt
sed -i 's/  / 0/g' /dev/shm/cpc_NAO.txt
sed -i 's/  / 0/g' /dev/shm/cpc_PNA.txt

sed -i 's/ \+/,/g' /dev/shm/cpc_AO.txt
sed -i 's/ \+/,/g' /dev/shm/cpc_AAO.txt
sed -i 's/ \+/,/g' /dev/shm/cpc_NAO.txt
sed -i 's/ \+/,/g' /dev/shm/cpc_PNA.txt

readarray cpcAO < /dev/shm/cpc_AO.txt
readarray cpcAAO < /dev/shm/cpc_AAO.txt
readarray cpcNAO < /dev/shm/cpc_NAO.txt
readarray cpcPNA < /dev/shm/cpc_PNA.txt

echo "INSERT INTO CPC_AO (ObsDate, Anom) VALUES " > /dev/shm/CPC_AO.sql
echo "INSERT INTO CPC_AAO (ObsDate, Anom) VALUES " > /dev/shm/CPC_AAO.sql
echo "INSERT INTO CPC_NAO (ObsDate, Anom) VALUES " > /dev/shm/CPC_NAO.sql
echo "INSERT INTO CPC_PNA (ObsDate, Anom) VALUES " > /dev/shm/CPC_PNA.sql

(
for thisData in ${cpcAO[@]}
do
	dtYear=$(echo $thisData | cut -d ',' -f 1)
	dtMonth=$(echo $thisData | cut -d ',' -f 2)
	dtDate=$(echo $thisData | cut -d ',' -f 3)
	dtValue=$(echo $thisData | cut -d ',' -f 4)
	echo ",('"$dtYear"-"$dtMonth"-"$dtDate"','"$dtValue"')" >> /dev/shm/CPC_AO.sql
done
) &

(
for thisData in ${cpcAAO[@]}
do
	dtYear=$(echo $thisData | cut -d ',' -f 1)
	dtMonth=$(echo $thisData | cut -d ',' -f 2)
	dtDate=$(echo $thisData | cut -d ',' -f 3)
	dtValue=$(echo $thisData | cut -d ',' -f 4)
	echo ",('"$dtYear"-"$dtMonth"-"$dtDate"','"$dtValue"')" >> /dev/shm/CPC_AAO.sql
done
) &

(
for thisData in ${cpcNAO[@]}
do
	dtYear=$(echo $thisData | cut -d ',' -f 1)
	dtMonth=$(echo $thisData | cut -d ',' -f 2)
	dtDate=$(echo $thisData | cut -d ',' -f 3)
	dtValue=$(echo $thisData | cut -d ',' -f 4)
	echo ",('"$dtYear"-"$dtMonth"-"$dtDate"','"$dtValue"')" >> /dev/shm/CPC_NAO.sql
done
) &

(
for thisData in ${cpcPNA[@]}
do
	dtYear=$(echo $thisData | cut -d ',' -f 1)
	dtMonth=$(echo $thisData | cut -d ',' -f 2)
	dtDate=$(echo $thisData | cut -d ',' -f 3)
	dtValue=$(echo $thisData | cut -d ',' -f 4)
	echo ",('"$dtYear"-"$dtMonth"-"$dtDate"','"$dtValue"')" >> /dev/shm/CPC_PNA.sql
done
) &
wait

echo " ON DUPLICATE KEY UPDATE ObsDate=ObsDate;" >> /dev/shm/CPC_AO.sql
echo " ON DUPLICATE KEY UPDATE ObsDate=ObsDate;" >> /dev/shm/CPC_AAO.sql
echo " ON DUPLICATE KEY UPDATE ObsDate=ObsDate;" >> /dev/shm/CPC_NAO.sql
echo " ON DUPLICATE KEY UPDATE ObsDate=ObsDate;" >> /dev/shm/CPC_PNA.sql

sed -i ':a;N;$!ba;s/\n//g' /dev/shm/CPC_AO.sql
sed -i ':a;N;$!ba;s/\n//g' /dev/shm/CPC_AAO.sql
sed -i ':a;N;$!ba;s/\n//g' /dev/shm/CPC_NAO.sql
sed -i ':a;N;$!ba;s/\n//g' /dev/shm/CPC_PNA.sql

sed -i 's/VALUES \,/VALUES /g' /dev/shm/CPC_AO.sql
sed -i 's/VALUES \,/VALUES /g' /dev/shm/CPC_AAO.sql
sed -i 's/VALUES \,/VALUES /g' /dev/shm/CPC_NAO.sql
sed -i 's/VALUES \,/VALUES /g' /dev/shm/CPC_PNA.sql

mysql WxObs < /dev/shm/CPC_AO.sql &
mysql WxObs < /dev/shm/CPC_AAO.sql &
mysql WxObs < /dev/shm/CPC_NAO.sql &
mysql WxObs < /dev/shm/CPC_PNA.sql &
wait

rm /dev/shm/cpc_*.txt
rm /dev/shm/CPC_*.sql

echo "Done with all tables!"

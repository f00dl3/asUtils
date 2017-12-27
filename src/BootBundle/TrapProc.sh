#!/bin/bash

thisTimestamp=$(date +"%Y%m%d%H%M")

read host
read ip
vars=
 
while read oid val
do
	if [ "$vars" = "" ]; then
		vars="$oid = $val"
	else
		vars="$vars, $oid = $val"
   	fi
done

pIPsrc=$(echo $ip | sed 's/.*P\: \[//' | sed 's/\].*//')
pIPdest=$(echo $ip | sed 's/.*\[//' | sed 's/\].*//')
pVars=$(echo $vars | sed 's/,/ /' | sed "s/\'/ /")

echo $ip > /dev/shm/testip
echo $pIPsrc >>  /dev/shm/testip

sudo -i mysql net_snmp << EOF
INSERT INTO AlarmTable (
	AlarmTime,
	Status,
	Host,
	SourceIP,
	DestIP,
	AlarmText
) VALUES (
	'$thisTimestamp',
	'NEW',
	'$host',
	'$pIPsrc',
	'$pIPdest',
	'$pVars'
);
EOF

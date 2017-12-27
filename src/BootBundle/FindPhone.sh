#!/bin/bash

doWhat=$1

if [ $doWhat = "A" ]; then
	PhoneLocation=$(echo "SELECT Location FROM Note3 ORDER BY WalkTime DESC LIMIT 1;" | sudo -i mysql -N net_snmp)
	PhoneLocation=$(echo $PhoneLocation | sed 's/\[/,/g' | sed 's/\]/,/g')
	PhoneLat=$(echo $PhoneLocation | cut -d ',' -f 3)
	PhoneLon=$(echo $PhoneLocation | cut -d ',' -f 2)
	firefox "http://maps.google.com/maps/@"$PhoneLat","$PhoneLon",21z"
fi

if [ $doWhat = "E" ]; then
	PhoneLocation=$(echo "SELECT Location FROM EmS4 ORDER BY WalkTime DESC LIMIT 1;" | sudo -i mysql -N net_snmp)
	PhoneLocation=$(echo $PhoneLocation | sed 's/\[/,/g' | sed 's/\]/,/g')
	PhoneLat=$(echo $PhoneLocation | cut -d ',' -f 3)
	PhoneLon=$(echo $PhoneLocation | cut -d ',' -f 2)
	firefox "http://maps.google.com/maps/@"$PhoneLat","$PhoneLon",21z"
fi


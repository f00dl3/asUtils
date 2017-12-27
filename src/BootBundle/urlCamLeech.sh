#!/bin/bash

#Global Variables
tempPath="/home/astump/Pictures/tcTmp"
mkdir -p $tempPath
leechURL=$1

while true; do
	curl --max-time 3 $leechURL > $tempPath/$(date +'%y%m%d-%H%M%S-%N').jpeg
	sleep 30
done

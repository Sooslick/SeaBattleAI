# utility script used by startup.php
# checks last startup date and tries to launch application in background

startupFile=lastStartup.txt
touch $startupFile

timestampNew=`date +%s`
timestampOld=$(<$startupFile)
if [ -z "$timestampOld" ]
then
	timestampOld=0
	echo "empty lastStartup.txt"
fi

timeSinceLastStartup=$((timestampNew - timestampOld))
if (( timeSinceLastStartup > 3 )) 
then
	java -jar sb.jar > applog.txt 2>&1 &
	echo "$timestampNew" > $startupFile
	echo "Launched app in background.";
else
	echo "startup denied";
fi
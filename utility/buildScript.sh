# Script updates app and builds executable.
sbRepoDir=/work/SeaBattleAI
targetDir=/work/SbWorkingDir

cd $sbRepoDir
git checkout master
git pull
mvn clean compile assembly:single
cd target
jarName="`ls | grep .*\.jar`"
mv -f $jarName $targetDir/sb.jar
echo "build successful"
if [ "$1" = "run" ]; then
	echo "run arg presents, running built jar"
	cd $targetDir
	java -jar sb.jar
fi
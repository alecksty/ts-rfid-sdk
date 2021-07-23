echo "copy files"
cp RfidLib_jar/*.jar .
cp tsrfid_jar/*.jar .

echo "running"
java -jar tsrfid.jar


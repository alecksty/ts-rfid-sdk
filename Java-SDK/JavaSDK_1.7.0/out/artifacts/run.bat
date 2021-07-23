echo "copy files"
copy RfidLib_jar/*.jar .
copy tsrfid_jar/*.jar .

echo "running"
java -jar tsrfid.jar



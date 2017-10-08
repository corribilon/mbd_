python2 ./deleteAll.py

while read p; do
  	IFS=\; read -a fields <<<"$p"
  	if [[ ${fields[0]} != *"null"* ]]; then
  		python2 ./upload_characterics.py "${fields[0]}"	
  	fi	
done <./localDB.ldb

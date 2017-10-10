rm -r release/
mkdir release/
chmod -R u+rwx,g+rwx ./

#Create the Barcode Scanner release

mkdir release/BarcodeScanner
cp target/barcodescanner.jar-jar-with-dependencies.jar release/BarcodeScanner/scanner.jar
cp config.barcodescanner.properties release/BarcodeScanner/
touch release/BarcodeScanner/buffer.bcs
find src/barcodescanner/ -type f -not -name "*.java" | xargs -i cp {} release/BarcodeScanner/
mkdir release/BarcodeScanner/logs
mkdir release/BarcodeScanner/timers


#Create the Enrollment release

mkdir release/Enrollment
cp target/enrollment.jar-jar-with-dependencies.jar release/Enrollment/enrollment.jar
cp config.enrollment.properties release/Enrollment/
find src/enrollment/ -type f -not -name "*.java" | xargs -i cp {} release/Enrollment/
mkdir release/Enrollment/logs

#Create the Rellotge release

mkdir release/Rellotge
cp target/rellotge.jar-jar-with-dependencies.jar release/Rellotge/scanner.jar
cp config.rellotge.properties release/Rellotge/
cp -r js release/Rellotge/
touch  release/Rellotge/buffer.bcs
find src/rellotge/ -type f -not -name "*.java" | xargs -i cp {} release/Rellotge/
mkdir release/Rellotge/timers
mkdir release/Rellotge/logs

#Create the SensorSync release

mkdir release/SensorSync
cp target/sensorsync.jar-jar-with-dependencies.jar release/SensorSync/sensorsync.jar
cp target/photodownload.jar-jar-with-dependencies.jar release/SensorSync/photodownload.jar
cp config.sensorsync.properties release/SensorSync/
find src/sensorsync/ -type f -not -name "*.java" | xargs -i cp {} release/SensorSync/
mkdir release/SensorSync/logs


# Create the zip files
cd release
find ./ -maxdepth 1 -type d | xargs -i zip -r {}.zip {}
chmod -R u+rwx,g+rwx ./




cd ../target
chmod -R u+rwx,g+rwx ./
mkdir BarcodeScanner
mv barcodescanner.jar-jar-with-dependencies BarcodeScanner/scanner.jar
cp ../config.barcodescanner.properties BarcodeScanner/

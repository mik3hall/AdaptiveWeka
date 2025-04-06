#!/bin/bash

set -e

PACKAGER=`/usr/libexec/java_home`/bin/jpackage
#PACKAGER=/Users/mjh/Documents/GitHub/jdk/build/*/images/jdk/bin/jpackage
SIGNING_CERT_3RD_PARTY="3rd Party Mac Developer Application: Michael Hall (5X6BXQB3Q7)"
DEFAULT_SIGNING_CERT="Michael Hall"
DEVELOPER_ID_APPLICATION="Developer ID Application: Michael Hall (5X6BXQB3Q7)"
SIGNING_CERT=$DEVELOPER_ID_APPLICATION

rm -r weka
rm -r us
echo 'extracing reference weka'
jar -xvf ../weka_other/weka.jar
echo 'compiling'
javac -d . -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar:/Users/mjh/wekafiles/packages/imageFilters/imageFilters.jar Cifar.java \
	src/*.java src_weka/weka/gui/explorer/*.java src_weka/weka/classifiers/*.java src_weka/weka/classifiers/evaluation/*.java
echo 'jarring'
jar -cvf weka.jar us weka org com
mv weka.jar input/weka.jar
rm -r WekaAdaptive.app

${PACKAGER} \
	--verbose \
	--input input \
	--icon input/weka.ico \
	--name WekaAdaptive \
	--type "app-image" \
	--main-jar weka.jar \
	--main-class weka.gui.GUIChooser \
	--module-path '/Users/mjh/Documents/javafx-jmods-21.0.1' \
	--java-options '-Xmx10g -XX:+UnlockDiagnosticVMOptions --add-opens java.desktop/javax.swing.text=ALL-UNNAMED --enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-exports java.base/java.lang=ALL-UNNAMED -Djava.security.manager=allow -DAEVD=true' \
	--mac-package-identifier "us.hall.weka" \
	--mac-sign \
	--mac-signing-key-user-name "$SIGNING_CERT" \

#!/bin/bash
#Created by @Pega88 on 16 Feb 2018

#build the jar from source
ant jar -f src/build/build.xml

#package jar to app
jar2app build/jars/ZclassicSwingWallet.jar  -i ./src/resources/images/zclassic-logo.icns


#add zcld and zcl-cli into the required Contents folder of the App
cp ./zcld ./ZclassicSwingWallet.app/Contents/zcld
cp ./zcl-cli ./ZclassicSwingWallet.app/Contents/zcl-cli


#statically build required libraries
dylibbundler -od -b -x ./ZclassicSwingWallet.app/Contents/zcld \
                    -x ./ZclassicSwingWallet.app/Contents/zcl-cli \
                    -d ./ZclassicSwingWallet.app/Contents/libs \
                    -p @executable_path/libs
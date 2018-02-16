#!/bin/bash
#Created by @Pega88 on 16 Feb 2018

#build the jar from source
ant jar -f src/build/build.xml

#package jar to app
jar2app build/jars/ZclassicSwingWalletUI.jar  -i ./src/resources/images/zclassic-logo.icns


#add zcld and zcl-cli into the required Contents folder of the App
cp ./zcld ./ZclassicSwingWalletUI.app/Contents/zcld
cp ./zcl-cli ./ZclassicSwingWalletUI.app/Contents/zcl-cli

#statically build required libraries
dylibbundler -od -b -x ./ZclassicSwingWalletUI.app/Contents/zcld \
                    -x ./ZclassicSwingWalletUI.app/Contents/zcl-cli \
                    -d ./ZclassicSwingWalletUI.app/Contents/libs \
                    -p @executable_path/libs
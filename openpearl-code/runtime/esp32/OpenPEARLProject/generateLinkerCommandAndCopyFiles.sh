#!/bin/bash
echo "*** extract new library list and linker commands from linker.txt"
source ../../../configuration/.config
echo $CONFIG_INSTALL_Target
echo $IDF_PATH
PWD=`pwd`
echo $PWD
gcc idf2mk.c -o idf2mk
echo "*** convert linker.txt  -> ./cp-cmd and ./esp_idf_build.mk"
./idf2mk linker.txt $IDF_PATH/components/ $PWD $CONFIG_INSTALL_Target/lib/OpenPEARLesp32
chmod +x cp.cmd
echo "*** copy libraries  and linker scripts to $CONFIG_INSTALL_Target/lib/OpenPEARLesp32"
./cp.cmd $PWD
echo "*** done. ./esp_idf_builf.mk is automatically read by ../Makefile" 


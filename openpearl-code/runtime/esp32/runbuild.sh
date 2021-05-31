#!/bin/bash
echo "runbuild: " $1
.  $1/export.sh
pwd
idf.py -C OpenPEARLProject build


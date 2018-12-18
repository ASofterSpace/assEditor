#!/bin/bash

if [[ ! -d ../Toolbox-Java ]]; then
	echo "It looks like you did not yet get the Toolbox-Java project - please do so (and put it as a folder next to the CDM Script Editor folder.)"
	exit
fi

cd src/com/asofterspace

rm -rf toolbox

mkdir toolbox
cd toolbox

mkdir configuration
mkdir io
mkdir gui
mkdir utils

cd ../../../..

cp ../Toolbox-Java/src/com/asofterspace/toolbox/*.java src/com/asofterspace/toolbox
cp ../Toolbox-Java/src/com/asofterspace/toolbox/configuration/*.* src/com/asofterspace/toolbox/configuration
cp ../Toolbox-Java/src/com/asofterspace/toolbox/io/*.* src/com/asofterspace/toolbox/io
cp ../Toolbox-Java/src/com/asofterspace/toolbox/gui/*.* src/com/asofterspace/toolbox/gui
cp ../Toolbox-Java/src/com/asofterspace/toolbox/utils/*.* src/com/asofterspace/toolbox/utils

rm -rf bin

mkdir bin

cd src

find . -name "*.java" > sourcefiles.list

javac -encoding utf8 -d ../bin @sourcefiles.list

cd ..

#!/usr/bin/env bash

echo "Re-building with target Java 7 (such that the compiled .class files will be compatible with as many JVMs as possible)..."

cd src

# build build build!
javac -encoding utf8 -d ../bin -bootclasspath ../other/java7_rt.jar -source 1.7 -target 1.7 @sourcefiles.list

cd ..



echo "Creating the release file assEditor.zip..."

mkdir release

cd release

mkdir assEditor

# copy the main files
cp -R ../bin assEditor
cp ../UNLICENSE assEditor
cp ../README.md assEditor
cp ../assEditor.sh assEditor
cp ../assEditor.bat assEditor

# convert \n to \r\n for the Windows files!
cd assEditor
awk 1 ORS='\r\n' assEditor.bat > rn
mv rn assEditor.bat
cd ..

# create a version tag right in the zip file
cd assEditor
version=$(./assEditor.sh --version_for_zip)
echo "$version" > "$version"
cd ..

# zip it all up
zip -rq assEditor.zip assEditor

mv assEditor.zip ..

cd ..
rm -rf release

echo "The file assEditor.zip has been created in $(pwd)"

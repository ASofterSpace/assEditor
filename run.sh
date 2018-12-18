#!/bin/bash

java -classpath "bin:emf/*" -Xms16m -Xmx1024m com.asofterspace.assEditor.Main "$@"

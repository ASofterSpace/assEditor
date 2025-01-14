#!/usr/bin/env bash

java -classpath "`dirname "$0"`/bin" -Xms16m -Xmx1024m -XX:+UseG1GC com.asofterspace.assEditor.AssEditor "$@" &

@echo off

start "A Softer Space Editor" javaw -classpath "%~dp0\bin" -Xms16m -Xmx1024m com.asofterspace.assEditor.Main %*

@echo off
@title Dump
set CLASSPATH=.;..\bin\*
java -server -Dwzpath=..\wz\ tools.WZStringDump strings
pause
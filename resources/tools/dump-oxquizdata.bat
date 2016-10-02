@echo off
@title Dump
set CLASSPATH=.;..\..\bin\*
java -server -Dwzpath=..\..\wz\ tools.wztosql.DumpOxQuizData
pause
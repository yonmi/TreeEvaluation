@echo off

set /p version=<%~dp0..\version.txt
java -Dnative.libpath=%~dp0..\lib\native -jar %~dp0..\lib\sis-jhdf5-h5ar-cli-%version%.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

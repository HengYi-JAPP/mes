@echo off

set SERVICE_NAME=mes-auto-print

set DIR=%cd%
set SRV=%DIR%\prunsrv.exe

%SRV% //DS//%SERVICE_NAME%

:end

@echo off

rem 设置服务
set SERVICE_NAME=mes-auto-printer

rem 设置路径
set BASE_DIR=%cd%
set SRV=%BASE_DIR%\windows\prunsrv.exe

rem 输出信息
echo SERVICE_NAME: %SERVICE_NAME%
echo prunsrv path: %SRV%

%SRV% //DS//%SERVICE_NAME%

:end
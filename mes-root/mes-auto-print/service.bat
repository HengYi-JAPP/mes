@echo off
setlocal

set SERVICE_NAME=MesAutoJappPrintService
set PR_INSTALL=%~dp0%prunsrv.exe
set PR_DESCRIPTION="mes-auto japp print service"

REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%~dp0%\
set PR_STDOUTPUT=%~dp0%\stdout.txt
set PR_STDERROR=%~dp0%\stderr.txt
set PR_LOGLEVEL=Debug

REM Path to java installation
set PR_JVM=%JAVA_HOME%\jre\bin\server\jvm.dll
set PR_CLASSPATH=mes-auto-print.jar

REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=Prunssrv
set PR_STARTMETHOD=prunsrvStartServer

REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=Prunssrv
set PR_STOPMETHOD=prunsrvStopServer
set PR_STOPTIMEOUT=120

REM JVM configuration
set PR_JVMMS=256
set PR_JVMMX=1024
set PR_JVMSS=4000

REM JVM options
set prunsrv_port=8080
set prunsrv_server=localhost
set PR_JVMOPTIONS=-Dprunsrv.port=%prunsrv_port%;-Dprunsrv.server=%prunsrv_server%

REM current file
set "SELF=%~dp0%service.bat"
REM current directory
set "CURRENT_DIR=%cd%"

REM start - This takes the input from installService and places it between x's
REM       - if there are none then you get xx as a null check
if "x%1x" == "xx" goto displayUsage
set SERVICE_CMD=%1
REM ahift moves to next field
shift
if "x%1x" == "xx" goto checkServiceCmd
:checkServiceCmd
if /i %SERVICE_CMD% == install goto doInstall
if /i %SERVICE_CMD% == remove goto doRemove
if /i %SERVICE_CMD% == uninstall goto doRemove
echo Unknown parameter "%SERVICE_CMD%"
:displayUsage
echo.
echo Usage: service.bat install/remove
goto end
:doRemove
echo Removing the service '%PR_INSTALL%' '%SERVICE_NAME%' ...
%PR_INSTALL% //DS//%SERVICE_NAME%
if not errorlevel 1 goto removed
echo Failed removing '%SERVICE_NAME%' service
goto end
:removed
echo The service '%SERVICE_NAME%' has been removed
goto end
:doInstall
echo Installing the service '%PR_INSTALL%' '%SERVICE_NAME%' ...
%PR_INSTALL% //IS//%SERVICE_NAME%
goto end
:end
echo Exiting service.bat ...
cd "%CURRENT_DIR%"
@echo off

set SERVICE_NAME=mes-auto-printer
set DISPLAY_NAME=mes-auto-printer
set MAIN_CLASS=com.hengyi.japp.mes.auto.print.Print

set DIR=%cd%
set SRV=%DIR%\prunsrv.exe
set JAVA_HOME=%DIR%\jdk
set CLASS_PATH=%DIR%\mes-auto-print.jar
set LOG_PATH=%DIR%\log

"%SRV%" //IS//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" "--Classpath=%CLASS_PATH%" "--Install=%SRV%" "--JavaHome=%JAVA_HOME%" --Startup=auto "--JvmOptions9=-Dmes.auto.print.path=%DIR%" "--StartPath=%DIR%" --StartMode=jvm "--StartClass=%MAIN_CLASS%" --StartMethod=start "--StopPath=%DIR%" --StopMode=jvm "--StopClass=%MAIN_CLASS%" --StopMethod=stop "--LogPath=%LOG_PATH%"

:end

@echo off

rem 设置服务
set SERVICE_NAME=mes-auto-printer
set DISPLAY_NAME=mes-auto-printer
set MAIN_CLASS=com.hengyi.japp.mes.auto.print.Print

rem 设置路径
set BASE_DIR=%cd%
set SRV=%BASE_DIR%\windows\prunsrv.exe
set J_HOME=%BASE_DIR%\jdk
set CLASS_PATH=%BASE_DIR%\mes-auto-print.jar
set CONFIG_PATH=%BASE_DIR%

rem 输出信息
echo SERVICE_NAME: %SERVICE_NAME%
echo JAVA_HOME: %J_HOME%
echo prunsrv path: %SRV%
echo CLASS_PATH: %CLASS_PATH%
echo MAIN_CLASS: %MAIN_CLASS%
echo CONFIG_FILE: %CONFIG_FILE%

rem 安装
"%SRV%" //IS//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" "--Classpath=%CLASS_PATH%" "--Install=%SRV%" "--JavaHome=%J_HOME%" --Startup=auto "--JvmOptions9=-Dmes.auto.print.path=%CONFIG_PATH%" "--StartPath=%BASE_DIR%" --StartMode=jvm --StartClass=%MAIN_CLASS% --StartMethod=start "--StopPath=%BASE_DIR%" --StopMode=jvm --StopClass=%MAIN_CLASS% --StopMethod=stop

:end
@echo off
title TP-JPA-Requetes
chcp 65001 >nul
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.16.101-hotspot"
set "MAVEN_OPTS=-Dfile.encoding=UTF-8"
cd /d "%~dp0"
echo ============================================================
echo  TP 5 - Requetes avancees avec JPA
echo  Salles disponibles, recherche multi-criteres, pagination
echo ============================================================
echo.
call mvn -B -q clean compile
if errorlevel 1 goto :echec
call mvn -B -q exec:java -Dexec.mainClass=com.example.App
if errorlevel 1 goto :echec
goto :fin
:echec
echo.
echo Echec de la compilation ou de l'execution.
:fin
echo.
echo ============================================================
echo  Fin de l'execution.
echo ============================================================
pause
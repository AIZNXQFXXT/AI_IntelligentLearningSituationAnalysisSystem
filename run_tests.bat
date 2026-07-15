@echo off
set JAVA_HOME=E:\tools\java\jdk-17
cd /d E:\WorkSpaces\AI-
call E:\Maven\apache-maven-3.9.16\bin\mvn.cmd test -pl backend-module -am -Dtest=SystemControllerTest,SystemServiceImplTest -DfailIfNoTests=false > E:\WorkSpaces\AI-\mvn_test.log 2>&1
echo EXIT=%ERRORLEVEL% >> E:\WorkSpaces\AI-\mvn_test.log

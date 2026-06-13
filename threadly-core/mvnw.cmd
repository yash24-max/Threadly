@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup batch script, version 3.3.2

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET PN=%__MVNW_ARG0_NAME__%
@SET SCRIPT_DIR=%~dp0

@SET WRAPPER_PROPERTIES=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties

@FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
  IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@SET DISTRIBUTION_NAME=%DISTRIBUTION_URL:~-27,-4%
@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
@SET MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_NAME%

@IF NOT EXIST "%MAVEN_HOME%" (
  MKDIR "%MAVEN_HOME%"
  ECHO Downloading Apache Maven from %DISTRIBUTION_URL%...
  powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%DISTRIBUTION_URL%', '%MAVEN_HOME%\maven.zip')"
  powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\maven.zip' -DestinationPath '%MAVEN_HOME%'"
  DEL "%MAVEN_HOME%\maven.zip"
)

@FOR /R "%MAVEN_HOME%" %%f IN (mvn.cmd) DO (
  SET MVN_EXEC=%%f
  GOTO run
)

:run
"%MVN_EXEC%" %*

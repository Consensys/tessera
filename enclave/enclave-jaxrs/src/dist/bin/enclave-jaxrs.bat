@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  enclave-jaxrs startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and ENCLAVE_JAXRS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Dtessera.cli.type=ENCLAVE"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\enclave-jaxrs-0.11-SNAPSHOT.jar;%APP_HOME%\lib\jaxrs-client-0.11-SNAPSHOT.jar;%APP_HOME%\lib\jersey-server-0.11-SNAPSHOT.jar;%APP_HOME%\lib\jaxrs-client-unixsocket-0.11-SNAPSHOT.jar;%APP_HOME%\lib\server-api-0.11-SNAPSHOT.jar;%APP_HOME%\lib\enclave-server-0.11-SNAPSHOT.jar;%APP_HOME%\lib\cli-api-0.11-SNAPSHOT.jar;%APP_HOME%\lib\server-utils-0.11-SNAPSHOT.jar;%APP_HOME%\lib\tessera-context-0.11-SNAPSHOT.jar;%APP_HOME%\lib\enclave-api-0.11-SNAPSHOT.jar;%APP_HOME%\lib\key-vault-api-0.11-SNAPSHOT.jar;%APP_HOME%\lib\security-0.11-SNAPSHOT.jar;%APP_HOME%\lib\config-0.11-SNAPSHOT.jar;%APP_HOME%\lib\encryption-jnacl-0.11-SNAPSHOT.jar;%APP_HOME%\lib\encryption-api-0.11-SNAPSHOT.jar;%APP_HOME%\lib\argon2-0.11-SNAPSHOT.jar;%APP_HOME%\lib\shared-0.11-SNAPSHOT.jar;%APP_HOME%\lib\logback-classic-1.2.3.jar;%APP_HOME%\lib\jul-to-slf4j-1.7.30.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.30.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\jaxb-runtime-2.3.3.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.3.jar;%APP_HOME%\lib\picocli-4.0.4.jar;%APP_HOME%\lib\jersey-bean-validation-2.27.jar;%APP_HOME%\lib\jersey-container-servlet-core-2.27.jar;%APP_HOME%\lib\jersey-server-2.27.jar;%APP_HOME%\lib\jersey-hk2-2.27.jar;%APP_HOME%\lib\jersey-media-json-processing-2.27.jar;%APP_HOME%\lib\jersey-media-moxy-2.27.jar;%APP_HOME%\lib\jersey-client-2.27.jar;%APP_HOME%\lib\jersey-media-jaxb-2.27.jar;%APP_HOME%\lib\jersey-common-2.27.jar;%APP_HOME%\lib\jersey-entity-filtering-2.27.jar;%APP_HOME%\lib\jakarta.ws.rs-api-2.1.6.jar;%APP_HOME%\lib\commons-lang3-3.7.jar;%APP_HOME%\lib\jakarta.json-1.1.6.jar;%APP_HOME%\lib\org.eclipse.persistence.moxy-2.7.7.jar;%APP_HOME%\lib\jetty-unixsocket-9.4.25.v20191220.jar;%APP_HOME%\lib\jnr-unixsocket-0.28.jar;%APP_HOME%\lib\argon2-jvm-2.5.jar;%APP_HOME%\lib\jasypt-1.9.3.jar;%APP_HOME%\lib\jetty-client-9.4.25.v20191220.jar;%APP_HOME%\lib\jetty-servlet-9.4.25.v20191220.jar;%APP_HOME%\lib\jetty-security-9.4.25.v20191220.jar;%APP_HOME%\lib\jetty-server-9.4.25.v20191220.jar;%APP_HOME%\lib\cryptacular-1.2.4.jar;%APP_HOME%\lib\jnacl-1.0.0.jar;%APP_HOME%\lib\bcpkix-jdk15on-1.64.jar;%APP_HOME%\lib\bcprov-jdk15on-1.64.jar;%APP_HOME%\lib\hk2-locator-2.5.0-b42.jar;%APP_HOME%\lib\hk2-api-2.5.0-b42.jar;%APP_HOME%\lib\hk2-utils-2.5.0-b42.jar;%APP_HOME%\lib\jakarta.inject-api-1.0.1.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.2.jar;%APP_HOME%\lib\jakarta.annotation-api-1.3.5.jar;%APP_HOME%\lib\jakarta.servlet-api-4.0.4.jar;%APP_HOME%\lib\jakarta.mail-1.6.5.jar;%APP_HOME%\lib\jsonp-jaxrs-1.1.6.jar;%APP_HOME%\lib\jakarta.el-3.0.3.jar;%APP_HOME%\lib\hibernate-validator-6.1.5.Final.jar;%APP_HOME%\lib\jakarta.validation-api-2.0.2.jar;%APP_HOME%\lib\logback-core-1.2.3.jar;%APP_HOME%\lib\jakarta.activation-1.2.2.jar;%APP_HOME%\lib\org.eclipse.persistence.core-2.7.7.jar;%APP_HOME%\lib\txw2-2.3.3.jar;%APP_HOME%\lib\istack-commons-runtime-3.0.11.jar;%APP_HOME%\lib\osgi-resource-locator-1.0.1.jar;%APP_HOME%\lib\aopalliance-repackaged-2.5.0-b42.jar;%APP_HOME%\lib\javassist-3.22.0-CR2.jar;%APP_HOME%\lib\jna-4.5.2.jar;%APP_HOME%\lib\org.eclipse.persistence.asm-2.7.7.jar;%APP_HOME%\lib\jetty-http-9.4.25.v20191220.jar;%APP_HOME%\lib\jetty-io-9.4.25.v20191220.jar;%APP_HOME%\lib\jboss-logging-3.3.2.Final.jar;%APP_HOME%\lib\classmate-1.3.4.jar;%APP_HOME%\lib\jnr-enxio-0.25.jar;%APP_HOME%\lib\jnr-posix-3.0.54.jar;%APP_HOME%\lib\jnr-ffi-2.1.12.jar;%APP_HOME%\lib\jnr-constants-0.9.15.jar;%APP_HOME%\lib\jetty-util-9.4.25.v20191220.jar;%APP_HOME%\lib\jffi-1.2.23.jar;%APP_HOME%\lib\jffi-1.2.23-native.jar;%APP_HOME%\lib\asm-commons-7.1.jar;%APP_HOME%\lib\asm-util-7.1.jar;%APP_HOME%\lib\asm-analysis-7.1.jar;%APP_HOME%\lib\asm-tree-7.1.jar;%APP_HOME%\lib\asm-7.1.jar;%APP_HOME%\lib\jnr-a64asm-1.0.0.jar;%APP_HOME%\lib\jnr-x86asm-1.0.2.jar


@rem Execute enclave-jaxrs
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %ENCLAVE_JAXRS_OPTS%  -classpath "%CLASSPATH%" com.quorum.tessera.enclave.rest.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable ENCLAVE_JAXRS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%ENCLAVE_JAXRS_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega

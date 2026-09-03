@echo off
setlocal
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)
echo Gradle 9.5.0 nao encontrado. Abra o projeto no Android Studio ou instale o Gradle 9.5.0.
exit /b 1

f:
cd F:\test
copy demo1.txt copy1.txt
@echo off
setlocal enabledelayedexpansion
set "file=copy1.txt"
set "outFile=temp1126.txt"
(for /f "tokens=*" %%i in (%file%) do (
  set s=%%i
  set s=!s:123=222!
  echo !s!
  )
)>%outFile%
./gradlew clean assembleRelease
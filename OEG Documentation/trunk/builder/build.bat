@echo off
set PATH=%PATH%;C:\Program Files\Java\jdk1.7.0_05\bin
rd /S /Q bin
md bin
md bin
xcopy /I /E /C /Y res bin
cls
javac -target 1.6 -source 1.6 -g -Xmaxerrs 0 -Xlint:none -d bin -classpath bin -O -sourcepath ../src ../src/client/Requests.java
jar cvfM bin.jar -C bin .
rd /S /Q bin
md bin
move bin.jar bin/bin.jar
jarsigner -keystore ".keystore" -storepass "71vZKY3DjrN40Uzty5eS922e4wWZsn76" -keypass "MKHjBLZuvm7pU8384MxAsLkfZsJg5Z8d" -digestalg "SHA1" -signedjar "bin/bin.signed.jar" "bin/bin.jar" "OEG"
copy bin\bin.signed.jar site\OEG.jar
pause

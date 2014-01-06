rem call "%VS100COMNTOOLS%vsvars32.bat"
rmdir /q /s work
md work
copy jni4net*.* work
copy C#\bin\Release\WindowsSpeech.dll work\WindowsSpeech.dll
proxygen.exe work\WindowsSpeech.dll -wd work
cd work
call build.cmd

copy WindowsSpeech.dll ..\..\lib
copy WindowsSpeech.j4n.dll ..\..\lib
copy WindowsSpeech.j4n.jar ..\..\lib

pause
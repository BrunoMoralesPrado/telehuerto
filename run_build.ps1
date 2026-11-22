$env:ANDROID_HOME = "C:\Users\al_ve\AppData\Local\Android\Sdk"
$env:ANDROID_USER_HOME = "C:\Users\al_ve\.android"
./gradlew.bat app:compileDebugKotlin --info > build_log.txt 2>&1

@echo off
echo ========================================================
echo Restoring google-services.json after AI Studio sync...
echo ========================================================

if not exist google-services.json (
    echo Error: google-services.json template not found in root!
    pause
    exit /b
)

copy /Y google-services.json app\google-services.json
git add google-services.json app\google-services.json
git commit -m "Restore google-services.json after AI Studio sync"
git push origin main

echo ========================================================
echo Done! Firebase config restored and successfully pushed!
echo ========================================================
pause

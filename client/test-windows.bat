@echo off
cd windows
set /P id="Type a platform and hit enter [d3d11|opengl|vulkan] : "
java -Xmx1G -Xms1G -jar ../test.jar --debug --dev-assets ../assets_dev --platform %id%
pause
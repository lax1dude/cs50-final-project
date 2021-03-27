#!/bin/sh
cd linux
java -Djava.library.path=. -Xmx1G -Xms1G -jar ../test.jar --debug --dev-assets ../assets_dev

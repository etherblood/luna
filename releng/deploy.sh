#!/bin/bash

GIT=$1
SERVER=$2
CLIENT=$3

cp ${GIT}/application-client/target/application-client-*[!s].jar ${CLIENT}/luna.jar
rsync -rc --delete ${GIT}/application-client/target/assets ${CLIENT}/
rsync -rc --delete ${GIT}/application-client/target/libs ${CLIENT}/
cp ${GIT}/releng/background.png /var/www/destrostudios/launcher/images/background_10.png
cp ${GIT}/releng/tile.png /var/www/destrostudios/launcher/images/tile_10.png
curl https://destrostudios.com:8080/apps/10/updateFiles
echo updated client files

cd ${SERVER}
pm2 stop luna 2> /dev/null
echo stopped server
cp ${GIT}/application-server/target/application-server-*[!s].jar ${SERVER}/luna.jar
rsync -rc --delete ${GIT}/application-server/target/libs ${SERVER}/
mv ${GIT}/releng/ecosystem.config.js ${SERVER}/ecosystem.config.js
echo updated server files
pm2 start
echo started server

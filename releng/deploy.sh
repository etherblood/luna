#!/bin/bash

GIT=$1
SERVER=$2
CLIENT=$3

cp ${GIT}/application-client/target/application-client-*[!s].jar ${CLIENT}/luna.jar
rsync -rc --delete ${GIT}/application-client/target/assets ${CLIENT}/
rsync -rc --delete ${GIT}/application-client/target/libs ${CLIENT}/
curl https://destrostudios.com:8080/apps/10/updateFiles
echo updated client files

cd ${SERVER}
bash stop.sh
echo stopped server
cp ${GIT}/application-server/target/application-server-*[!s].jar ${SERVER}/luna.jar
rsync -rc --delete ${GIT}/application-server/target/libs ${SERVER}/
#rsync -rc --delete ${GIT}/application-server/target/assets ${SERVER}/
echo updated server files
bash start.sh
echo started server
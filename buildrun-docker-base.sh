#!/bin/sh

clear;
./bin/fakeversion.sh
#./gradlew installDist bootRepackage

docker-compose -f ./docker/docker-compose-base.yml \
-f ./docker/compose/docker-compose-idam.yml \
down

docker-compose -f ./docker/docker-compose-base.yml \
-f ./docker/compose/docker-compose-idam.yml \
pull

docker-compose -f ./docker/docker-compose-base.yml \
-f ./docker/compose/docker-compose-idam.yml \
up --build

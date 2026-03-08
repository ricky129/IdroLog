#!/bin/bash
set -e
mvn clean package -pl idrolog-server -am
scp idrolog-server/target/app-shaded.jar myserver@192.168.1.252:~/docker/idrolog/idrolog-server/target/app-shaded.jar
ssh myserver@192.168.1.252 "cd ~/docker/idrolog/idrolog-server && docker compose down && docker compose up -d --build"
echo "Deploy completato."

continuous-online-hearing

To run in docker
```bash
./gradlew clean assemble

az acr login --name hmctspublic --subscription 1c4f0704-a29e-403d-b719-b90c34ef14c9
az acr login --name hmctsprivate --subscription 1c4f0704-a29e-403d-b719-b90c34ef14c9
docker-compose -f docker-compose-all.yml up --build
```
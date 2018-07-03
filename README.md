# Theming Service

## Run
Using sbt: 
```
sbt run
```

Run latest published image in Docker:
```
docker-compose up -d
```

Verify if service is up by going to http://localhost:9000/api/healthcheck

## Publish to Docker Hub 
1. make sure docker client is logged in to docker hub
    ```
    docker login
    ```
2. publish
    ```
    sbt docker:publish
    ```
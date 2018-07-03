# Theming Service

To run using sbt: 
```
sbt run
```

To run latest published image in Docker:
```
docker-compose up -d
```

To publish new latest Docker image:
1. make sure docker client is logged in to docker hub
    ```
    docker login
    ```
2. publish
    ```
    sbt docker:publish
    ```
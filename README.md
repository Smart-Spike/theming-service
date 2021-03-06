# Theming Service
[![Run Status](https://api.shippable.com/projects/5b3e2755fbb951070000329e/badge?branch=master)](https://app.shippable.com/github/Smart-Spike/theming-service)

## Run
Using sbt: 
```
sbt run
```

Run latest published image in Docker:
```
docker-compose up -d mysql 
# wait for db to initialize (a few seconds ususally)
docker-compose up -d theming-service
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
    
## Usage example
```http request
POST http://localhost:9000/api/login
Content-Type: application/json

{
    "email": "admin@feature-service.com",
    "password": "password123"
}

# Response: token 
```

```http request
GET http://localhost:9000/api/users/user-id/theme
Authorization: Bearer {token from login}

# Response: 
# {
#   "theme": "DARK",
#   "config": {
#     "font": "large"
#   }
# }
```
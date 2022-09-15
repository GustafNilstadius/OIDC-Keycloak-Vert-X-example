# OpenID Connect example demo with Keycloak and Vert.X

To launch your tests:
```shell script
mvn lean test
```

To package your application:
```shell script
mvn clean package
```

To run your application:
```shell script
java -jar target/[jar file] --conf conf/conf.json
```

## Keycloak

### Run Keycloak
```shell script
docker run -p 8989:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:19.0.2 start-dev
```


### Setup
1. Create realm called ```dev```.
2. Create client.
    1. Client type ```OpenID Connect```.
    2. Client ID ```dev-client```.
    3. Client authentication to ```on```.
    4. Rest is default.
    5. Create
3. Under ```Credentials``` tab, note the secret and put it in the config together with the Client ID.
4. Under access settings
    1. Set Root URL to ```http://localhost:8888```
    2. Set Valid redirect URI to ```/*```
    3. Set Home URL to ```http://localhost:8888/index.html```
    4. Save
5. Under Realm setting>Login, enable user registration.


# Network-group-switcher

An app which provides an api for changing network aliases in your home.
In order to have use for this app you need a pfsense or other router with similar function and set it up to listen to this api.
This enables you to easily change which computer/phone is in which region for watching streaming services or other content.
Some hardware (smart-tv, chromecast etc) does not have a built in vpn client. Therefore this also solves the problem for those devices.

This is an remade version of my old projects:
https://github.com/AdamBremholm/alias-switcher
https://github.com/AdamBremholm/alias-switcher-frontend

The main difference is that this project is build with microservices and has a more extensive api than the old version.
The microservices structure enables us to get more modularity and scalability.
The microservices in this project are:

  - Netflix Eureka Server - Spring boot app
  - Netflix Zuul Gateway - Spring boot app using kotlin
  - User-api - Spring boot app using kotlin
  - Host-api - Spring boot app using kotlin
  - Frontend - Vuejs project

### Installation

Java 11, kotlin 1.3.72, npm 6.14.5 and Gradle 6.4.1 are required to run the project.

```sh
$ gradle build
cd eureka/build/libs
java -jar eureka-0.0.1-SNAPSHOT.jar
(repeat for each spring boot project)
```
The eureka server should be started before the other microservices.
After all four backend services are started:

```sh
$ cd frontend
npm install
npm run serve
```

### Demo
Ports and other environmental variables can be changed in gradle.properties but requires a new gradle build.
You can access the frontend at http://localhost:8080
A demouser is provided with username demo, password: 123abc
Some hosts and aliases are also included to begin with.

You can login and drag and drop hosts to other aliases.

User-api and host-apis have swagger descriptions at:
http://localhost:80XX/swagger-ui.html#/
(allparams queryParam need to be removed when accessing the findWithQueryParams endpoints.)

You can also use this user to access the api with insomnia/postman
POST request to localhost:8081/login with
{
  "userName": "demo",
  "password": "123abc"
}
to get jwt token and set it to access the other services.
All backend services should be accessed through the gateway, so for example, to get the host api: http://localhost:8081/api/network/hosts
In a production environment host-api and user-api are not accessible to the outside world, only the gateway. (Could have been implemented with docker-compose or kubernetes but didn't have the time to set this up)
A database (for example Mariadb) can  be connected to the application to allow persisting data. In this demo the InMemory H2 database is being used but since the apis uses JPA, this is easy to change.


## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/flowcrmtutorial-1.0-SNAPSHOT.jar`

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). 
- `views` package in `src/main/java` contains the server-side Java views.
- `views` folder in `frontend/` contains the client-side JavaScript views.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Deploying using Docker

To build the Dockerized version of the project, run

```
mvn clean package -Pproduction
docker build . -t flowcrmtutorial:latest
```

Once the Docker image is correctly built, you can test it locally using

```
docker run -p 8080:8080 flowcrmtutorial:latest

```

## Accessing the application remotely

The project has already been deployed, and is running on the following link:


```
gcpro.azurewebsites.net

```

## Deployment to Azure is done using the following:

Install the Azure CLI
Add the Azure Plugin
Create a Production Build using:

mvn package -Pproduction

Configure The Application using

mvn azure-webapp:config

    <create> when asked to choose a Java SE Web App

    Linux when asked to choose an OS

    Java 11 for the Java version

    F1 for the pricingTier

    Enter 'Y' on your keyboard to confirm

Lastly deploy the application using

mvn azure-webapp:deploy

(Note, if you configuration step was already done previously, no need to do it again, just deploy the application using the last command)

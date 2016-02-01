# digitraffic-metadata

## Development

###Preconditions
1. Java 1.8 JDK
2. Maven
3. Git client


### Clone project to your computer

	$ git clone https://github.com/finnishtransportagency/digitraffic-metadata.git
	# Clones a repository to your computer

### Build project
 
	$ mvn clean install

### Configure project

To configure project copy ***src/main/resources/application-localhost.template*** -file as ***application-localhost.properties*** and configure it acording your environment.

### Runing the application

	$ mvn spring-boot:run -Dspring.profiles.active=localhost
	
Or build the JAR file with: 

	$ mvn clean package

 And run the JAR by typing:
 
 	$ java -Dspring.profiles.active=localhost -jar target/metadata-0.0.1-SNAPSHOT.jar
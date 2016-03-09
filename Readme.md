# digitraffic-metadata

## Development

###Preconditions
1. Java 1.8 JDK
2. Maven
3. Git client
4. Oracle 11g database


### Clone project to your computer

	$ git clone https://github.com/finnishtransportagency/digitraffic-metadata.git
	# Clones a repository to your computer

### Configure project

To configure project copy ***src/main/resources/application-localhost.template*** -file
as ***application-localhost.properties*** and configure it according to your environment.

### Configure Oracle JDBC driver

Download Oracle JDBC driver and add it to your local Maven repository.

	$ mvn install:install-file -DgroupId=oracle -DartifactId=ojdbc7 \
	  -Dversion=12.1.0.2 -Dpackaging=jar  -DgeneratePom=true -Dfile=ojdbc7-12.1.0.2.jar

**Or** add Maven repository that contains OJDBC-driver to project's pom.xml inside repositories-tag.


### Build project

	$ mvn clean install

### Running the application

	$ mvn spring-boot:run -Dspring.profiles.active=localhost
	
Or build the JAR file with: 

	$ mvn clean package

 And run the JAR by typing:
 
 	$ java -Dspring.profiles.active=localhost -jar target/metadata-0.0.1-SNAPSHOT.jar

### Misc

To compile Java-classes from wsdl:s run command

    $ mvn jaxb2:generate
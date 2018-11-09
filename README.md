# digitraffic-metadata

## Development

### Preconditions
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

If you want to disable some jobs, it can be done by adding following line to application.properties file

    # Disable jobs. Format: quartz.{jobClassName}.enabled = false
    quartz.CameraUpdateJob.enabled = false

### Run PosgresSQL server

See dbroad directory [README.md](dbroad/README.md)
### Configure Oracle JDBC driver

Download Oracle JDBC driver and add it to your local Maven repository.

    $ mvn install:install-file -DgroupId=com.oracle.jdbc -DartifactId=ojdbc7 \
      -Dversion=12.1.0.2 -Dpackaging=jar  -DgeneratePom=true -Dfile=ojdbc7-12.1.0.2.jar

**Or** add Maven repository that contains OJDBC-driver to project's pom.xml inside repositories-tag.


### Build project

Before building application with tests enabled install test db for it! (**See ci-db -project.**)

    $ mvn clean install
    
    # Or with out tests
    $ mvn clean install -DskipTests

### Running the application

    $ mvn spring-boot:run -Drun.profiles=localhost # localhost is default, you may leave it out :)

Or build the JAR file with:

    $ mvn clean package

 And run the JAR by typing:

    $ java -Dspring.profiles.active=localhost -jar target/metadata-0.0.1-SNAPSHOT.jar

### Misc commands

To compile Java-classes from wsdl:s run command

    $ mvn jaxb2:generate

Check for Maven dependency updates

    $ mvn versions:display-dependency-updates
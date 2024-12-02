# digitraffic-road

## Development

### Preconditions
1. Java 17 JDK
2. Maven
3. Git client

### Clone project to your computer

    $ git clone https://github.com/tmfg/digitraffic-metadata.git
    # Clones a repository to your computer

### Configure project

To configure project copy ***src/main/resources/application-localhost.template*** -file
as ***application-localhost.properties*** and configure it according to your environment.

If you want to disable some jobs, it can be done by adding following line to application.properties file

    # Disable jobs. Format: quartz.{jobClassName}.enabled = false
    quartz.CameraUpdateJob.enabled = false

### Run PosgresSQL server

See dbroad directory [README.md](dbroad/README.md)

### Build project

Before building application with tests enabled, start dbroad instance.
See [dbroad/README.md](dbroad/README.md).

    $ mvn clean install

    # Or with out tests
    $ mvn clean install -DskipTests

### Running the application

    $ mvn spring-boot:run -Dspring-boot.run.profiles=localhost-daemon

Or build the JAR file with:

    $ mvn clean package

 And run the JAR by typing:

    $ java -Dspring.profiles.active=localhost -jar target/metadata-0.0.1-SNAPSHOT.jar

### Running tests


Copy the template `application-default.properties.template` to `application-localhost.properties`.
Running may also require to specify the active Spring profile by setting _-Dspring.profiles.active=localhost_.

### Generate SchemaSpy schemas from the db with Maven

    $ mvn exec:exec@schemaspy

Generated schemas can be found at `dbroad/schemaspy/schema` -directory

Or with custom parameters.

    $ mvn exec:exec@schemaspy -Dexec.args="-o=/tmp/schema"

Or without Maven

    $ cd dbroad/schemaspy
    $ get-deps-and-run-schemaspy.sh [-o=/tmp/schema]

### Misc commands

To compile Java-classes from wsdl:s run command

    $ mvn jaxb2:generate

Check for Maven dependency updates

    $ mvn versions:display-dependency-updates

Update Maven dependencies

    $  mvn versions:use-latest-releases versions:update-properties


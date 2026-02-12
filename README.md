# digitraffic-road

## Development

### Preconditions
1. Java 21 JDK
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

Properties can be found at [`application-localhost.properties`](src/test/resources/application-localhost.properties).
Running may also require to specify the active Spring profile by setting _-Dspring.profiles.active=localhost_.

### Misc commands

#### To compile Java-classes from wsdl:s run command

    $ mvn jaxb2:generate

#### Check for Maven dependency updates

    $ mvn versions:display-dependency-updates

#### Update Maven dependencies

    $  mvn versions:use-latest-releases versions:update-properties

#### Run dependency check

    mvn -Pdepcheck

Report can be found at  [target/dependency-check-report.html](target/dependency-check-report.html)

Oneliner to run dependency check and open the report in default browser (MacOS):

    mvn -Pdepcheck; open target/dependency-check-report.html

### Known warnings

Running the daemon will produce the below warning on startup unless `logging.level.org.springframework.aop.framework.CglibAopProxy=ERROR` is configured. The warning is harmless because no AOP behavior is needed on the method `WebServiceGatewaySupport.afterPropertiesSet()`. It is caused by classes inheriting `AbstractLotjuMetadataClient`.

```
org.springframework.aop.framework.CglibAopProxy: Unable to proxy interface-implementing method [public final void org.springframework.ws.client.core.support.WebServiceGatewaySupport.afterPropertiesSet() throws java.lang.Exception] because it is marked as final, consider using interface-based JDK proxies instead.
```

//package fi.livi.digitraffic.tie.conf;
//
//import java.io.File;
//
//import javax.servlet.DispatcherType;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.boot.web.servlet.ServletContextInitializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.RestController;
//
//import net.bull.javamelody.MonitoredWithAnnotationPointcut;
//import net.bull.javamelody.MonitoringFilter;
//import net.bull.javamelody.MonitoringSpringAdvisor;
//import net.bull.javamelody.Parameter;
//import net.bull.javamelody.SessionListener;
//import net.bull.javamelody.SpringDataSourceBeanPostProcessor;
//
///**
// *
// * Original from https://github.com/javamelody/javamelody/blob/javamelody-core-1.63.0/javamelody-for-spring-boot/src/main/java/hello/JavaMelodyConfiguration.java
// *
// * @See https://github.com/javamelody/javamelody/wiki/UserGuide
// */
//@ConditionalOnProperty(name = "javamelody.enabled")
//@Configuration
//public class JavaMelodyConfiguration implements ServletContextInitializer {
//
//    private static final Logger log = LoggerFactory.getLogger(JavaMelodyConfiguration.class);
//
//    @Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//        servletContext.addListener(new SessionListener());
//    }
//
//    @Bean
//    public FilterRegistrationBean javaMelody(@Value("${javamelody.authorized-users}")
//                                             final String javamelodyAuthorizedUsers,
//                                             @Value("${javamelody.storage-directory}")
//                                             final String javamelodyStorageDirectory) {
//        final FilterRegistrationBean javaMelody = new FilterRegistrationBean();
//        final MonitoringFilter filter = new MonitoringFilter();
//        javaMelody.setFilter(filter);
//        javaMelody.setAsyncSupported(true);
//        javaMelody.setName("net.bull.javamelody.MetaMelody");
//        javaMelody.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
//
//        // see the list of parameters:
//        // https://github.com/javamelody/javamelody/wiki/UserGuide#6-optional-parameters
//        javaMelody.addInitParameter(Parameter.LOG.getCode(), Boolean.toString(true));
//
//        // to exclude images, css, fonts and js urls from the monitoring:
//        // javaMelody.addInitParameter(Parameter.URL_EXCLUDE_PATTERN.getCode(), "(/webjars/.*|/css/.*|/images/.*|/fonts/.*|/js/.*)");
//        // to add basic auth:
//        javaMelody.addInitParameter(Parameter.AUTHORIZED_USERS.getCode(), javamelodyAuthorizedUsers);
//
//        // to change the default storage directory:
//        String storageDirectory = null;
//        if (javamelodyStorageDirectory.startsWith("/")) {
//            storageDirectory = javamelodyStorageDirectory;
//        } else {
//            storageDirectory = new FileSystemResource("").getFile().getAbsolutePath() + File.separator + javamelodyStorageDirectory;
//        }
//        log.info("Set JavaMelody storage directory to {}", storageDirectory);
//        javaMelody.addInitParameter(Parameter.STORAGE_DIRECTORY.getCode(), storageDirectory);
//
//        // https://github.com/javamelody/javamelody/wiki/UserGuide#13-batch-jobs-if-quartz
//        javaMelody.addInitParameter(Parameter.QUARTZ_DEFAULT_LISTENER_DISABLED.getCode(), Boolean.toString(true));
//
//        javaMelody.addUrlPatterns("/*");
//        return javaMelody;
//    }
//
//    // monitoring of jdbc datasources:
//    @Bean
//    public SpringDataSourceBeanPostProcessor monitoringDataSourceBeanPostProcessor() {
//        final SpringDataSourceBeanPostProcessor processor = new SpringDataSourceBeanPostProcessor();
//        processor.setExcludedDatasources(null);
//        return processor;
//    }
//
//    // monitoring of beans or methods having @MonitoredWithSpring:
//    @Bean
//    public MonitoringSpringAdvisor monitoringAdvisor() {
//        final MonitoringSpringAdvisor interceptor = new MonitoringSpringAdvisor();
//        interceptor.setPointcut(new MonitoredWithAnnotationPointcut());
//        return interceptor;
//    }
//
//    // monitoring of all services and controllers (even without having @MonitoredWithSpring):
//    @Bean
//    public MonitoringSpringAdvisor springServiceMonitoringAdvisor() {
//        final MonitoringSpringAdvisor interceptor = new MonitoringSpringAdvisor();
//        interceptor.setPointcut(new AnnotationMatchingPointcut(Service.class));
//        return interceptor;
//    }
//
//    @Bean
//    public MonitoringSpringAdvisor springControllerMonitoringAdvisor() {
//        final MonitoringSpringAdvisor interceptor = new MonitoringSpringAdvisor();
//        interceptor.setPointcut(new AnnotationMatchingPointcut(Controller.class));
//        return interceptor;
//    }
//
//    @Bean
//    public MonitoringSpringAdvisor springRestControllerMonitoringAdvisor() {
//        final MonitoringSpringAdvisor interceptor = new MonitoringSpringAdvisor();
//        interceptor.setPointcut(new AnnotationMatchingPointcut(RestController.class));
//        return interceptor;
//    }
//}

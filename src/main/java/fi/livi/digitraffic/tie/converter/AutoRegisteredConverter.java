package fi.livi.digitraffic.tie.converter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

public abstract class AutoRegisteredConverter<S, T> implements Converter<S, T> {

    @Autowired
    @Qualifier("conversionService")
    private GenericConversionService conversionService;

    @SuppressWarnings("unused")
    @PostConstruct
    private void register() {
        conversionService.addConverter(this);
    }
}
package fi.livi.digitraffic.tie.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    Environment env;

    @Autowired
    MessageSource messageSource;

    @Override
    public String getMessage(String code) {
        return getMessage(code, (Object[])null);
    }

    @Override
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
        return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }

    @Override
    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    protected String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

}

package fi.livi.digitraffic.tie.service;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOG = Logger.getLogger(MessageServiceImpl.class);

    private final MessageSource messageSource;

    @Autowired
    public MessageServiceImpl(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(final String code) {
        return getMessage(code, (Object[])null);
    }

    @Override
    public String getMessage(final String code, final Object[] args) {
        return getMessage(code, args, null, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(final String code, final String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(final MessageSourceResolvable resolvable) {
        return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }

    @Override
    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    protected String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}

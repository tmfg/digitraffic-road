package fi.livi.digitraffic.tie.metadata.service;

import java.util.Locale;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Simplification of {@link org.springframework.context.MessageSource}.
 * @see org.springframework.context.MessageSource
 */
public interface MessageService {

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     * or {@code null} if none.
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     * @see org.springframework.context.MessageSource
     */
    String getMessage(String code);

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Array of arguments that will be filled in for params within
     * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     * or {@code null} if none.
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     * @see org.springframework.context.MessageSource
     */
    String getMessage(String code, Object[] args);

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     * this class are encouraged to base message names on the relevant fully
     * qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param defaultMessage String to return if the lookup fails
     * @return the resolved message if the lookup was successful;
     * otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     * @see org.springframework.context.MessageSource
     */
    String getMessage(String code, String defaultMessage);

    /**
     * Try to resolve the message. Return default message if no message was found.
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     * this class are encouraged to base message names on the relevant fully
     * qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args array of arguments that will be filled in for params within
     * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     * or {@code null} if none.
     * @param defaultMessage String to return if the lookup fails
     * @return the resolved message if the lookup was successful;
     * otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     * @see org.springframework.context.MessageSource
     */
    String getMessage(String code, Object[] args, String defaultMessage);

    /**
     * Try to resolve the message using all the attributes contained within the
     * {@code MessageSourceResolvable} argument that was passed in.
     * <p>NOTE: We must throw a {@code NoSuchMessageException} on this method
     * since at the time of calling this method we aren't able to determine if the
     * {@code defaultMessage} property of the resolvable is null or not.
     * @param resolvable value object storing attributes required to properly resolve a message
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     * @see org.springframework.context.MessageSource
     */
    String getMessage(MessageSourceResolvable resolvable);

    Locale getLocale();
}

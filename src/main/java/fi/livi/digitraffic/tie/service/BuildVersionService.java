package fi.livi.digitraffic.tie.service;

/**
 * Application build information.
 */
public interface BuildVersionService {

    /**
     * Return app's base version
     * @return version
     */
    String getAppVersion();

    /**
     * Return app's build revision
     * @return revision
     */
    String getAppBuildRevision();

    /**
     * Returns app's base version + build revision
     * @return  version + revision
     */
    String getAppFullVersion();
}

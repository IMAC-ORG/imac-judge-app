package co.za.imac.judge.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SettingUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(SettingUtils.class);

    private static final String DEFAULT_APPLICATION_CONFIG_PATH = "/var/opt/judge";
    private static String APPLICATION_CONFIG_PATH = DEFAULT_APPLICATION_CONFIG_PATH;

    public static String getApplicationConfigPath() {
        return (APPLICATION_CONFIG_PATH);
    }

    public static String setApplicationConfigPath(String appConfigPath) {
        APPLICATION_CONFIG_PATH = appConfigPath;
        return (APPLICATION_CONFIG_PATH);
    }

}

package org.oleg.fcs.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Configuration {

    private final Log log = LogFactory.getLog(Configuration.class);

    private Properties properties;

    public Configuration(Properties properties) {
        this.properties = properties != null ? properties : new Properties();
    }

    public String getString(String name) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            throw new ConfigurationException("Missing configuration parameter '" + name + "'.");
        }

        return prop;
    }

    public String getString(String name, String defaultValue) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            log.warn("Missing configuration parameter '" + name + "'. Default value was used: " + defaultValue);
            return defaultValue;
        } else {
            return prop;
        }

    }

    public boolean getBoolean(String name) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            throw new ConfigurationException("Missing configuration parameter '" + name + "'.");
        }

        return Boolean.parseBoolean(prop);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            log.warn("Missing configuration parameter '" + name + "'. Default value was used: " + defaultValue);
            return defaultValue;
        } else {
            return Boolean.parseBoolean(prop);
        }

    }

    public Integer getInteger(String name) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            throw new ConfigurationException("Missing configuration parameter '" + name + "'.");
        }

        return Integer.valueOf(prop);
    }

    public Integer getInteger(String name, Integer defaultValue) {
        String prop = properties.getProperty(name);

        if (prop == null) {
            log.warn("Missing configuration parameter '" + name + "'. Default value was used: " + defaultValue);
            return defaultValue;
        } else {
            return Integer.valueOf(prop);
        }
    }

    public String[] getStringArray(String name) {
        return getStringArray(name, null);
    }

    public String[] getStringArray(String name, String defaultValue) {
        String prop = properties.getProperty(name);

        if (prop == null && defaultValue == null) {
            throw new ConfigurationException("Missing configuration parameter '" + name + "'.");
        } else if (prop == null) {
            log.warn("Missing configuration parameter '" + name + "'. Default value was used: " + defaultValue);
            return splitArray(defaultValue);
        } else {
            return splitArray(prop);
        }
    }

    private static String[] splitArray(String s) {
        String[] arr = s.split(",");
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
        }

        return arr;
    }

    public boolean isPresent(String name) {
        return properties.get(name) != null;
    }
}

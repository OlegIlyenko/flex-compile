package org.oleg.fcs.util;

import org.oleg.fcs.helper.Configuration;
import org.oleg.fcs.helper.ConfigurationException;
import org.oleg.fcs.server.ConfigProperties;
import org.oleg.fcs.Constants;

import java.util.Map;
import java.util.HashMap;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class FlexUtil {

    public static void loadLibs(Configuration config) {
        String[] libs = config.getStringArray(ConfigProperties.SERVER_LIBPATH, Constants.DEFAULT_LIBPATH);
        String flexHome = getFlexSDK(config);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("flexHome", flexHome);

        for (String lib : libs) {
            ClassLoaderUtil.loadLibraries(lib, placeholders);
        }
    }

    public static String getFlexSDK(Configuration configuration) {
        String flexSdk = configuration.getString(ConfigProperties.FLEX_HOME, null);

        if (flexSdk == null || flexSdk.trim().equals("")) {
            for (String env : Constants.FLEX_SDK_ENV_VARS) {
                flexSdk = System.getenv(env);
                if (flexSdk != null && flexSdk.trim().equals("")) {
                    break;
                }
            }
        }

        if (flexSdk == null || flexSdk.trim().equals("")) {
            throw new ConfigurationException("Can't find path to the flex SDK. You can provide it through server.properties file with key '" + ConfigProperties.FLEX_HOME + "' or by setting environment variable: " + Constants.FLEX_SDK_ENV_VARS);
        }

        return flexSdk;
    }

}

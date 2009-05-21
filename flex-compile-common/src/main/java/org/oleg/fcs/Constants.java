package org.oleg.fcs;

import java.util.Arrays;
import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public interface Constants {

    int DEFAULT_PORT = 2233;

    String DEFAULT_PROJECT_FILE_NAME = "flex-project.xml";

    String DEFAULT_FLEX_CONFIG_FILE_NAME = "flex-config.xml";

    String DEFAUT_PROPERTIES_FILE = "server.properties";

    String DEFAULT_LIBPATH = "${flexHome}/lib/*.jar";

    String DEFAULT_FLEX_FONTS = "winFonts.ser";

    int DEFAULR_LOG_VIEWER_WIDTH = 900;

    int DEFAULR_LOG_VIEWER_HEIGHT = 900;

    String CLIENT_DEFAULT_HOST = "localhost";

    List<String> FLEX_SDK_ENV_VARS = Arrays.asList("FLEX_SDK", "FLEX_HOME");

    String[] DEFAULT_LOCALE = new String[] {"en_US"};

    interface ComponentFolders {
        // Project dirs
        String SRC_DIR = "src";
        String MXML_DIR = "mxml";
        String LIB_DIR = "lib";
        String CSS_DIR = "css";

        //Output dirs
        String SWC_TARGET_DIR = "libraries";
        String MODULE_TARGET_DIR = "modules";
        String CSS_TARGET_DIR = "css";
        String DEFAULT_TARGET_DIR = "target";
    }

}

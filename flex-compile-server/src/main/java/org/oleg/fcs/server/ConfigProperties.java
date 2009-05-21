package org.oleg.fcs.server;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public interface ConfigProperties {
    /**
     *
     */
    String SERVER_PORT = "fcs.server.port";

    /**
     * Where to search for the JARs
     * and ${flexHome} can be used
     */
    String SERVER_LIBPATH = "fcs.server.libpath";

    /**
     * Flex home dir path
     */
    String FLEX_HOME = "fcs.flex.flexHome";

    String FLEX_FONTS = "fcs.flex.flexFonts";

    String FLEX_ADDITIONAL_LIBS = "fcs.flex.additionalLibs";

    // --- UI

    String SHOW_TRAY_NOTIFICATIONS = "fcs.showTrayNotifications";

    String SHOW_COMPONENT_NOTIFICATIONS = "fcs.showComponentNotifications";

    String SHOW_PROJECT_NOTIFICATIONS = "fcs.showProjectNotifications";

    String SHOW_PROGRESS = "fcs.showProgress";

    String LOG_VIEWER_WIDTH = "fcs.logViewer.width";

    String LOG_VIEWER_HEIGHT = "fcs.logViewer.height";
}

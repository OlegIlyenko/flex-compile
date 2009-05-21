package org.oleg.fcs.runner;

import org.oleg.fcs.Constants;
import org.oleg.fcs.project.dao.impl.XmlProjectDaoImpl;
import org.oleg.fcs.event.EventDispatcher;
import org.oleg.fcs.util.ClassLoaderUtil;
import org.oleg.fcs.util.FlexUtil;
import org.oleg.fcs.helper.Configuration;
import org.oleg.fcs.server.CompillerServer;
import org.oleg.fcs.server.ConfigProperties;
import org.oleg.fcs.compiler.FlexCompilerImpl;
import org.oleg.fcs.compiler.listener.TrayListener;
import org.oleg.fcs.compiler.listener.LogListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompilationServer {

    private static final Log log = LogFactory.getLog(CompilationServer.class);

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");

        String propertiesFileName = null;
        String command = null;

        if (args.length == 1) {
            if (args[0] != null && args[0].equals("-help")) {
                System.out.println("Usage:");
                System.out.println("java -jar flex-compile-server.jar [-p=<PATH_TO_PROPERTIES>]");
                return;
            }

            propertiesFileName = args[0].split("=")[1];
        }

        if (propertiesFileName == null) {
            propertiesFileName = Constants.DEFAUT_PROPERTIES_FILE;
        }

        log.info("Starting server...");

        Properties props = new Properties();

        File cFile = new File(propertiesFileName);
        if (cFile.exists()) {
            log.info("Using configuration file " + cFile.getAbsolutePath());
            props.load(new FileInputStream(cFile));
        }

        Configuration configuration = new Configuration(props);

        FlexUtil.loadLibs(configuration);
        setupServer(configuration);
    }



    private static void setupServer(Configuration configuration) {
        EventDispatcher eventDispatcher = new EventDispatcher();
        eventDispatcher.start();

        TrayListener trayListener = new TrayListener(configuration);
        eventDispatcher.addEventListener(trayListener);
        eventDispatcher.addEventListener(new LogListener());

        FlexCompilerImpl compiller = new FlexCompilerImpl(configuration, eventDispatcher.getEventQueue(), new XmlProjectDaoImpl());

        trayListener.setFlexCompiler(compiller);

        CompillerServer server = new CompillerServer(compiller, configuration);

        new Thread(server, "Server listener thread").start();
    }

}

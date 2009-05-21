package org.oleg.fcs.client;

import org.oleg.fcs.Constants;
import org.oleg.fcs.event.Event;
import org.oleg.fcs.project.dao.impl.XmlProjectDaoImpl;
import org.oleg.fcs.compiler.FlexCompilerImpl;
import org.oleg.fcs.util.FlexUtil;
import org.oleg.fcs.helper.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.util.LinkedList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class LocalCompilerStarter {

    private static final Log log = LogFactory.getLog(LocalCompilerStarter.class);

    FlexCompilerImpl compiller;

    public LocalCompilerStarter(String propertiesFileName) throws IOException {
        if (propertiesFileName == null) {
            propertiesFileName = Constants.DEFAUT_PROPERTIES_FILE;
        }
        
        Properties props = new Properties();

        File cFile = new File(propertiesFileName);
        if (cFile.exists()) {
            log.info("Using configuration file " + cFile.getAbsolutePath());
            props.load(new FileInputStream(cFile));
        }

        init(props);
    }

    public LocalCompilerStarter(Properties propertiesFileName) throws IOException {
        init(propertiesFileName);
    }

    protected void init(Properties properties) {
        log.info("Initializing local compiler...");

        Configuration configuration = new Configuration(properties);

        FlexUtil.loadLibs(configuration);

        compiller = new FlexCompilerImpl(configuration, new LinkedList<Event>(), new XmlProjectDaoImpl());
    }

    public FlexCompilerImpl getCompiller() {
        return compiller;
    }
}

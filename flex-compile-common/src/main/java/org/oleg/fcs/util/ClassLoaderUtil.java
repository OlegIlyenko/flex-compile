package org.oleg.fcs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
@SuppressWarnings("unchecked")
public abstract class ClassLoaderUtil {

    private static final Log log = LogFactory.getLog(ClassLoaderUtil.class);

//    public static ClassLoader CLASS_LOADER;

    public static ClassLoader ORIGINAL_CLASS_LOADER;

    /**
     * Loads libraries from the <code>path</code>.<br>
     * You can use * (star) as wildcard of set of provided placeholders in path.
     *
     * @param path where to search
     * @param placeholders they will be fetched in path
     */
    public static void loadLibraries(String path, Map<String, String> placeholders) {
        String workingPath = path;
        for (String ph : placeholders.keySet()) {
            workingPath = workingPath.replaceAll("\\$\\{" + ph + "\\}", placeholders.get(ph));
        }

        File f = new File(workingPath);
        String name = f.getName();
        Pattern filePath = Pattern.compile(name.replaceAll("\\*", ".*"));
        File pathDir = f.getParentFile();

        try {
            List<URL> filesToProcess = new ArrayList<URL>();
            if (pathDir.exists()) {
                for (File fInDir : pathDir.listFiles()) {
                    if (filePath.matcher(fInDir.getName()).matches()) {
                        filesToProcess.add(fInDir.toURI().toURL());
                    }
                }
            }

//            if (CLASS_LOADER == null) {
//                CLASS_LOADER = ClassLoaderUtil.class.getClassLoader();
//            }

//            URLClassLoader cl = new URLClassLoader(filesToProcess.toArray(new URL[filesToProcess.size()]), CLASS_LOADER);
//            CLASS_LOADER = cl;


            // Add new Jars to the system classpath !!! (little hack, but works)
            URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;
            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            for (URL u : filesToProcess) {
                method.invoke(sysloader, u);
            }

            log.debug("Loaded libraries (in path " + path + "): " + filesToProcess);
        } catch (Throwable e) {
            log.error("Error during loading lib in path " + path, e);
        }

    }

//    public static void changeContextClassLoader() {
//        if (CLASS_LOADER != null) {
//            ORIGINAL_CLASS_LOADER = Thread.currentThread().getContextClassLoader();
//            Thread.currentThread().setContextClassLoader(CLASS_LOADER);
//        }
//    }
//
//    public static void restoreContextClassLoader() {
//        if (ORIGINAL_CLASS_LOADER != null) {
//            Thread.currentThread().setContextClassLoader(ORIGINAL_CLASS_LOADER);
//        }
//    }
//
//    public static Class loadClass(String name) throws ClassNotFoundException {
//        if (CLASS_LOADER != null) {
//            return CLASS_LOADER.loadClass(name);
//        } else {
//            throw new IllegalStateException("No custom class loader !!!");
//        }
//    }
}

package org.oleg.fcs.client;

import org.oleg.fcs.Constants;
import org.oleg.fcs.api.CompilationResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompilationClient {

    private static final Log log = LogFactory.getLog(CompilationClient.class);

    public static void main(String[] args) {

        if ((args.length == 1 && args[0].endsWith("-help"))) {
            System.out.println("Usage:");
            System.out.println("java -jar flex-compile-client.jar [-host=<SERVER_HOST>] [-port=<SERVER_PORT>] [-config=<COMPILATION_PROPS_FILE>] [-application=<TARGET_BASE>] [-target=<TARGET_DIR>] [projectFileOrDir]");
            return;
        }

        String host = Constants.CLIENT_DEFAULT_HOST;
        int port = Constants.DEFAULT_PORT;
        String props = null;
        String application = null;
        String target = null;

        List<String> compileArgs = new ArrayList<String>();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                String[] a = arg.split("=");

                if (a[0].equals("-host")) {
                    host = a[1];
                } else if (a[0].equals("-port")) {
                    port = Integer.parseInt(a[1]);
                } else if (a[0].equals("-config")) {
                    props = a[1];
                } else if (a[0].equals("-application")) {
                    application = a[1];
                } else if (a[0].equals("-target")) {
                    target = a[1];
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }

            } else {
                compileArgs.add(arg);
            }
        }

        if (compileArgs.size() > 1) {
            throw new IllegalStateException("Invalid parameters. See help (-help)");
        }

        FlexCompilerSocketImpl compillerSocket = null;

        File projectFile = null;
        if (compileArgs.size() == 0) {
            projectFile = new File(Constants.DEFAULT_PROJECT_FILE_NAME);
        } else {
            File f = new File(compileArgs.get(0));
            if (f.isDirectory()) {
                projectFile = new File(f, Constants.DEFAULT_PROJECT_FILE_NAME);
            } else if (f.isFile()) {
                projectFile = f;
            }
        }

        if (target == null) {
            target = new File(projectFile.getParent(), Constants.ComponentFolders.DEFAULT_TARGET_DIR).getAbsolutePath();
        }

        if (new File(target).mkdirs()) {
            log.info("TargetFilder was created at: " + target);
        }

        if (projectFile == null || !projectFile.exists()) {
            throw new IllegalArgumentException("Can't find project file. You shld provide it or use default file name: " + Constants.DEFAULT_PROJECT_FILE_NAME);
        }

        List<CompilationResults> results = null;
        try {
            compillerSocket = new FlexCompilerSocketImpl(host, port);
            results = compillerSocket.compile(application, projectFile, target);
        } catch (ConnectionException e) {
            String message = "Unable to connect to the compilation server. Building project localy (this can be much slower). It's recommended to use server.";
            log.warn(message);
            System.err.println(message);

            LocalCompilerStarter localCompilerStarter;
            try {
                localCompilerStarter = new LocalCompilerStarter(props);
            } catch (IOException e1) {
                throw new RuntimeException(e);
            }

            results = localCompilerStarter.getCompiller().compile(application, projectFile, target);
        } finally {
            if (compillerSocket != null) {
                compillerSocket.closeConnection();
            }
        }

        for (CompilationResults cr : results) {
            System.out.println(cr.toString());
        }

    }

}

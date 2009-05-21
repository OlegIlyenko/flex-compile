package org.oleg.maven2.plugin.flex;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Contributor;
import org.oleg.fcs.client.FlexCompilerSocketImpl;
import org.oleg.fcs.client.ConnectionException;
import org.oleg.fcs.client.LocalCompilerStarter;
import org.oleg.fcs.Constants;
import org.oleg.fcs.api.CompilationResults;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;
import java.io.*;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 * @goal flex-compile
 */
public class FlexCompileMojo extends AbstractMojo {


    /**
     * the path to the war temporary target directory
     *
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}/"
     */
    private String warTargetDirectory;

    /**
     * Path relative to the web-app root
     *
     * @parameter default-value="/flex-bin"
     */
    private String targetDirectory;

    /**
     * @parameter
     */
    private String serverHost;


    /**
     * @parameter
     */
    private Integer serverPort;

    /**
     * @parameter default-value="${basedir}/src/main/flex/"
     */
    private String projectLocation;

    /**
     * @parameter
     */
    private Properties compilerProperties;

    /**
     * @parameter default-value="${basedir}/flex-compile.properties"
     */
    private String compilerPropertiesFile;

    /**
   * POM
   *
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Flex project compilation started...");

        String host = serverHost != null && !serverHost.trim().equals("") ? serverHost : Constants.CLIENT_DEFAULT_HOST;
        int port = serverPort != null ? serverPort : Constants.DEFAULT_PORT;

        FlexCompilerSocketImpl compillerSocket = null;

        File projectFile = null;
        File f = new File(projectLocation);
        if (f.isDirectory()) {
            projectFile = new File(f, Constants.DEFAULT_PROJECT_FILE_NAME);
        } else if (f.isFile()) {
            projectFile = f;
        }

        if (projectFile == null || !projectFile.exists()) {
            getLog().warn("Can't find flex project file. You shld provide it or use default file name: " + Constants.DEFAULT_PROJECT_FILE_NAME + " in the default folder 'src/main/flex/'");
            return;
        }

        File target = new File(warTargetDirectory, targetDirectory);
        target.mkdirs();

        List<CompilationResults> results = null;
        try {
            compillerSocket = new FlexCompilerSocketImpl(host, port);
            results = compillerSocket.compile(null, projectFile, target.getAbsolutePath());
        } catch (ConnectionException e) {
            String message = "Unable to connect to the compilation server. Building project localy (this can be much slower). It's recommended to use server.";
            getLog().warn(message);

            Properties props = new Properties();
            if (compilerProperties != null) {
                props = compilerProperties;
            } else if (new File(compilerPropertiesFile).exists()) {
                try {
                    props.load(new FileReader(new File(compilerPropertiesFile)));
                } catch (IOException e1) {
                    getLog().error(e1.getMessage());
                    throw new MojoFailureException(e1.getMessage(), e1);
                }
            }

            LocalCompilerStarter localCompilerStarter;
            try {
                localCompilerStarter = new LocalCompilerStarter(props);
            } catch (IOException e1) {
                throw new RuntimeException(e);
            }

            results = localCompilerStarter.getCompiller().compile(null, projectFile, target.getAbsolutePath());
        } finally {
            if (compillerSocket != null) {
                compillerSocket.closeConnection();
            }
        }


        getLog().info("Flex compiler output:");
        boolean hasErrors = false;
        for (CompilationResults cr : results) {
            getLog().info(cr.toString());
            if (cr.isHasErrors()) {
                hasErrors = true;
            }
        }

        if (hasErrors) {
            String errorMessage = "Flex sources contain compilation errors. See flex compiler output above.";
            getLog().error(errorMessage);
            throw new MojoFailureException(errorMessage);
        }
    }
}


package org.oleg.fcs.web.filter;

import org.oleg.fcs.client.FlexCompilerSocketImpl;
import org.oleg.fcs.client.LocalCompilerStarter;
import org.oleg.fcs.api.FlexCompiler;
import org.oleg.fcs.api.CompilationResults;
import org.oleg.fcs.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class DynamicFlexCompilationFilter implements Filter {

    private static final Log log = LogFactory.getLog(DynamicFlexCompilationFilter.class);

    public static final DateFormat SDF = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");

    private interface InitParams {
        String PORT = "port";
        String HOST = "host";

        String TARGET_DIR = "targetDir";

        String FLEX_PROJECT = "flexProject";

        String FILTERRESOURCE_REGEXP = "filterResourceRegexp";

        String COMPILER_PROPERTIES = "compilerProperties";
        String COMPILER_PROPERTIES_FILE = "compilerPropertiesFile";
    }

    private FilterConfig filterConfig;

    private FlexCompiler compiler;

    private File flexProject;

    private File targetDir;

    private Pattern filterResource;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        String host = Constants.CLIENT_DEFAULT_HOST;
        int port = Constants.DEFAULT_PORT;

        if (filterConfig.getInitParameter(InitParams.HOST) != null) {
            host = filterConfig.getInitParameter(InitParams.HOST);
        }
        if (filterConfig.getInitParameter(InitParams.PORT) != null) {
            port = Integer.valueOf(filterConfig.getInitParameter(InitParams.PORT));
        }

        FlexCompiler c = null;

        try {
            c = new FlexCompilerSocketImpl(host, port);
        } catch (Exception e) {
            String message = "Unable to connect to the compilation server. Building project localy (this can be much slower). It's recommended to use server.";
            log.warn(message);
            System.err.println(message);

            Properties cp = new Properties();

            try {
                String compilerProperties = filterConfig.getInitParameter(InitParams.COMPILER_PROPERTIES);
                String compilerPropertiesFile = filterConfig.getInitParameter(InitParams.COMPILER_PROPERTIES_FILE);
                if (compilerProperties != null && !compilerProperties.trim().equals("")) {
                    cp.load(new StringReader(compilerProperties));
                } else if (compilerPropertiesFile != null && !compilerPropertiesFile.trim().equals("")) {
                    cp.load(new FileInputStream(resolveFile(compilerPropertiesFile)));
                }

                LocalCompilerStarter localCompilerStarter = new LocalCompilerStarter(cp);
                c = localCompilerStarter.getCompiller();
            } catch (IOException e1) {
                throw new ServletException(e);
            }
        }

        compiler = c;

        String p = filterConfig.getInitParameter(InitParams.FLEX_PROJECT);
        if (p == null || p.trim().equals("")) {
            throw new IllegalArgumentException("Please, set the project dir or file you want to compile");
        } else {
            File f = resolveFile(p);
            if (f.isDirectory()) {
                flexProject = new File(f, Constants.DEFAULT_PROJECT_FILE_NAME);
            } else if (f.isFile()) {
                flexProject = f;
            }
        }

        String t = filterConfig.getInitParameter(InitParams.TARGET_DIR);
        if (t == null || t.trim().equals("")) {
            throw new IllegalArgumentException("Please, set the target dir where you want to compile project");
        } else {
            targetDir = resolveFile(t);
        }

        filterResource = Pattern.compile(filterConfig.getInitParameter(InitParams.FILTERRESOURCE_REGEXP));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String currentPath = ((HttpServletRequest) request).getServletPath();

        if (!filterResource.matcher(currentPath).matches()) {
            chain.doFilter(request, response);
            return;
        }

        System.out.println("Flex compiler: Starting...");

        if (currentPath.indexOf("/") != -1) {
            currentPath = currentPath.substring(currentPath.lastIndexOf("/") + 1);
        }

        String currentBaseName = currentPath.substring(0, currentPath.indexOf("."));

        List<CompilationResults> results = compiler.compile(currentBaseName, flexProject, targetDir.getAbsolutePath());

        boolean hasErrors = false;
        for (CompilationResults cr : results) {
            if (cr.isHasErrors()) {
                hasErrors = true;
            }
        }

        StringBuilder prot = new StringBuilder();
        for (CompilationResults cr : results) {
            System.out.println(cr.toString());
            prot.append(cr.toString());
        }

        if (hasErrors) {
            throw new IllegalStateException(prot.toString());
        } else {
            giveResourceDirectly(request, response);
        }
    }

    protected File resolveFile(String path) {
        File testFile = new File(path);

        if (testFile.isAbsolute()) {
            return testFile;
        } else {
            return new File(filterConfig.getServletContext().getRealPath("/"), path);
        }
    }

    protected void giveResourceDirectly(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Expires", " Mon, 20 Dec 1998 01:00:00 GMT");
        resp.setHeader("Last-Modified", SDF.format(new Date()) + " GMT");
        resp.setHeader("Cache-Control", "no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");

        long t = System.currentTimeMillis();
        File f = new File(httpRequest.getSession().getServletContext().getRealPath(httpRequest.getServletPath()));
        OutputStream out = resp.getOutputStream();
        InputStream in = new FileInputStream(f);

        byte[] buf = new byte[100000];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }

        out.flush();
        out.close();
    }

    public void destroy() {
    }
}

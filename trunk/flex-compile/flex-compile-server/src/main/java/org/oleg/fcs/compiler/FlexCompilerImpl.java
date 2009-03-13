package org.oleg.fcs.compiler;

import org.oleg.fcs.api.FlexCompiler;
import org.oleg.fcs.api.CompilationResults;
import org.oleg.fcs.event.Event;
import org.oleg.fcs.helper.Configuration;
import org.oleg.fcs.Constants;
import org.oleg.fcs.project.dao.ProjectDao;
import org.oleg.fcs.project.dao.ProjectException;
import org.oleg.fcs.server.ConfigProperties;
import org.oleg.fcs.util.ClassLoaderUtil;
import org.oleg.fcs.util.FlexUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;

import flex2.tools.oem.*;
import flex2.tools.oem.Application;
import flex2.tools.oem.internal.OEMProgressMeter;

import org.oleg.fcs.project.model.*;
import org.oleg.fcs.project.model.Project;
import org.oleg.fcs.project.model.Component;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
@SuppressWarnings("unchecked")
public class FlexCompilerImpl implements FlexCompiler {

    private final Log log = LogFactory.getLog(Configuration.class);

    public static final String  FRAMEWORKS_DIR = "frameworks";

    private static final Map<File, ProjectCacheEntry> PROJECT_CACHE = new HashMap<File, ProjectCacheEntry>();

    private ProjectDao projectDao;

    private Queue<Event> eventQueue;

    private Configuration conf;

    private String flexHome;
    private String frameworksDir;

    private Map<String, flex2.tools.oem.Application> applicationCache = new HashMap<String, flex2.tools.oem.Application>();
    private Map<String, Library> componentCache = new HashMap<String, Library>();
    private LibraryCache libraryCache;

    private ProgressTracker progressTracker = new ProgressTracker();

    public FlexCompilerImpl(Configuration configuration, Queue<Event> eventQueue, ProjectDao projectDao) {
        this.conf = configuration;
        this.eventQueue = eventQueue;
        this.projectDao = projectDao;

        flexHome = new File(FlexUtil.getFlexSDK(configuration)).getAbsolutePath();
        frameworksDir = flexHome + File.separator + FRAMEWORKS_DIR;

        Locale.setDefault(Locale.ENGLISH);

        fireEvent(new CompilerEvent(this, "Flex compiler initialized", CompilerEvent.EventType.Initialized, null));
    }

    protected void sortComponents(List<Component> components, List<Component> resultComponents, Map<Component, Set<Component>> componentDependencies, Stack<Component> componentStack) {
        for (Component c : components) {
            if (resultComponents.contains(c)) {
                continue; // component already processed
            }

            if (componentStack.contains(c)) {
                throw new IllegalStateException("Project contains recursive dependencies. Component: " + c.getName());
            }

            if (c.getDependencies() == null || c.getDependencies().size() == 0) {
                resultComponents.add(c);
                componentDependencies.put(c, new HashSet<Component>());
            } else {
                List<Component> cd = new ArrayList<Component>();
                List<Component> comps = new ArrayList<Component>();
                for (Dependency d : c.getDependencies()) {
                    comps.add(d.getComponent());
                }

                componentStack.push(c);
                sortComponents(comps, resultComponents, componentDependencies, componentStack);
                componentStack.pop();

                resultComponents.add(c);
                componentDependencies.put(c, collectDependencies(comps, componentDependencies));
            }
        }
    }

    protected Set<Component> collectDependencies(List<Component> dependencies, Map<Component, Set<Component>> componentDependencies) {
        Set<Component> result = new HashSet<Component>();

        for (Component c : dependencies) {
            if (componentDependencies.containsKey(c)) {
                Set<Component> d = collectDependencies(new ArrayList<Component>(componentDependencies.get(c)), componentDependencies);
                result.addAll(d);
            }

            result.add(c);
        }

        return result;
    }

    @Override
    public void clearCache() {
        PROJECT_CACHE.clear();
        applicationCache.clear();
        componentCache.clear();
    }

    @Override
    public List<CompilationResults> compile(String targetName, File projectFile, String dstDir) {
        Project project;
        List<Component> sortedComponentList = null;
        Map<Component, Set<Component>> componentDependencies = null;

        ProjectCacheEntry cacheEntry = PROJECT_CACHE.get(projectFile);

        try {
            if (cacheEntry == null || projectDao.isProjectModified(cacheEntry.getProject(), projectFile.toURI().toURL())) {
                try {
                    project = projectDao.getProject(projectFile.toURI().toURL());
                } catch (Exception e) {
                    throw new RuntimeException("Error in project definition.", e);
                }

                sortedComponentList = new ArrayList<Component>();
                componentDependencies = new HashMap<Component, Set<Component>>();
                sortComponents(project.getComponents(), sortedComponentList, componentDependencies, new Stack<Component>());

                PROJECT_CACHE.put(projectFile, new ProjectCacheEntry(project, sortedComponentList, componentDependencies));
            } else {
                project = cacheEntry.getProject();
                sortedComponentList = cacheEntry.getSortedComponents();
                componentDependencies = cacheEntry.getComponentDependencies();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        List<CompilationResults> compilationResultses = new ArrayList<CompilationResults>();
        long startTime = System.currentTimeMillis();

        fireEvent(new CompilerEvent(this, "Starting project '" + project.getName() + "'...", CompilerEvent.EventType.ProjectStart, project.toString()));

        boolean hasErrors = false;
        StringBuilder projectProtocol = new StringBuilder();
        for (Component comp : sortedComponentList) {
            CompilationResults compilationResults = compile(targetName, project, comp, projectFile.getParentFile(), dstDir, componentDependencies);
            compilationResultses.add(compilationResults);

            projectProtocol.append("------------------------------------------------------------- ").append(comp.toString()).append("\n");
            projectProtocol.append(compilationResults.getCompileProtocol()).append("\n\n");

            if (compilationResults.isHasErrors()) {
                hasErrors = true;
                break;
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        CompilerEvent event;
        if (hasErrors) {
            event = new CompilerEvent(this, "Flex project compilation fail.", CompilerEvent.EventType.ProjectFinishErrors, project.toString());
        } else {
            event = new CompilerEvent(this, "Flex project compilation success.", CompilerEvent.EventType.ProjectFinishSuccess, project.toString());
        }
        event.setCompilationProtocol(projectProtocol.toString());
        event.setCompilationTime(processingTime);
        fireEvent(event);

        return compilationResultses;
    }

    protected File getArtifactOutputFile(Artefact artefact, File dstDir) {
        File outputFile = null;

        String targetFileName = artefact.getFileName().substring(0, artefact.getFileName().lastIndexOf(".")) + ".swf";
        switch (artefact.getType()) {
            case application:
                outputFile = new File(dstDir, File.separator + targetFileName);
                break;
            case module:
                File swfTargetDir = new File(dstDir, Constants.ComponentFolders.MODULE_TARGET_DIR);
                if (!swfTargetDir.exists()) {
                    swfTargetDir.mkdirs();
                }
                outputFile = new File(swfTargetDir, targetFileName);
                break;
        }

        return outputFile;
    }

    protected CompilationResults compile(String targetName, Project project, Component component, File projectDir, String dstDir, Map<Component, Set<Component>> componentDependencies) {
        StringBuilder protocol = new StringBuilder();

        long startTime = System.currentTimeMillis();

        String currentPath = targetName;

        // detect changes
        if (targetName != null) {
            File swf = null;
            if (component instanceof org.oleg.fcs.project.model.Application) {
                org.oleg.fcs.project.model.Application app = (org.oleg.fcs.project.model.Application) component;

                Artefact appArt = app.getArtefacts().get(0);
                for (Artefact a : app.getArtefacts()) {
                    if (a.getType() == Artefact.Type.application && a.getFileName().equals(targetName + ".mxml")) {
                        appArt = a;
                        break;
                    }
                }

                String appArtFileName = appArt.getFileName().substring(0, appArt.getFileName().lastIndexOf(".")) + ".swf";

                if (appArt.getType() == Artefact.Type.application) {
                    swf = new File(dstDir, appArtFileName);
                } else {
                    swf = new File(dstDir, Constants.ComponentFolders.MODULE_TARGET_DIR + File.separator + appArtFileName);
                }
            } else {
                swf = new File(dstDir, Constants.ComponentFolders.SWC_TARGET_DIR + File.separator + component.getName() + ".swc");
            }

            if (swf.exists()) {
                Date resDate = new Date(swf.lastModified());
                if (!isModificationsPresent(new File(projectDir, component.getName()), resDate)) {
                    protocol.append("No changes... Finishing.");

                    CompilerEvent event = new CompilerEvent(this, "No Chnages", CompilerEvent.EventType.Success, component.toString());
                    event.setCompilationProtocol(protocol.toString());
                    fireEvent(event);

                    return new CompilationResults(protocol.toString(), false, false, project.getName(), component.getName());
                }
            }
        }

        Report report = null;
        String currentBuildTarget = null;
        List<File> outputFiles = new ArrayList<File>();

        if (!(component instanceof org.oleg.fcs.project.model.Application)) {
            protocol.append("Starting library '" + component.getName() + "'...").append("\n");
            fireEvent(new CompilerEvent(this, "Starting...", CompilerEvent.EventType.Started, component.toString()));

            File swcTargetDir = new File(dstDir, Constants.ComponentFolders.SWC_TARGET_DIR);
            if (!swcTargetDir.exists()) {
                swcTargetDir.mkdirs();
            }

            String flexComponentCacheKey = project.toString() + component.toString();
            Library flexComponent = componentCache.get(flexComponentCacheKey);
            if (flexComponent == null) {
                flexComponent = new Library();

                if (conf.getBoolean(ConfigProperties.SHOW_PROGRESS, false)) {
                    flexComponent.setProgressMeter(progressTracker);
                }

                flex2.tools.oem.Configuration configuration = flexComponent.getDefaultConfiguration();
                configure(configuration, new File(projectDir, component.getName()), new File(dstDir), component, componentDependencies.get(component));
                flexComponent.setConfiguration(configuration);

                File mxmlComp = new File(projectDir, component.getName() + File.separator + Constants.ComponentFolders.SRC_DIR);
                if (mxmlComp.exists()) {
                    flexComponent.addComponent(mxmlComp);
                }

                File srcComp = new File(projectDir, component.getName() + File.separator + Constants.ComponentFolders.MXML_DIR);
                if (srcComp.exists()) {
                    flexComponent.addComponent(srcComp);
                }

                flexComponent.setOutput(new File(swcTargetDir, component.getName() + ".swc"));
                componentCache.put(flexComponentCacheKey, flexComponent);
            }

            outputFiles.add(new File(swcTargetDir, component.getName() + ".swc"));

            protocol.append("Compiling...").append("\n");
            fireEvent(new CompilerEvent(this, "Compiling...", CompilerEvent.EventType.Compilation, component.toString()));

            try {
                flexComponent.build(true);
            } catch (IOException e) {
                log.error("Error during copilation", e);
                protocol.append("Technical error: ").append(e.getMessage());
                return new CompilationResults(protocol.toString(), true, true, project.getName(), component.getName());
            }

            report = flexComponent.getReport();
            currentBuildTarget = component.toString();
        } else {
            org.oleg.fcs.project.model.Application app = (org.oleg.fcs.project.model.Application) component;
            for (Artefact artefact : app.getArtefacts()) {
                if (targetName != null && artefact.getType() == Artefact.Type.application && !artefact.getFileName().equals(currentPath + ".mxml")) {
                    continue; // skip all applications if they are not needed
                }

                String currentMxml = artefact.getFileName();

                protocol.append("Starting " + artefact.getType() + " '" + artefact.getFileName() + "'...").append("\n");
                fireEvent(new CompilerEvent(this, "Starting...", CompilerEvent.EventType.Started, currentMxml));

                File mxmlFile = new File(projectDir, app.getName() + File.separator + Constants.ComponentFolders.MXML_DIR + File.separator + currentMxml);
                if (!mxmlFile.exists()) {
                    return new CompilationResults("No changes: MXML was deleted... Finishing.", false, false, project.getName(), component.getName());
                }

                String applicationCacheKey = project.toString() + app.toString() + currentMxml;
                flex2.tools.oem.Application application = applicationCache.get(applicationCacheKey);

                if (application == null) {
                    try {
                        application = new Application(mxmlFile);
                    } catch (FileNotFoundException e) {/* impossible! we already checked it */}

                    // FIXME: library cache
//                    if (libraryCache != null) {
//                        application.setSwcCache(libraryCache);
//                    }

                    if (conf.getBoolean(ConfigProperties.SHOW_PROGRESS, false)) {
                        application.setProgressMeter(progressTracker);
                    }

                    flex2.tools.oem.Configuration configuration = application.getDefaultConfiguration();
                    configure(configuration, new File(projectDir, app.getName()), new File(dstDir), app, componentDependencies.get(component));
                    application.setConfiguration(configuration);

                    application.setOutput(getArtifactOutputFile(artefact, new File(dstDir)));

                    applicationCache.put(applicationCacheKey, application);
                }

                protocol.append("Compiling...").append("\n");
                fireEvent(new CompilerEvent(this, "Compiling...", CompilerEvent.EventType.Compilation, currentMxml));

                if (outputFiles.size() == 0) {
                    for (Artefact a : app.getArtefacts()) {
                        outputFiles.add(getArtifactOutputFile(a, new File(dstDir)));
                    }
                }

                try {
                    application.build(true);
                } catch (IOException e) {
                    log.error("Error during copilation", e);
                    protocol.append("Technical error: ").append(e.getMessage());
                    return new CompilationResults(protocol.toString(), true, true, project.getName(), component.getName());
                }

                // FIXME: library cache
                // libraryCache = application.getSwcCache();

                report = application.getReport();
                currentBuildTarget = currentMxml;

                if (hasErrors(report.getMessages())) {
                    break;
                }
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        if (hasErrors(report.getMessages())) {
            if (outputFiles != null) {
                for (File f : outputFiles) {
                    f.delete(); // delete old artefact
                }
            }

            String s = "Flex compilation errors: \n" + toString(report.getMessages());
            protocol.append(s).append("\n");
            protocol.append("Compilation time: ").append(processingTime).append(" ms").append("\n");

            CompilerEvent event = new CompilerEvent(this, s, CompilerEvent.EventType.Errors, currentBuildTarget);
            event.setCompilationTime(processingTime);
            event.setCompilationProtocol(protocol.toString());
            fireEvent(event);
            return new CompilationResults(protocol.toString(), true, true, project.getName(), component.getName());
        } else {
            String s = "Flex compilation successful." + toString(report.getMessages());
            protocol.append(s).append("\n");
            protocol.append("Compilation time: ").append(processingTime).append(" ms").append("\n");

            CompilerEvent event = new CompilerEvent(this, s, CompilerEvent.EventType.Success, currentBuildTarget);
            event.setCompilationTime(processingTime);
            event.setCompilationProtocol(protocol.toString());
            fireEvent(event);
            return new CompilationResults(protocol.toString(), true, false, project.getName(), component.getName());
        }
    }

    private boolean isModificationsPresent(File path, Date date) {
        for (File f : path.listFiles()) {
            boolean modified = false;

            if (f.isDirectory()) {
                modified = isModificationsPresent(f, date);
            } else {
                if (!f.getName().endsWith(".cache")) {
                    modified = new Date(f.lastModified()).after(date);
                }
            }

            if (modified) {
                return true;
            }

        }

        return false;
    }

    protected void configure(flex2.tools.oem.Configuration configuration, File componentBaseDir, File dstDir, Component component, Set<Component> componentDependencies) {
        configuration.setConfiguration(new File(frameworksDir, "flex-config.xml"));
        configuration.setLocalFontSnapshot(new File(frameworksDir, conf.getString(ConfigProperties.FLEX_FONTS, Constants.DEFAULT_FLEX_FONTS)));
        configuration.setLocale(new String[] {"en_US"});

        List<File> sourcePaths = new ArrayList<File>();
        File mxmlPath = new File(componentBaseDir, Constants.ComponentFolders.MXML_DIR);
        if (mxmlPath.exists()) {
            sourcePaths.add(mxmlPath);
        }
        File srcPath = new File(componentBaseDir, Constants.ComponentFolders.SRC_DIR);
        if (srcPath.exists()) {
            sourcePaths.add(srcPath);
        }
        configuration.setSourcePath(sourcePaths.toArray(new File[sourcePaths.size()]));

        List<File> libs = new ArrayList<File>();

        libs.add(new File(frameworksDir, "libs"));
        libs.add(new File(frameworksDir, "locale"));

        File libDir = new File(componentBaseDir, Constants.ComponentFolders.LIB_DIR);
        if (libDir.exists()) {
            libs.add(libDir);
        }

        if (componentDependencies != null && componentDependencies.size() > 0) {
            libs.add(new File(dstDir, Constants.ComponentFolders.SWC_TARGET_DIR));
            for (Component c : componentDependencies) {
                File compLibDir = new File(componentBaseDir, ".." + File.separator + c.getName() + File.separator + Constants.ComponentFolders.LIB_DIR);
                if (compLibDir.exists()) {
                    libs.add(compLibDir);
                }
            }
        }

        if (conf.isPresent(ConfigProperties.FLEX_ADDITIONAL_LIBS)) {
            String[] additional = conf.getStringArray(ConfigProperties.FLEX_ADDITIONAL_LIBS);
            for (String al : additional) {
                libs.add(new File(al));
            }
        }

        configuration.setLibraryPath(libs.toArray(new File[libs.size()]));
    }

    protected String toString(Message[] messages) {
        if (messages != null) {
            StringBuilder sb = new StringBuilder();
            for (Message m : messages) {
                sb.append("\n");
                sb.append(m.getLevel()).append(": ").append(m.toString()).append(" [").append(m.getPath()).append(" ").append(m.getLine()).append(":").append(m.getColumn()).append("]").append("\n");
                writeLineFromFile(sb, m.getPath(), m.getLine(), m.getColumn());
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    protected void writeLineFromFile(StringBuilder sb, String filePath, int line, int col) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));

            int count = 0;
            String s;
            while ((s = reader.readLine()) != null) {
                count++;

                if (count == line - 1 || count == line + 1) {
                    sb.append(String.format("%4d> %s\n", count, s));
                } else if (count == line) {
                    sb.append(String.format("%4d> %s\n", count, s));
                    sb.append(String.format("      %" + col + "s\n", "^"));
                }
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean hasErrors(Message[] messages) {
        if (messages != null) {
            for (Message m : messages) {
                if (m.getLevel().equals(Message.ERROR)) {
                    return true;
                }
            }
        }

        return false;
    }
    // --------------------------------- Events

    public void setEventQueue(Queue<Event> eventQueue) {
        this.eventQueue = eventQueue;
    }

    protected void fireEvent(Event event) {
        synchronized (eventQueue) {
            eventQueue.add(event);
            eventQueue.notifyAll();
        }
    }

    private class ProgressTracker implements ProgressMeter {

        public void start() { }

        public void end() { }

        public void percentDone(int i) {
            CompilerEvent event = new CompilerEvent(this, null, CompilerEvent.EventType.Process, null);
            event.setCompilationProcess(i);
            fireEvent(event);
        }
    }

    // ------------------------------------- Classes

    private static class ProjectCacheEntry {
        private Project project;
        private List<Component> sortedComponents;
        private Map<Component, Set<Component>> componentDependencies;

        private ProjectCacheEntry(Project project, List<Component> sortedComponents, Map<Component, Set<Component>> componentDependencies) {
            this.project = project;
            this.sortedComponents = sortedComponents;
            this.componentDependencies = componentDependencies;
        }

        public Project getProject() {
            return project;
        }

        public List<Component> getSortedComponents() {
            return sortedComponents;
        }

        public Map<Component, Set<Component>> getComponentDependencies() {
            return componentDependencies;
        }
    }
}

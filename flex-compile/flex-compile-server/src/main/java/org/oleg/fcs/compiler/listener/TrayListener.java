package org.oleg.fcs.compiler.listener;

import org.oleg.fcs.event.EventListener;
import org.oleg.fcs.event.Event;
import org.oleg.fcs.compiler.CompilerEvent;
import org.oleg.fcs.ui.FileViewer;
import org.oleg.fcs.helper.Configuration;
import org.oleg.fcs.server.ConfigProperties;
import org.oleg.fcs.api.FlexCompiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class TrayListener implements EventListener {

    public static final Image NORMAL_ICON = Toolkit.getDefaultToolkit().getImage(TrayListener.class.getResource("/META-INF/icons/logo_flex.gif"));
    public static final Image RUNNING_ICON = Toolkit.getDefaultToolkit().getImage(TrayListener.class.getResource("/META-INF/icons/outbox.png"));

    private static final File[] LOG_FILES = new File[] {new File("flex-compile-server-reports.log"), new File("flex-compile-server.log")};

    private TrayIcon trayIcon;

    private Configuration conf;

    private static FileViewer FILE_VIEWER;

    private FlexCompiler flexCompiler;

    private boolean showComponentNotifications;

    private boolean showProjectNotifications;

    private boolean showNotifications;

    private String lastProtocol;

    public TrayListener(Configuration configuration) {
        this.conf = configuration;

        showNotifications = conf.getBoolean(ConfigProperties.SHOW_TRAY_NOTIFICATIONS, true);
        showComponentNotifications = configuration.getBoolean(ConfigProperties.SHOW_COMPONENT_NOTIFICATIONS, false);
        showProjectNotifications = configuration.getBoolean(ConfigProperties.SHOW_PROJECT_NOTIFICATIONS, true);

        ActionListener logListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFileViewer();
            }
        };

        ActionListener exitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = new MenuItem("About...");
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Flex Compile Server v1.0\n Author: Oleg Ilyenko", "About...", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        popup.add(aboutItem);

        MenuItem clearCacheItem = new MenuItem("Clear cache");
        clearCacheItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flexCompiler.clearCache();
            }
        });
        popup.add(clearCacheItem);

        MenuItem logItem = new MenuItem("View logs...");
        logItem.addActionListener(logListener);
        popup.add(logItem);

        popup.addSeparator();

        MenuItem defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);


        trayIcon = new TrayIcon(NORMAL_ICON, "Flex Compile Server: Waiting.", popup);

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showFileViewer();
                    }
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
                return;
            }

        } else {

            //  System Tray is not supported

        }
    }

    public FlexCompiler getFlexCompiler() {
        return flexCompiler;
    }

    public void setFlexCompiler(FlexCompiler flexCompiler) {
        this.flexCompiler = flexCompiler;
    }

    protected void showFileViewer() {
        if (FILE_VIEWER == null) {
            FILE_VIEWER = new FileViewer(conf, "Compiller logs", flexCompiler, LOG_FILES);
            FILE_VIEWER.setLastCompilationResults(lastProtocol == null ? "" : lastProtocol);
        } else {
            FILE_VIEWER.setLastCompilationResults(lastProtocol == null ? "" : lastProtocol);
            FILE_VIEWER.showFileViewer();

        }
    }

    public void onEvent(Event e) {
        if (e instanceof CompilerEvent && showNotifications) {
            CompilerEvent ce = (CompilerEvent) e;


            if (ce.getEventType() == CompilerEvent.EventType.Initialized) {
                trayIcon.displayMessage("Initialization successful", ce.getMessage(), TrayIcon.MessageType.INFO);
                trayIcon.setToolTip( "Flex Compile Server: Waiting for connections.");
            }

            if (showComponentNotifications) {
                switch (ce.getEventType()) {
                    case Started:
                        trayIcon.setImage(RUNNING_ICON);
                        trayIcon.displayMessage("Compilation started", "Compiling " + ce.getMainArtifact(), TrayIcon.MessageType.INFO);
                        trayIcon.setToolTip( "Flex Compile Server: Compiling...");
                        break;
                    case Compilation:
                        break;
                    case Process:
                        trayIcon.displayMessage("Compilation process", "Compiling " + ce.getCompilationProcess() + "%", TrayIcon.MessageType.INFO);
                        break;
                    case Errors:
                        trayIcon.displayMessage("Compilation failed", "Compalation failed! Time spent: " + ce.getCompilationTime() + " ms", TrayIcon.MessageType.ERROR);
                        trayIcon.setToolTip( "Flex Compile Server: Waiting for connections.");
                        trayIcon.setImage(NORMAL_ICON);
                        lastProtocol = ce.getCompilationProtocol();
                        break;
                    case Success:
                        trayIcon.displayMessage("Compilation success", "Compalation Success! Time spent: " + ce.getCompilationTime() + " ms", TrayIcon.MessageType.INFO);
                        trayIcon.setToolTip( "Flex Compile Server: Waiting for connections.");
                        trayIcon.setImage(NORMAL_ICON);
                        lastProtocol = ce.getCompilationProtocol();
                        break;
                }
            }

            if (showProjectNotifications) {
                switch (ce.getEventType()) {
                    case ProjectStart:
                        trayIcon.setImage(RUNNING_ICON);
                        trayIcon.displayMessage("Compilation started", "Compiling " + ce.getMainArtifact(), TrayIcon.MessageType.INFO);
                        trayIcon.setToolTip( "Flex Compile Server: Compiling project...");
                        break;
                    case Compilation:
                        break;
                    case Process:
                        trayIcon.displayMessage("Compilation process", "Compiling " + ce.getCompilationProcess() + "%", TrayIcon.MessageType.INFO);
                        break;
                    case ProjectFinishErrors:
                        trayIcon.displayMessage("Compilation failed", "Compalation of project failed! Time spent: " + ce.getCompilationTime() + " ms", TrayIcon.MessageType.ERROR);
                        trayIcon.setToolTip( "Flex Compile Server: Waiting for connections.");
                        trayIcon.setImage(NORMAL_ICON);
                        lastProtocol = ce.getCompilationProtocol();
                        break;
                    case ProjectFinishSuccess:
                        trayIcon.displayMessage("Compilation success", "Compalation of project successfil! Time spent: " + ce.getCompilationTime() + " ms", TrayIcon.MessageType.INFO);
                        trayIcon.setToolTip( "Flex Compile Server: Waiting for connections.");
                        trayIcon.setImage(NORMAL_ICON);
                        lastProtocol = ce.getCompilationProtocol();
                        break;
                }
            }
        }
    }
}

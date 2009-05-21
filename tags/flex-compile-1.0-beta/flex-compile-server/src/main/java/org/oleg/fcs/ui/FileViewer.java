package org.oleg.fcs.ui;

import org.oleg.fcs.helper.Configuration;
import org.oleg.fcs.Constants;
import org.oleg.fcs.api.FlexCompiler;
import org.oleg.fcs.server.ConfigProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class FileViewer extends JFrame {

    private File[] filesToProcess;

    private Map<String, JTextArea> panes = new HashMap<String, JTextArea>();

    private Configuration conf;

    private FlexCompiler flexCompiler;

    private JTextArea lastCompilationResults;

    public FileViewer(Configuration configuration, String title, FlexCompiler fc, File... files) throws HeadlessException {
        super(title);
        this.conf = configuration;
        this.filesToProcess = files;
        this.flexCompiler = fc;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabby = new JTabbedPane();

        lastCompilationResults = new JTextArea();
        lastCompilationResults.setEditable(false);
        tabby.addTab("Last compilation results", new JScrollPane(lastCompilationResults));

        for (File f : filesToProcess) {
            JTextArea textPane = new JTextArea();
            textPane.setEditable(false);
            textPane.setWrapStyleWord(false);
            panes.put(f.getAbsolutePath(), textPane);
            tabby.addTab(f.getName(), new JScrollPane(textPane));
        }

        updatePanes();

        add(tabby);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton but = new JButton("Clear logs");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (File f : filesToProcess) {
                    try {
                        new FileOutputStream(f).close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    updatePanes();
                }
            }
        });
        buttonPane.add(but);

        but = new JButton("Clear cache");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flexCompiler.clearCache();
            }
        });
        buttonPane.add(but);

        but = new JButton("Refresh");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePanes();
            }
        });
        buttonPane.add(but);

        but = new JButton("Close");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(but);

        but = new JButton("Exit");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonPane.add(but);

        add(buttonPane, BorderLayout.SOUTH);

        int width = configuration.getInteger(ConfigProperties.LOG_VIEWER_WIDTH, Constants.DEFAULR_LOG_VIEWER_WIDTH);
        int height = configuration.getInteger(ConfigProperties.LOG_VIEWER_HEIGHT, Constants.DEFAULR_LOG_VIEWER_HEIGHT);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent w) {
                setVisible(false);
            }
        });

        JPanel content = (JPanel) getContentPane();
        InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        content.getActionMap().put("ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });


        setBounds(dim.width - width, dim.height - height - 24, width, height);
        setVisible(true);
    }

    public void showFileViewer() {
        updatePanes();
        setVisible(true);
    }

    public void setLastCompilationResults(String text) {
        lastCompilationResults.setText(text);
    }

    protected void updatePanes() {
        for (File f : filesToProcess) {
            JTextArea pane = panes.get(f.getAbsolutePath());

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(f));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                pane.setText(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
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

    }

}

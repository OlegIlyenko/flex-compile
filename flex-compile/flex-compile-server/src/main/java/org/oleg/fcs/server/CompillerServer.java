package org.oleg.fcs.server;

import org.oleg.fcs.Constants;
import org.oleg.fcs.project.dao.ProjectDao;
import org.oleg.fcs.project.model.Project;
import org.oleg.fcs.api.*;
import org.oleg.fcs.helper.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompillerServer implements Runnable {

    private final Log log = LogFactory.getLog(this.getClass());

    private Configuration conf;

    private FlexCompiler delegated;

    public CompillerServer(FlexCompiler flexCompiler, Configuration configuration) {
        this.conf = configuration;
        this.delegated = flexCompiler;
    }

    public void run() {
        int port = conf.getInteger(ConfigProperties.SERVER_PORT, Constants.DEFAULT_PORT);

        log.info("Using port: " + port);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            log.error("Error during starting the server.", e);
            System.exit(-1);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                log.info("Recieved new connection.");
                Thread thread = new Thread(new Comunicator(socket), "Client socket comunicator");
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                log.error("Error during accepting connection", e);
            }
        }
    }

    protected class Comunicator implements Runnable {

        private Socket socket;

        private ObjectInputStream in;

        private ObjectOutputStream out;

        public Comunicator(Socket socket) throws IOException {
            this.socket = socket;
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        }

        public void run() {
            try {
                while (true) {
                    Object obj = in.readObject();

                    if (obj instanceof CompileValue) {
                        CompileValue val = (CompileValue) obj;
                        File projectFile = val.getProjectFile();

                        List<CompilationResults> res = delegated.compile(val.getPath(), projectFile, val.getDstDir());
                        out.writeObject(res);

                    } else if (obj instanceof ClearCacheValue) {
                        delegated.clearCache();
                    } else if (obj instanceof CloseConnectionValue) {
                        socket.close();
                        break;
                    } else {
                        log.error("Unknown object recieved from server: " + obj.getClass().getName());
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error during comunicating with client", e);
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    log.error("Error during closing connection with client", e);
                }
            }
        }
    }

}

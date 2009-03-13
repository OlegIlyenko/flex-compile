package org.oleg.fcs.client;

import org.oleg.fcs.Constants;
import org.oleg.fcs.api.*;

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
@SuppressWarnings("unchecked")
public class FlexCompilerSocketImpl implements FlexCompiler {

    private String host = Constants.CLIENT_DEFAULT_HOST;

    private int port = Constants.DEFAULT_PORT;

    protected Socket socket;

    protected ObjectInputStream in;

    protected ObjectOutputStream out;

    public FlexCompilerSocketImpl() {
    }

    public FlexCompilerSocketImpl(String host, int port) throws ConnectionException {
        this.host = host;
        this.port = port;

        try {
            checkAndInitializeConnection();
        } catch (IOException e) {
            throw new ConnectionException("Unable connect to the server", e);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public List<CompilationResults> compile(String targetName, File projecFile, String dstDir) throws ConnectionException {
        try {
            checkAndInitializeConnection();

            out.writeObject(new CompileValue(targetName, projecFile, dstDir));
            Object res = in.readObject();

            if (!(res instanceof List)) {
                throw new ConnectionException("Server returned unknown object: " + res.getClass().getName());
            }

            return (List<CompilationResults>) res;
        } catch (Exception e) {
            throw new ConnectionException("Error duting comunication with server.", e);
        }
    }

    @Override
    public void clearCache() {
        try {
            checkAndInitializeConnection();
            out.writeObject(new ClearCacheValue());
        } catch (Exception e) {
            throw new ConnectionException("Error duting comunication with server.", e);
        }
    }

    public void closeConnection () {
        try {
            out.writeObject(new CloseConnectionValue());
            socket.close();
        } catch (Exception e) {
            throw new ConnectionException("Error duting comunication with server.", e);
        }
    }

    protected void checkAndInitializeConnection() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            socket = getConnection();
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
    }

    protected Socket getConnection(){
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }
}

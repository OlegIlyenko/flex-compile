package org.oleg.fcs.api;

import java.io.Serializable;
import java.io.File;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class CompileValue implements Serializable {

    private String path;

    private File projectFile;

    private String dstDir;

    public CompileValue(String path, File projectFile, String dstDir) {
        this.path = path;
        this.dstDir = dstDir;
        this.projectFile = projectFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public String getDstDir() {
        return dstDir;
    }

    public void setDstDir(String dstDir) {
        this.dstDir = dstDir;
    }
}

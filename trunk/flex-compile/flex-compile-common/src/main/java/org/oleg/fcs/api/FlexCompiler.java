package org.oleg.fcs.api;

import java.io.File;
import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public interface FlexCompiler {

    List<CompilationResults> compile(String targetName, File projecFile, String dstDir);

    void clearCache();
}

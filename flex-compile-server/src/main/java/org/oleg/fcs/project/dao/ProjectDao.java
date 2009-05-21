package org.oleg.fcs.project.dao;

import org.oleg.fcs.project.model.Project;

import java.net.URL;
import java.io.InputStream;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public interface ProjectDao {

    Project getProject(URL resouce) throws ProjectException;

    Project getProject(InputStream inputStream) throws ProjectException;

    boolean isProjectModified(Project project, URL resource);

}

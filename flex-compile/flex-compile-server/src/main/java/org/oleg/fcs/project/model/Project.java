package org.oleg.fcs.project.model;

import java.util.List;
import java.util.Date;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Project {

    private String name;

    private List<Component> components;

    private Date lastModificationDate;

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "Project '" + name + "'";
    }
}

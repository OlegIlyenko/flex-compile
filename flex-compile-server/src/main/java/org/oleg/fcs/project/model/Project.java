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

    private List<String> defaultLocales;

    private String defaultFlexConfig;

    private List<Theme> defaultThemes;

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

    public List<String> getDefaultLocales() {
        return defaultLocales;
    }

    public void setDefaultLocales(List<String> defaultLocales) {
        this.defaultLocales = defaultLocales;
    }

    public String getDefaultFlexConfig() {
        return defaultFlexConfig;
    }

    public void setDefaultFlexConfig(String defaultFlexConfig) {
        this.defaultFlexConfig = defaultFlexConfig;
    }

    public List<Theme> getDefaultThemes() {
        return defaultThemes;
    }

    public void setDefaultThemes(List<Theme> defaultThemes) {
        this.defaultThemes = defaultThemes;
    }
}

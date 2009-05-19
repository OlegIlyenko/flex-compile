package org.oleg.fcs.project.model;

import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Component {

    private String name;

    private String flexConfig;

    private List<String> locales;

    private List<Dependency> dependencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getLocales() {
        return locales;
    }

    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    public String getFlexConfig() {
        return flexConfig;
    }

    public void setFlexConfig(String flexConfig) {
        this.flexConfig = flexConfig;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Component '" + name + "'";
    }

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((Component) obj).getName());
    }
}

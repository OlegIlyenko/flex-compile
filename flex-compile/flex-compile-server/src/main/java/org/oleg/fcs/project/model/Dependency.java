package org.oleg.fcs.project.model;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Dependency {

    private String name;

    private Component component;

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

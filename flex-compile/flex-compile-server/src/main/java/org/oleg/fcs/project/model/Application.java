package org.oleg.fcs.project.model;

import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Application extends Component {

    private List<Artefact> artefacts;

    private List<Theme> themes;

    public List<Artefact> getArtefacts() {
        return artefacts;
    }

    public void setArtefacts(List<Artefact> artefacts) {
        this.artefacts = artefacts;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    @Override
    public String toString() {
        return "Application '" + getName() + "'. Artifacts: " + getArtefacts();
    }
}

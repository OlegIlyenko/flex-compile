package org.oleg.fcs.project.model;

import java.util.List;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class Application extends Component {

    private List<Artefact> artefacts;

    public List<Artefact> getArtefacts() {
        return artefacts;
    }

    public void setArtefacts(List<Artefact> artefacts) {
        this.artefacts = artefacts;
    }

    @Override
    public String toString() {
        return "Application '" + getName() + "'. Artifacts: " + getArtefacts();
    }
}

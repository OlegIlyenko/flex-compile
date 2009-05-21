package org.oleg.fcs.project.dao.impl;

import org.oleg.fcs.project.dao.ProjectDao;
import org.oleg.fcs.project.dao.ProjectException;
import org.oleg.fcs.project.model.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.*;

/**
 * TODO: Class Description
 *
 * @author Oleg Ilyenko
 */
public class XmlProjectDaoImpl implements ProjectDao {

    @Override
    public boolean isProjectModified(Project project, URL resource) {
        try {
            File file = new File(resource.toURI());
            Date newDate = new Date(file.lastModified());
            return !project.getLastModificationDate().equals(newDate);
        } catch (Exception e) {
            return true; // we can't guess this, so assume that it was modified
        }
    }

    @Override
    public Project getProject(URL resouce) throws ProjectException {
        try {
            Project project = getProject(resouce.openStream());

            if (project != null) {
                try {
                    File file = new File(resouce.toURI());
                    project.setLastModificationDate(new Date(file.lastModified()));
                } catch (Exception e) {
                    // just skip we can't guess last modification time
                }
            }

            return project;
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    @Override
    public Project getProject(InputStream inputStream) throws ProjectException {
        Project project;

        XMLInputFactory xmlif = XMLInputFactory.newInstance();

        try {
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(inputStream);

            project = readProject(xmlr);
            restoreDependencies(project);

            xmlr.close();
        } catch (XMLStreamException e) {
            throw new ProjectException(e);
        }

        return project;
    }

    private void restoreDependencies(Project project) {
        Map<String, Component> componentMap = new HashMap<String, Component>();
        for (Component c : project.getComponents()) {
            componentMap.put(c.getName(), c);
        }

        for (Component c : project.getComponents()) {
            if (c.getDependencies() != null && c.getDependencies().size() > 0) {
                for (Dependency dep : c.getDependencies()) {
                    dep.setComponent(componentMap.get(dep.getName()));
                }
            }
        }
    }

    protected Project readProject(XMLStreamReader reader) throws ProjectException {
        Project project = new Project();
        project.setComponents(new ArrayList<Component>());

        try {
            String text = "";
            Component currComponent = null;
            Artefact currArtefact = null;
            Theme currTheme = null;
            String name = null;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        name = reader.getLocalName();
                        if (name.equals("flex-project")) {
                            project.setDefaultLocales(getLocales(reader.getAttributeValue(null, "defaultLocales")));
                        } else if (name.equals("application")) {
                            currComponent = new Application();
                            currComponent.setLocales(getLocales(reader.getAttributeValue(null, "locales")));
                        } else if (name.equals("component")) {
                            currComponent = new Component();
                            currComponent.setLocales(getLocales(reader.getAttributeValue(null, "locales")));
                        } else if (name.equals("theme")) {
                            currTheme = new Theme();
                        } else if (name.equals("themes")) {
                            ((Application) currComponent).setThemes(new ArrayList<Theme>());
                        } else if (name.equals("default-themes")) {
                            project.setDefaultThemes(new ArrayList<Theme>());
                        } else if (name.equals("artefact")) {
                            if (!(currComponent instanceof Application)) {
                                throw new ProjectException("Component can't have any artifacts");
                            }

                            currArtefact = new Artefact();
                        }

                        text = "";
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        name = reader.getLocalName();
                        if (name.equals("application") || name.equals("component")) {
                            project.getComponents().add(currComponent);
                            currComponent = null;
                        } else if (name.equals("name")) {
                            if (currComponent != null) {
                                if (currTheme != null) {
                                    currTheme.setName(text);
                                } else {
                                    currComponent.setName(text);
                                }
                            } else {
                                project.setName(text);
                            }
                        } else if (name.equals("type")) {
                            if (currTheme != null) {
                                currTheme.setType(Theme.Type.valueOf(text));
                            } else if (currArtefact != null) {
                                currArtefact.setType(Artefact.Type.valueOf(text));
                            }
                        } else if (name.equals("default-flex-config")) {
                            project.setDefaultFlexConfig(text);
                        } else if (name.equals("flex-config")) {
                            currComponent.setFlexConfig(text);
                        } else if (name.equals("dependency")) {
                            if (currComponent.getDependencies() == null) {
                                currComponent.setDependencies(new ArrayList<Dependency>());
                            }

                            Dependency dep = new Dependency();
                            dep.setName(text);
                            currComponent.getDependencies().add(dep);
                        } else if (name.equals("fileName")) {
                            currArtefact.setFileName(text);
                        } else if (name.equals("artefact")) {
                            Application app = (Application) currComponent;
                            if (app.getArtefacts() == null) {
                                app.setArtefacts(new ArrayList<Artefact>());
                            }
                            app.getArtefacts().add(currArtefact);
                            currArtefact = null;
                        } else if (name.equals("theme")) {
                            if (currComponent != null && (currComponent instanceof Application)) {
                                ((Application) currComponent).getThemes().add(currTheme);
                            } else {
                                project.getDefaultThemes().add(currTheme);
                            }
                            currTheme = null;
                        }
                        break;
                    case XMLStreamConstants.SPACE:
                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.CDATA:
                        int start = reader.getTextStart();
                        int length = reader.getTextLength();
                        text += new String(reader.getTextCharacters(), start, length);
                        break;
                }
            }
        } catch (XMLStreamException e) {
            throw new ProjectException(e);
        }

        return project;
    }

    protected List<String> getLocales(String locales) {
        if (locales == null || locales.trim().equals("")) {
            return new ArrayList<String>();
        }

        List<String> localeList = new ArrayList<String>();
        for (String locale : locales.split("\\s*,\\s*")) {
            if (!locale.trim().equals("")) {
                localeList.add(locale.trim());
            }
        }

        return localeList;
    }
}

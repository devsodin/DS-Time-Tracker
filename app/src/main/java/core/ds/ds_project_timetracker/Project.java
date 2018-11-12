package core.ds.ds_project_timetracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Structural representation of a project.
 */
public class Project extends Node implements Serializable, Visitable {

    /**
     * Collection that contains all the activities (Projects or Tasks).
     */
    private Collection<Node> activities;
    private final static Logger LOGGER = LoggerFactory.getLogger(Project.class);


    /**
     * Constructor for the Project objects.
     * It calls the constructor and also init the activity Collection
     * and add its to his parent's activity Collection
     *
     * @param name        Project's Name. Must be non empty String
     * @param description Project's Description
     * @param parent      Project's parent. Null if it's root project
     */
    Project(final String name, final String description, final Project parent) {
        if (name != null && !name.isEmpty()) {
            this.setName(name);
            this.setDescription(description);
            this.setParent(parent);
            this.setDuration(0);
            this.setStartDate(null);
            this.setEndDate(null);

        } else {
            throw new IllegalArgumentException("Project must have a name");
        }

        this.activities = new ArrayList<>();

        if (this.getParent() != null) {
            this.getParent().getActivities().add(this);
        }

        this.id = new Id();
        if (isRootNode()) {
            this.id.generateid();
        } else {
            this.id.setId(this.getParent().getId().getId() + "." + this.getParent().getActivities().size());
        }

        Project.LOGGER.info("New Project created with name" + this.getName());
    }

    /**
     * Method that updates all the data for the current project
     * and calls recursively to it's parent.
     *
     * @param time time to do the update. Usually the actual Clock time
     */
    @Override
    public void updateData(final Date time) {
        if (this.getStartDate() == null) {
            this.setStartDate(time);
        }
        this.setEndDate(time);

        long tmp = 0;
        for (Node i : this.getActivities()) {
            tmp += i.getDuration();
        }
        this.setDuration(tmp);

        if (this.getParent() != null) {
            this.parent.updateData(time);
        }

    }

    /**
     * Entrance method for the visitor to do tasks for the Project class.
     *
     * @param visitor the visitor
     */
    @Override
    public void accept(final Visitor visitor) {
        LOGGER.info("Visitor accepts in project" + this.getName());
        ((TreeVisitor) visitor).visitProject(this);
    }

    /**
     * Method that gives all relevant information
     * about a project with the format.
     * Name StartDate EndDate Duration ParentName/None
     *
     * @return String with all the data separated by one tab (\t)
     */
    @Override
    public String toString() {
        String parent;
        if (this.getParent() == null) {
            parent = "None";
        } else {
            parent = this.getParent().getName();
        }

        return this.getName() + "\t" + this.getStartDate() + "\t"
                + this.getEndDate() + "\t" + this.getDuration() + "\t" + parent;
    }

    /**
     * Getter for the activity list of this node.
     *
     * @return Collection of Nodes with all the node's
     * activities (Tasks and Subprojects)
     */
    public Collection<Node> getActivities() {
        return this.activities;
    }


    /**
     * Getter for the parent's project. It calls the Node
     * version and casts it to Project because
     * a Project parent is always another Project.
     *
     * @return The project's parent
     */
    @Override
    public Project getParent() {
        return (Project) super.getParent();
    }


}

package core.ds.ds_project_timetracker;


import java.io.Serializable;
import java.util.Date;

/**
 * Abstract class that represent a generic node on the tree (Task and Projects).
 * It's serializable and Visitable so it can be saved to a file and can accept visitors.
 */
public abstract class Node implements Serializable, Visitable {

    protected String name;
    protected String description;
    protected long duration;
    protected Date startDate;
    protected Date endDate;
    protected Node parent;

    /**
     * Node constructor that setup all the shared fields. Name is required and must be non-empty string
     *
     * @param name        Name of the node
     * @param description Description of the node
     * @param parent      Parent of the node. Null if is the root node
     */
    public Node(String name, String description, Node parent) {
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

    }

    /**
     * Empty constructor
     */
    public Node() {
        //TODO ask if it's necessary or correct
    }

    /**
     * Abstract method used for the refresh of all the modified data
     *
     * @param time time to do the update. Usually the actual Clock time
     */
    public abstract void updateData(Date time);

    /**
     * Getter for the name field
     *
     * @return String of the node's name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the description field
     *
     * @return String of the node's description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the duration field
     *
     * @return long of the node's duration
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Getter for the startDate field
     * @return Date of the node's startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter for the endDate field
     * @return Date of the node's endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter for the parent field
     * @return Node parent of the node. Null if is the root Node. Must be casted to Project or Task
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Setter for the parent node
     * @param parent the parent of the node (Node, Task or Project)
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }
}



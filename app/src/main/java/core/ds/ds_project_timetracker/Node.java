package core.ds.ds_project_timetracker;


public class Node {

    protected String name;
    protected String description;

    private float duration = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}



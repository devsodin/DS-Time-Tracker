
package core.ds.ds_project_timetracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Concrete class that represent a basic and Short Report.
 * It contains only the Report data and the rootProjects data.
 */
public class ShortReport extends Report implements TreeVisitor {

    private Container title = new Title("Short Report");
    private Container subtitleReports = new Subtitle("Period");
    private Container subtitleRootProjects = new Subtitle("Root Projects");
    private Container footer = new Text("Time Tracker 1.0");

    /**
     * Constructor for the ShortReport.
     * It creates the title and the tables.
     *
     * @param rootVisitable the first Visitable to visit.
     * @param reportPeriod  The period to be reported.
     */
    public ShortReport(final Project rootVisitable, final Period reportPeriod, final ReportGenerator reportGenerator) {
        super(rootVisitable, reportPeriod, reportGenerator);

    }


    @Override
    public void createReport() {
        this.reportGenerator.visitSeparator(new Separator());
        this.reportGenerator.visitTitle((Title) this.title);
        this.reportGenerator.visitSeparator(new Separator());
        this.reportGenerator.visitSubtitle((Subtitle) this.subtitleReports);
        this.reportGenerator.visitTable(createReportTable());
        this.reportGenerator.visitSeparator(new Separator());
        this.reportGenerator.visitSubtitle((Subtitle) this.subtitleRootProjects);
        //this.reportGenerator.visitTable();
        this.reportGenerator.visitSeparator(new Separator());
        this.reportGenerator.visitText((Text) footer);

    }


    @Override
    public void visitProject(final Project project) {

        if (isInPeriod(project.getStartDate(), project.getEndDate())) {

            String name = project.getName();
            String desc = project.getDescription();
            Date startDate = getNewStartDate(project.getStartDate());
            Date endDate = getNewEndDate(project.getEndDate());
            long duration = getNewDuration(project.getStartDate(), project.getEndDate(), project.getDuration()); //TODO ESTO NO ES REAL SIEMPRE

            ArrayList<String> entry = new ArrayList<>(Arrays.asList(name, desc,
                    startDate.toString(), endDate.toString(),
                    String.valueOf(duration)));

            //this.rootProjectsTable.addRow(entry);
        }

    }

    @Override
    public void visitTask(final Task task) {

        long taskduration = 0;
        for (Interval i : task.getIntervals()) {
            if (isInPeriod(i.getStartDate(), i.getEndDate())) {
                taskduration += i.getDuration();
            }
        }

    }

    @Override
    public void visitInterval(Interval interval) {
        return;
    }

}

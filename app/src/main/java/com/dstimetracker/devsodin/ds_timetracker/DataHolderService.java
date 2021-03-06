package com.dstimetracker.devsodin.ds_timetracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.dstimetracker.devsodin.core.BaseTask;
import com.dstimetracker.devsodin.core.Clock;
import com.dstimetracker.devsodin.core.DataManager;
import com.dstimetracker.devsodin.core.Interval;
import com.dstimetracker.devsodin.core.Node;
import com.dstimetracker.devsodin.core.Project;
import com.dstimetracker.devsodin.core.Task;
import com.dstimetracker.devsodin.core.TreeVisitor;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Service class used to hold the Node-tree.
 * It implements observer to send new data according to refresh rate and treeVisitor to get all active tasks
 * <p>
 * This class has one reciver to manage the messages of activities who interact with nodes.
 */
public class DataHolderService extends Service implements Observer, TreeVisitor {

    public static final String UPDATE_DATA = "updateData";
    public static final String STOP = "stop";
    public static final String ACTIVE_TASKS_DATA = "activeTasks";
    private DataManager dataManager;
    private Node rootNode;
    private Node currentNode;
    private ArrayList<Task> activeTasks = new ArrayList<>();
    private ArrayList<Node> path = new ArrayList<>();
    private BroadcastReceiver receiver;
    private boolean isLevelChanged = false;


    public DataHolderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int refreshRate = Integer.parseInt(preferences.getString(SettingsActivity.KEY_PREFERENCE_REFRESH_RATE, "1"));

        Clock.getInstance().setRefreshTicks(refreshRate);
        Clock.getInstance().addObserver(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle intentData = intent.getExtras();
                if (intentData != null && intentData.containsKey("type")) {
                    String type = intentData.getString("type");
                    int nodePosition;
                    switch (type) {
                        case TreeViewerActivity.HOME:
                            path.clear();
                            isLevelChanged = true;
                            currentNode = rootNode;
                            break;
                        case NodeAdapter.CHILDREN:
                            nodePosition = intentData.getInt("nodePosition");
                            Node newNode = (Node) ((Project) currentNode).getActivities().toArray()[nodePosition];
                            path.add(currentNode);
                            currentNode = newNode;
                            isLevelChanged = true;
                            break;
                        case TreeViewerActivity.PARENT:
                            if (currentNode.getParent() != null) {
                                currentNode = currentNode.getParent();
                                path.remove(path.size()-1);
                                isLevelChanged = true;
                            } else {
                                dataManager.saveData((Project) rootNode);
                                stopSelf();
                            }
                            break;
                        case NewNodeDialog.NEW_TASK:
                            Task task = new BaseTask(intentData.getString("taskName"), intentData.getString("taskDescription"), (Project) currentNode);
                            break;
                        case NewNodeDialog.NEW_PROJECT:
                            Project project = new Project(intentData.getString("projectName"), intentData.getString("projectDescription"), (Project) currentNode);
                            break;
                        case NodeAdapter.START:
                            nodePosition = intentData.getInt("nodePosition");
                            ((Task) ((Project) currentNode).getActivities().toArray()[nodePosition]).startInterval();
                            break;
                        case NodeAdapter.STOP:
                            nodePosition = intentData.getInt("nodePosition");
                            ((Task) ((Project) currentNode).getActivities().toArray()[nodePosition]).stopInterval();
                            break;
                        case NodeAdapter.REMOVE:
                            nodePosition = intentData.getInt("nodePosition");
                            ((ArrayList) ((Project) currentNode).getActivities()).remove(nodePosition);
                            break;
                        case NodeAdapter.EDIT:
                            nodePosition = intentData.getInt("nodePosition");
                            ((Node) ((ArrayList) ((Project) currentNode).getActivities()).get(nodePosition)).setName(intentData.getString("nodeName"));
                            ((Node) ((ArrayList) ((Project) currentNode).getActivities()).get(nodePosition)).setDescription(intentData.getString("nodeDescription"));
                            break;
                        case ActiveNodesActivity.ACTIVE_TASKS:
                            sendActiveTasks();
                            break;
                        case ActiveNodesActivity.PAUSE_ALL:
                            for (Task t : activeTasks) {
                                if (t.isActive()) {
                                    t.stopInterval();
                                }
                            }
                            sendActiveTasks();
                            break;
                        case ActiveNodesActivity.RESUME_ALL:
                            for (Task t : activeTasks) {
                                if (!t.isActive()) {
                                    t.startInterval();
                                }
                            }
                            sendActiveTasks();
                            break;
                        default:
                            break;

                    }
                    sendNewData();

                }
            }
        };

        if (rootNode == null) {
            if (dataManager == null) {
                dataManager = new DataManager(getFilesDir() + "/save.db");
                this.rootNode = (Node) dataManager.loadData();
                if (this.rootNode == null) {
                    rootNode = new Project("root", "", null);
                }
                currentNode = rootNode;
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(TreeViewerActivity.HOME);
        filter.addAction(TreeViewerActivity.PARENT);
        filter.addAction(NodeAdapter.CHILDREN);
        filter.addAction(NodeAdapter.REMOVE);
        filter.addAction(NodeAdapter.EDIT);
        filter.addAction(NewNodeDialog.NEW_PROJECT);
        filter.addAction(NewNodeDialog.NEW_TASK);
        filter.addAction(NodeAdapter.START);
        filter.addAction(NodeAdapter.STOP);
        filter.addAction(ActiveNodesActivity.ACTIVE_TASKS);
        filter.addAction(ActiveNodesActivity.PAUSE_ALL);
        filter.addAction(ActiveNodesActivity.RESUME_ALL);
        registerReceiver(this.receiver, filter);

        sendNewData();

        return Service.START_STICKY;
    }

    /**
     * Method that visits all the tree and add the active tasks to an array who it's sended via broadcast to ActiveNodesActivity.
     */
    private synchronized void sendActiveTasks() {
        activeTasks.clear();
        rootNode.accept(this);
        Intent broadcast = new Intent(ACTIVE_TASKS_DATA);
        broadcast.putExtra("activeTasks", activeTasks);
        sendBroadcast(broadcast);
    }

    @Override
    public void update(Observable o, Object arg) {
        sendNewData();
    }

    /**
     * Method that generetaes the new path to be shown.
     * @return string with path
     */
    private String getNewPath() {
        StringBuilder newPath = new StringBuilder();
        newPath.append("/");
        if (path.size() > 0) {
            for (int i = 1; i < path.size(); i++) {
                newPath.append(path.get(i).getName());
                newPath.append("/");
            }
            if (currentNode != null) {
                newPath.append(currentNode.getName());
            }
        }
        return newPath.toString();
    }

    /**
     * method used on update method. it sends the data of currentNode and it's path.
     */
    private void sendNewData() {
        Intent broadcast = new Intent(UPDATE_DATA);
        if (isLevelChanged) {
            broadcast.putExtra("updateDial", 0);
            isLevelChanged = false;
        }
        broadcast.putExtra("node", currentNode);
        broadcast.putExtra("path", getNewPath());
        sendBroadcast(broadcast);
    }



    @Override
    public void onDestroy() {
        path.clear();
        unregisterReceiver(this.receiver);
        super.onDestroy();
    }

    @Override
    public void visitProject(Project project) {
        for (Node n : project.getActivities()) {
            if (n.isTask()) {
                n.accept(this);
            }
        }
    }

    @Override
    public void visitTask(Task task) {
        if (task.isActive()) {
            activeTasks.add(task);
        }
    }

    @Override
    public void visitInterval(Interval interval) {

    }
}

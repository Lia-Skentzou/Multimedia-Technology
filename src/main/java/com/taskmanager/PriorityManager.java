package com.taskmanager;

import com.taskmanager.utils.JsonUtil;
import java.util.ArrayList;
import java.util.List;

public class PriorityManager {
    private List<Priority> priorities;
    private TaskManager taskManager;
    private static final String FILE_PATH = "medialab/priorities.json"; //priorities info will be saved to this json file: priorities.json

    // constructor
    public PriorityManager(TaskManager taskManager) {
        this.priorities = new ArrayList<>();
        // default priority is already created 
        this.priorities.add(new Priority("Default"));
        this.taskManager = taskManager;
    }

    // set a taskmanager associated with the priority manager
    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // load priority information from json file
    public void loadFromJson() {
        this.priorities = new ArrayList<>(JsonUtil.readFromJsonFile(FILE_PATH, Priority[].class));
        System.out.println("Loaded " + priorities.size() + " priorities from JSON.");
    }

    // save priority information to json file
    public void saveToJson() {
        JsonUtil.writeToJsonFile(FILE_PATH, priorities);
        System.out.println("Saved priorities to JSON.");
    }

    // get all priorities that have been created 
    public List<Priority> getAllPriorities() {
        return priorities;
    }

    // add a new priority if it does not already exist
    public boolean addPriority(Priority priority) {
        for (Priority existingPriority : priorities) {
            if (existingPriority.getName().equalsIgnoreCase(priority.getName())) {
                return false;
            }
        }
        priorities.add(priority);
        return true;
    }

    // update a priority name -> tasks with this priority are also updated 
    public boolean updatePriority(String oldName, String newName) {
        for (Priority priority : priorities) {
            if (priority.getName().equalsIgnoreCase(oldName) && !oldName.equalsIgnoreCase("Default")) {
                priority.setName(newName);
                // Update tasks that have the old priority name to use the new one
                taskManager.updateTasksWithPriority(oldName, new Priority(newName));
                return true;
            }
        }
        return false;
    }

    // delete a priority -> all tasks with this priority will then have priority "Default"
    public void removePriority(String priorityName) {
        if (!priorityName.equalsIgnoreCase("Default")) {
            // Update tasks using the removed priority to use the default priority.
            taskManager.updateTasksWithPriority(priorityName, new Priority("Default"));
            priorities.removeIf(priority -> priority.getName().equalsIgnoreCase(priorityName));
        }
    }
}

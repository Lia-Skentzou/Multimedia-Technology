package com.taskmanager;

import com.taskmanager.utils.JsonUtil;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private List<Task> tasks;
    private ReminderManager reminderManager; // task manager includes reminder manager
    private CategoryManager categoryManager; // task manager includes category manager
    private PriorityManager priorityManager; // task manager includes priority manager
    private static final String FILE_PATH = "medialab/tasks.json";  // tasks info will be saved to this json file: tasks.json

    // constructor
    public TaskManager(ReminderManager reminderManager, CategoryManager categoryManager, PriorityManager priorityManager) {
        this.tasks = new ArrayList<>();
        this.reminderManager = reminderManager;
        this.categoryManager = categoryManager;
        this.priorityManager = priorityManager;
    }

    // set a priority manager associated with the task manager
    public void setPriorityManager(PriorityManager priorityManager) {
        this.priorityManager = priorityManager;
    }
    
    // load task information from json file
    public void loadFromJson() {

        List<Task> loadedTasks = JsonUtil.readFromJsonFile(FILE_PATH, Task[].class);
        
        if (loadedTasks == null || loadedTasks.isEmpty()) {
            System.out.println("No tasks found in JSON, keeping the existing list.");
            return; // Don't overwrite tasks if loading failed
        }
        
        this.tasks = new ArrayList<>(loadedTasks);
    
        for (Task task : tasks) {
            Optional<Category> existingCategory = categoryManager.getCategoryByName(task.getCategory().getName());
            if (existingCategory.isPresent()) {
                task.setCategory(existingCategory.get()); // assign existing category
            } else {
                categoryManager.addCategory(task.getCategory()); // add new category if needed
            }
        }
    }
    
    // save task information to json file
    public void saveToJson() {
        JsonUtil.writeToJsonFile(FILE_PATH, tasks);
        System.out.println("Saved tasks to JSON.");
    }

    // private helper to add a task object to the list 
    private void addTaskInternal(Task task) {
        Optional<Category> existingCategory = categoryManager.getCategoryByName(task.getCategory().getName());
        if (existingCategory.isPresent()) {
            task.setCategory(existingCategory.get());
        } else {
            categoryManager.addCategory(task.getCategory());
        }
        tasks.add(task);
    }
    
    // add a new task from provided parameters
    // we cannot create a task with state "Delayed"
    public boolean addTask(String title, String description, String categoryName, 
    String priorityName, LocalDate dueDate, TaskStatus status) {
        // title, category, and due date must be provided
        if (title == null || title.isEmpty() || categoryName == null || categoryName.isEmpty() || dueDate == null) {
        return false;
        }

        // ensure that when creating a task, its state is not set to DELAYED
        if (status == TaskStatus.DELAYED) {
        status = TaskStatus.OPEN;
        }

        // get th existing category or create a new one if needed
        Category category = categoryManager.getOrCreateCategory(categoryName);

        // default priority is "Default"
        Priority priority = new Priority((priorityName == null || priorityName.isEmpty()) ? "Default" : priorityName);
        boolean exists = priorityManager.getAllPriorities().stream()
        .anyMatch(p -> p.getName().equalsIgnoreCase(priority.getName()));
        if (!exists) {
        priorityManager.addPriority(priority);
        }

        // create and add the new task
        Task newTask = new Task(title, description, category, priority, dueDate, status);
        addTaskInternal(newTask);
        return true;
        }


    // update an existing task, we can not set a task state to delayed
    public boolean updateTask(String taskId, String title, String description, 
            String categoryName, String priorityName, LocalDate dueDate, TaskStatus newStatus) {
        Task task = getTask(taskId);
        if (task == null) {
            return false; // task not found
        }

        // if the task is already in delayed state, do not allow update
        if (task.getStatus() == TaskStatus.DELAYED) {
            System.out.println("Error: Cannot update a task that is already DELAYED.");
            return false;
        }

        // update basic fields
        task.setTitle(title);
        task.setDescription(description);

        // get or create the category if needed 
        Optional<Category> existingCat = categoryManager.getCategoryByName(categoryName);
        Category category = existingCat.orElseGet(() -> {
            Category newCategory = new Category(categoryName);
            categoryManager.addCategory(newCategory);
            return newCategory;
        });
        task.setCategory(category);

        // determine the priority, if no input given then set priority to default
        Priority priority = new Priority((priorityName == null || priorityName.isEmpty()) ? "Default" : priorityName);
        boolean exists = priorityManager.getAllPriorities().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(priority.getName()));
        if (!exists) {
            priorityManager.addPriority(priority);
        }
        task.setPriority(priority);

        // update due date
        task.setDueDate(dueDate);
        // update status
        task.setStatus(newStatus);

        // if the updated status is completed then remove all the reminders for this task
        if (task.getStatus() == TaskStatus.COMPLETED) {
            reminderManager.removeRemindersForTask(task);
        }
        return true;
    }


    // remove a task -> all reminders for this task are also removed
    public void removeTask(String taskId) {
        Task taskToRemove = getTask(taskId);
        if (taskToRemove != null) {
            reminderManager.removeRemindersForTask(taskToRemove);
            tasks.removeIf(task -> task.getId().equals(taskId));
        }
    }


    // search tasks based on title, category, priority or a combination of this info 
    public List<Task> searchTasks(String title, String category, String priority) {
        return tasks.stream()
                .filter(task -> (title == null || title.isEmpty() || task.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(task -> (category == null || category.isEmpty() || task.getCategory().getName().equalsIgnoreCase(category)))
                .filter(task -> (priority == null || priority.isEmpty() || task.getPriority().getName().equalsIgnoreCase(priority)))
                .collect(Collectors.toList());
    }


    // get a specific task object
    public Task getTask(String taskId) {
        return tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst().orElse(null);
    }

    // get all tasks that have been created
    public List<Task> getAllTasks() {
        return tasks;
    }

    // get number of total tasks
    public int getTotalTasksCount() {
        return tasks.size();
    }

    // get number of tasks from each category 
    public int getTaskCountByCategory(Category category) {
        return (int) tasks.stream()
                .filter(task -> task.getCategory().equals(category))
                .count();
    }

    // get number of completed tasks
    public int getCompletedTaskCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    // get number of delayed tasks
    public int getDelayedTaskCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
    }

    // get number of tasks with duedate within the next 7 days
    public int getUpcomingTaskCount() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);
        return (int) tasks.stream()
                .filter(task -> !task.getDueDate().isBefore(today) && !task.getDueDate().isAfter(sevenDaysFromNow))
                .count();
    }
    

    // get tasks organized by category 
    public Map<Category, List<Task>> getTasksGroupedByCategory() {
        return tasks.stream().collect(Collectors.groupingBy(Task::getCategory));
    }

    // update a task status if status is not delayed or completed 
    public void updateTaskStatuses() {
        LocalDate today = LocalDate.now();
        for (Task task : tasks) {
            if (!task.getStatus().equals(TaskStatus.COMPLETED) && task.getDueDate().isBefore(today)) {
                task.setStatus(TaskStatus.DELAYED);
            }
        }
    }

    // remove tasks that belong to a given category 
    public void removeTasksByCategory(String categoryName) {
        tasks.removeIf(task -> {
            reminderManager.removeRemindersForTask(task);
            return task.getCategory().getName().equalsIgnoreCase(categoryName);
        });
    }

    // update the task with oldpriority to have newpriority
    public void updateTasksWithPriority(String oldPriorityName, Priority newPriority) {
        for (Task task : tasks) {
            if (task.getPriority().getName().equalsIgnoreCase(oldPriorityName)) {
                task.setPriority(newPriority);
            }
        }
    }
    
    // get all the delayed tasks and check if thre are any new delayed tasks based on current day and due dates
    public List<Task> getDelayedTasks() {
        LocalDate today = LocalDate.now();
        for (Task task : tasks) {
            if (!task.getStatus().equals(TaskStatus.COMPLETED) && task.getDueDate().isBefore(today)) {
                task.setStatus(TaskStatus.DELAYED);
            }
        }
        return tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                    .collect(Collectors.toList());
    }
    
}

package com.taskmanager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppController {
    private TaskManager taskManager;
    private CategoryManager categoryManager;
    private PriorityManager priorityManager;
    private ReminderManager reminderManager;

    // UI component references (set later from the view)
    private VBox taskDisplay;
    private VBox categoryDisplay;
    private VBox priorityDisplay;
    private VBox reminderDisplay;
    private Label taskCountLabel;
    private Label completedTaskCountLabel;
    private Label delayedTaskCountLabel;
    private Label upcomingTaskCountLabel;

    public AppController() {
        // initialize managers and set relationships
        categoryManager = new CategoryManager(null);
        reminderManager = new ReminderManager();
        taskManager = new TaskManager(reminderManager, categoryManager, null);
        priorityManager = new PriorityManager(taskManager);
        taskManager.setPriorityManager(priorityManager);
        categoryManager.setTaskManager(taskManager);
        // Load data from JSON files
        initializeDataFromJson();
    }

    // pass UI components from the view to the controller
    public void setUIComponents(VBox taskDisplay, VBox categoryDisplay, VBox priorityDisplay, VBox reminderDisplay,
                                Label taskCountLabel, Label completedTaskCountLabel, Label delayedTaskCountLabel,
                                Label upcomingTaskCountLabel, TextField searchTitleField, TextField searchCategoryField,
                                TextField searchPriorityField) {
        this.taskDisplay = taskDisplay;
        this.categoryDisplay = categoryDisplay;
        this.priorityDisplay = priorityDisplay;
        this.reminderDisplay = reminderDisplay;
        this.taskCountLabel = taskCountLabel;
        this.completedTaskCountLabel = completedTaskCountLabel;
        this.delayedTaskCountLabel = delayedTaskCountLabel;
        this.upcomingTaskCountLabel = upcomingTaskCountLabel;
    }

    private void initializeDataFromJson() {
        // load data fom json files 
        categoryManager.loadFromJson();
        taskManager.loadFromJson();
        priorityManager.loadFromJson();
        reminderManager.loadFromJson();
        // remove past reminders if they exist, when the app starts 
        reminderManager.removePastReminders();
    }

    // update task statistics ui display 
    public void updateTaskStatistics() {
        taskCountLabel.setText("Total Tasks: " + taskManager.getTotalTasksCount());
        completedTaskCountLabel.setText("Completed Tasks: " + taskManager.getCompletedTaskCount());
        delayedTaskCountLabel.setText("Delayed Tasks: " + taskManager.getDelayedTaskCount());
        upcomingTaskCountLabel.setText("Upcoming (7 days): " + taskManager.getUpcomingTaskCount());
    }

    // update task ui display 
    public void updateTaskDisplay(List<Task> tasksToDisplay) {
        taskDisplay.getChildren().clear();
        Map<Category, List<Task>> categorizedTasks = tasksToDisplay.stream()
                .collect(Collectors.groupingBy(Task::getCategory));
        for (Map.Entry<Category, List<Task>> entry : categorizedTasks.entrySet()) {
            Category category = entry.getKey();
            List<Task> tasks = entry.getValue();
            Label categoryLabel = new Label(category.getName() + " (" + tasks.size() + " tasks)");
            categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
            VBox taskContainer = new VBox(15);
            for (Task task : tasks) {
                VBox taskBox = createTaskBox(task);
                taskContainer.getChildren().add(taskBox);
            }
            VBox categoryBox = new VBox(10, categoryLabel, taskContainer);
            categoryBox.setPadding(new Insets(10));
            categoryBox.setStyle("-fx-background-color: #bdc3c7; -fx-background-radius: 10;");
            taskDisplay.getChildren().add(categoryBox);
        }
    }

    // overload to update all tasks
    public void updateTaskDisplay() {
        updateTaskDisplay(taskManager.getAllTasks());
    }

    // helper to create a task UI box
    private VBox createTaskBox(Task task) {
        Label titleLabel = new Label("Title: " + task.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label descriptionLabel = new Label("Description: " + task.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label dueDateLabel = new Label("Due: " + task.getDueDate());
        Label priorityLabel = new Label("Priority: " + task.getPriority().getName());
        Label stateLabel = new Label("State: " + task.getStatus());
        dueDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        priorityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        stateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        HBox detailsRow = new HBox(15, dueDateLabel, priorityLabel, stateLabel);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Button updateButton = new Button("Update Task");
        updateButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5;");
        updateButton.setOnAction(e -> showUpdateTaskDialog(task));

        Button deleteButton = new Button("Delete Task");
        deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this task?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                taskManager.removeTask(task.getId());
                updateTaskDisplay();
                updateTaskStatistics();
                updateCategoryDisplay();
                updateReminderDisplay();
            }
        });

        Button addReminderButton = new Button("Add Reminder");
        addReminderButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 5;");
        addReminderButton.setOnAction(e -> showAddReminderDialog(task));

        btnBox.getChildren().addAll(updateButton, deleteButton, addReminderButton);
        VBox taskBox = new VBox(5, titleLabel, descriptionLabel, detailsRow, btnBox);
        taskBox.setPadding(new Insets(10));
        taskBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 8; -fx-background-radius: 8;");
        return taskBox;
    }

    // update category ui display 
    public void updateCategoryDisplay() {
        categoryDisplay.getChildren().clear();
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label header = new Label("Categories");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        addCategoryButton.setOnAction(e -> showAddCategoryDialog());
        headerBox.getChildren().addAll(header, addCategoryButton);
        categoryDisplay.getChildren().add(headerBox);
        for (Category cat : categoryManager.getAllCategories()) {
            VBox catBox = new VBox(5);
            catBox.setPadding(new Insets(5));
            catBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 8; -fx-background-radius: 8;");
            catBox.setAlignment(Pos.CENTER);
            Label catLabel = new Label(cat.getName());
            catLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            HBox btnBox = new HBox(5);
            btnBox.setAlignment(Pos.CENTER);
            Button updateButton = new Button("Update");
            updateButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5;");
            updateButton.setOnAction(e -> showUpdateCategoryDialog(cat));
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5;");
            deleteButton.setOnAction(e -> {
                categoryManager.removeCategory(cat.getName());
                updateCategoryDisplay();
                updateTaskDisplay();
                updateTaskStatistics();
            });
            btnBox.getChildren().addAll(updateButton, deleteButton);
            catBox.getChildren().addAll(catLabel, btnBox);
            categoryDisplay.getChildren().add(catBox);
        }
    }

    // update priority ui display 
    public void updatePriorityDisplay() {
        priorityDisplay.getChildren().clear();
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label header = new Label("Priorities");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        headerBox.getChildren().add(header);
        priorityDisplay.getChildren().add(headerBox);
        for (Priority priority : priorityManager.getAllPriorities()) {
            VBox prioBox = new VBox(5);
            prioBox.setPadding(new Insets(5));
            prioBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 8; -fx-background-radius: 8;");
            prioBox.setAlignment(Pos.CENTER);
            Label prioLabel = new Label(priority.getName());
            prioLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            HBox btnBox = new HBox(5);
            btnBox.setAlignment(Pos.CENTER);
            Button updateButton = new Button("Update");
            updateButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5;");
            updateButton.setOnAction(e -> showUpdatePriorityDialog(priority));
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5;");
            deleteButton.setOnAction(e -> {
                priorityManager.removePriority(priority.getName());
                updatePriorityDisplay();
                updateTaskDisplay();
            });
            btnBox.getChildren().addAll(updateButton, deleteButton);
            prioBox.getChildren().addAll(prioLabel, btnBox);
            priorityDisplay.getChildren().add(prioBox);
        }
    }

    // update reminder ui display 
    public void updateReminderDisplay() {
        reminderDisplay.getChildren().clear();
        Label header = new Label("Active Reminders:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        reminderDisplay.getChildren().add(header);
        List<Reminder> reminders = reminderManager.getAllReminders();
        if (reminders.isEmpty()) {
            Label none = new Label("No active reminders");
            none.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            reminderDisplay.getChildren().add(none);
        } else {
            for (Reminder reminder : reminders) {
                HBox reminderBox = new HBox(10);
                reminderBox.setAlignment(Pos.CENTER_LEFT);
                reminderBox.setPadding(new Insets(5));
                reminderBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
                String text = "Reminder (" + reminder.getReminderType() + "): Task: " 
                        + reminder.getTask().getTitle() + " | Date: " + reminder.getReminderDate();
                Label reminderLabel = new Label(text);
                reminderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                Button updateReminderButton = new Button("Update");
                updateReminderButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5;");
                updateReminderButton.setOnAction(e -> showUpdateReminderDialog(reminder));
                Button deleteReminderButton = new Button("Delete");
                deleteReminderButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteReminderButton.setOnAction(e -> {
                    reminderManager.removeReminder(reminder);
                    updateReminderDisplay();
                });
                reminderBox.getChildren().addAll(reminderLabel, spacer, updateReminderButton, deleteReminderButton);
                reminderDisplay.getChildren().add(reminderBox);
            }
        }
    }

    // due reminder popup sequence
    public void showDueRemindersSequentially() {
        List<Reminder> dueReminders = reminderManager.getAllReminders().stream()
                .filter(r -> r.getReminderDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
        showDueRemindersRecursive(dueReminders, 0);
    }

    private void showDueRemindersRecursive(List<Reminder> reminders, int index) {
        if (index >= reminders.size()) {
            return;
        }
        Reminder r = reminders.get(index);
        Task t = r.getTask();
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Reminder Due");
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #2980b9;");
        Label titleLabel = new Label("Title: " + t.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label descriptionLabel = new Label("Description: " + t.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px;");
        Label priorityLabel = new Label("Priority: " + t.getPriority().getName());
        priorityLabel.setStyle("-fx-font-size: 14px;");
        Label dueDateLabel = new Label("Due Date: " + t.getDueDate());
        dueDateLabel.setStyle("-fx-font-size: 14px;");
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        okButton.setOnAction(ev -> popup.close());
        vbox.getChildren().addAll(titleLabel, descriptionLabel, priorityLabel, dueDateLabel, okButton);
        Scene scene = new Scene(vbox, 350, 250);
        popup.setScene(scene);
        popup.showAndWait();
        reminderManager.removeReminder(r);
        updateReminderDisplay();
        showDueRemindersRecursive(reminders, index + 1);
    }

    // display shown when update reminder button is clicked 
    public void showUpdateReminderDialog(Reminder oldReminder) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Reminder for Task: " + oldReminder.getTask().getTitle());
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        Label typeLabel = new Label("Select Reminder Type:");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        ComboBox<ReminderType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(ReminderType.ONE_DAY_BEFORE, ReminderType.ONE_WEEK_BEFORE, ReminderType.ONE_MONTH_BEFORE, ReminderType.SPECIFIC_DATE);
        typeBox.setValue(oldReminder.getReminderType());
        DatePicker specificDatePicker = new DatePicker();
        specificDatePicker.setVisible(oldReminder.getReminderType() == ReminderType.SPECIFIC_DATE);
        specificDatePicker.setValue(oldReminder.getReminderType() == ReminderType.SPECIFIC_DATE 
                ? oldReminder.getReminderDate() 
                : oldReminder.getTask().getDueDate().minusDays(1));
        typeBox.setOnAction(e -> {
            if (typeBox.getValue() == ReminderType.SPECIFIC_DATE) {
                specificDatePicker.setVisible(true);
            } else {
                specificDatePicker.setVisible(false);
            }
        });
        Button updateButton = new Button("Update Reminder");
        updateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        updateButton.setOnAction(e -> {
            try {
                Reminder newReminder;
                if (typeBox.getValue() == ReminderType.SPECIFIC_DATE) {
                    LocalDate specificDate = specificDatePicker.getValue();
                    if (specificDate == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a specific date!", ButtonType.OK);
                        alert.show();
                        return;
                    }
                    newReminder = new Reminder(oldReminder.getTask(), specificDate);
                } else {
                    newReminder = new Reminder(oldReminder.getTask(), typeBox.getValue());
                }
                boolean updated = reminderManager.updateReminder(oldReminder, newReminder);
                if (updated) {
                    updateReminderDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Update failed!", ButtonType.OK);
                    alert.show();
                }
            } catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(typeLabel, typeBox, specificDatePicker, updateButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // display shown when add reminder button is clicked 
    public void showAddReminderDialog(Task task) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Reminder for Task: " + task.getTitle());
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        
        Label typeLabel = new Label("Select Reminder Type:");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ComboBox<ReminderType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(ReminderType.ONE_DAY_BEFORE, ReminderType.ONE_WEEK_BEFORE, 
                                    ReminderType.ONE_MONTH_BEFORE, ReminderType.SPECIFIC_DATE);
        typeBox.setValue(ReminderType.ONE_DAY_BEFORE);
        
        DatePicker specificDatePicker = new DatePicker();
        specificDatePicker.setVisible(false);
        specificDatePicker.setValue(task.getDueDate().minusDays(1));
        
        typeBox.setOnAction(e -> {
            if (typeBox.getValue() == ReminderType.SPECIFIC_DATE) {
                specificDatePicker.setVisible(true);
            } else {
                specificDatePicker.setVisible(false);
            }
        });
        
        Button addButton = new Button("Add Reminder");
        addButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 5;");
        addButton.setOnAction(e -> {
            try {
                Reminder reminder;
                if (typeBox.getValue() == ReminderType.SPECIFIC_DATE) {
                    LocalDate specificDate = specificDatePicker.getValue();
                    if (specificDate == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a specific date!", ButtonType.OK);
                        alert.show();
                        return;
                    }
                    reminder = new Reminder(task, specificDate);
                } else {
                    reminder = new Reminder(task, typeBox.getValue());
                }
                // check for duplicate reminder if necessary
                if (reminderManager.reminderExists(reminder)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "This reminder already exists for the task!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
                boolean added = reminderManager.addReminder(reminder);
                if (added) {
                    updateReminderDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot add reminder for completed tasks!", ButtonType.OK);
                    alert.show();
                }
            } catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                alert.show();
            }
        });
        
        dialogVBox.getChildren().addAll(typeLabel, typeBox, specificDatePicker, addButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    

    // display shown when update priority button is clicked 
    public void showUpdatePriorityDialog(Priority priority) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Priority");
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        TextField newNameField = new TextField();
        newNameField.setPromptText("New Priority Name");
        newNameField.setText(priority.getName());
        Button updateButton = new Button("Update Priority");
        updateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        updateButton.setOnAction(e -> {
            String newName = newNameField.getText().trim();
            if (!newName.isEmpty()) {
                boolean success = priorityManager.updatePriority(priority.getName(), newName);
                if (success) {
                    updatePriorityDisplay();
                    updateTaskDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Update failed!", ButtonType.OK);
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "New priority name cannot be empty!", ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(new Label("New Priority Name:"), newNameField, updateButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // display shown when update category button is clicked 
    public void showUpdateCategoryDialog(Category category) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Category");
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        TextField newNameField = new TextField();
        newNameField.setPromptText("New Category Name");
        newNameField.setText(category.getName());
        Button updateButton = new Button("Update Category");
        updateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        updateButton.setOnAction(e -> {
            String newName = newNameField.getText().trim();
            if (!newName.isEmpty()) {
                boolean success = categoryManager.updateCategory(category.getName(), newName);
                if (success) {
                    updateCategoryDisplay();
                    updateTaskDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Update failed!", ButtonType.OK);
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "New category name cannot be empty!", ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(new Label("New Category Name:"), newNameField, updateButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // display shown when add category button is clicked 
    public void showAddCategoryDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Category");
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category Name");
        Button saveButton = new Button("Save Category");
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            String name = categoryField.getText().trim();
            if (!name.isEmpty()) {
                if (categoryManager.categoryExists(name)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Category '" + name + "' already exists!", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    categoryManager.addCategory(new Category(name));
                    updateCategoryDisplay();
                    dialog.close();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Category name cannot be empty!", ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(new Label("Category Name:"), categoryField, saveButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // display shown when add task button is clicked 
    public void showAddTaskDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Task");
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category Name");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority Name (Optional, default: 'Default')");
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDate.now().plusDays(1));
        ComboBox<TaskStatus> statusBox = new ComboBox<>();
        for (TaskStatus status : TaskStatus.values()) {
            if (status != TaskStatus.DELAYED) {
                statusBox.getItems().add(status);
            }
        }
        statusBox.setValue(TaskStatus.OPEN);
        Button saveButton = new Button("Save Task");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            if (!titleField.getText().isEmpty() && !categoryField.getText().isEmpty() && dueDatePicker.getValue() != null) {
                boolean success = taskManager.addTask(
                        titleField.getText(),
                        descriptionField.getText(),
                        categoryField.getText(),
                        priorityField.getText(),
                        dueDatePicker.getValue(),
                        statusBox.getValue()
                );
                if (success) {
                    taskManager.saveToJson();
                    updateTaskDisplay();
                    updateTaskStatistics();
                    updateCategoryDisplay();
                    updatePriorityDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all required fields!", ButtonType.OK);
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all required fields!", ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionField,
                new Label("Category:"), categoryField,
                new Label("Priority:"), priorityField,
                new Label("Due Date:"), dueDatePicker,
                new Label("Status:"), statusBox,
                saveButton
        );
        Scene dialogScene = new Scene(dialogVBox, 300, 450);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // display shown when update task button is clicked 
    public void showUpdateTaskDialog(Task task) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Task");
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(15));
        dialogVBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10;");
        TextField titleField = new TextField(task.getTitle());
        TextField descriptionField = new TextField(task.getDescription());
        TextField categoryField = new TextField(task.getCategory().getName());
        TextField priorityField = new TextField(task.getPriority().getName());
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        ComboBox<TaskStatus> statusBox = new ComboBox<>();
        for (TaskStatus status : TaskStatus.values()) {
            if (status != TaskStatus.DELAYED) {
                statusBox.getItems().add(status);
            }
        }
        if (task.getStatus() == TaskStatus.DELAYED) {
            statusBox.setValue(TaskStatus.OPEN);
        } else {
            statusBox.setValue(task.getStatus());
        }
        Button saveButton = new Button("Update Task");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            if (!titleField.getText().isEmpty() && !categoryField.getText().isEmpty() && dueDatePicker.getValue() != null) {
                boolean success = taskManager.updateTask(
                        task.getId(),
                        titleField.getText(),
                        descriptionField.getText(),
                        categoryField.getText(),
                        priorityField.getText(),
                        dueDatePicker.getValue(),
                        statusBox.getValue()
                );
                if (success) {
                    updateTaskDisplay();
                    updateTaskStatistics();
                    updateCategoryDisplay();
                    updateReminderDisplay();
                    updatePriorityDisplay();
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot update a task that is already DELAYED!", ButtonType.OK);
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all required fields!", ButtonType.OK);
                alert.show();
            }
        });
        dialogVBox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionField,
                new Label("Category:"), categoryField,
                new Label("Priority:"), priorityField,
                new Label("Due Date:"), dueDatePicker,
                new Label("Status:"), statusBox,
                saveButton
        );
        Scene dialogScene = new Scene(dialogVBox, 500, 450);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    // getters for managers 
    public TaskManager getTaskManager() {
        return taskManager;
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public PriorityManager getPriorityManager() {
        return priorityManager;
    }

    public ReminderManager getReminderManager() {
        return reminderManager;
    }
    
    // popup of delayed tasks
    public void showDelayedTasksPopup() {
        List<Task> delayedTasks = taskManager.getDelayedTasks();
        if (delayedTasks.isEmpty()) {
            return;
        }
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Delayed Tasks Alert");
        VBox popupVBox = new VBox(10);
        popupVBox.setPadding(new Insets(15));
        popupVBox.setAlignment(Pos.CENTER);
        popupVBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e74c3c;");
        Label header = new Label("Total Delayed Tasks: " + delayedTasks.size());
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        popupVBox.getChildren().add(header);
        for (Task task : delayedTasks) {
            Label taskLabel = new Label(task.getTitle() + " (Due: " + task.getDueDate() + ")");
            taskLabel.setStyle("-fx-font-size: 13px;");
            popupVBox.getChildren().add(taskLabel);
        }
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> popup.close());
        popupVBox.getChildren().add(closeButton);
        Scene scene = new Scene(popupVBox, 400, 300);
        popup.setScene(scene);
        popup.showAndWait();
    }
}

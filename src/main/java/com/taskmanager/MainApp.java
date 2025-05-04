package com.taskmanager;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private AppController controller;

    // UI components
    private VBox taskDisplay;
    private VBox categoryDisplay;
    private VBox priorityDisplay;
    private VBox reminderDisplay;
    private Label taskCountLabel;
    private Label completedTaskCountLabel;
    private Label delayedTaskCountLabel;
    private Label upcomingTaskCountLabel;
    private TextField searchTitleField;
    private TextField searchCategoryField;
    private TextField searchPriorityField;

    @Override
    public void start(Stage primaryStage) {
        controller = new AppController();

        // build top container: task statistics and reminders list
        VBox topContainer = new VBox(10);
        topContainer.setPadding(new Insets(15));
        topContainer.setStyle("-fx-background-color: grey; -fx-background-radius: 10;");
        HBox topSection = new HBox(20);
        topSection.setPadding(new Insets(15));
        topSection.setAlignment(Pos.CENTER);
        topSection.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10;");
        taskCountLabel = new Label("Total Tasks: 0");
        taskCountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        completedTaskCountLabel = new Label("Completed Tasks: 0");
        completedTaskCountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        delayedTaskCountLabel = new Label("Delayed Tasks: 0");
        delayedTaskCountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        upcomingTaskCountLabel = new Label("Upcoming (7 days): 0");
        upcomingTaskCountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        topSection.getChildren().addAll(taskCountLabel, completedTaskCountLabel, delayedTaskCountLabel, upcomingTaskCountLabel);

        reminderDisplay = new VBox(5);
        reminderDisplay.setPadding(new Insets(10));
        reminderDisplay.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;");

        topContainer.getChildren().addAll(topSection, reminderDisplay);

        // left section: categories panel
        categoryDisplay = new VBox(10);
        categoryDisplay.setPadding(new Insets(15));
        categoryDisplay.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");

        // center section: search panel and task display
        taskDisplay = new VBox(10);
        taskDisplay.setPadding(new Insets(20));
        ScrollPane centerScroll = new ScrollPane(taskDisplay);
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox centerContainer = new VBox(10);
        centerContainer.setPadding(new Insets(15));
        HBox searchPanel = createSearchPanel();
        centerContainer.getChildren().addAll(searchPanel, centerScroll);

        // right section: priorities panel
        priorityDisplay = new VBox(10);
        priorityDisplay.setPadding(new Insets(15));
        priorityDisplay.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");

        // bottom section: add task button
        HBox bottomSection = new HBox();
        bottomSection.setPadding(new Insets(15));
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-background-color: #f4f4f4;");
        Button addTaskButton = new Button("Add Task");
        addTaskButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        addTaskButton.setOnAction(e -> controller.showAddTaskDialog());
        bottomSection.getChildren().add(addTaskButton);

        // root layout: BorderPane
        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setLeft(categoryDisplay);
        root.setCenter(centerContainer);
        root.setRight(priorityDisplay);
        root.setBottom(bottomSection);
        root.setPadding(new Insets(10));
        root.setEffect(new DropShadow(10, Color.gray(0.3)));

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setTitle("MediaLabAssistant");
        primaryStage.setScene(scene);
        primaryStage.show();

        // pass UI component references to controller
        controller.setUIComponents(taskDisplay, categoryDisplay, priorityDisplay, reminderDisplay,
                taskCountLabel, completedTaskCountLabel, delayedTaskCountLabel, upcomingTaskCountLabel,
                searchTitleField, searchCategoryField, searchPriorityField);

        // update UI from controller
        controller.updateTaskDisplay();
        controller.updateCategoryDisplay();
        controller.updatePriorityDisplay();
        controller.updateReminderDisplay();
        controller.updateTaskStatistics();

        // show delayed tasks and due reminders
        controller.showDelayedTasksPopup();
        controller.showDueRemindersSequentially();
    }

    // create a search panel 
    private HBox createSearchPanel() {
        HBox searchPanel = new HBox(10);
        searchPanel.setPadding(new Insets(10));
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        searchPanel.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #bdc3c7;");
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        searchTitleField = new TextField();
        searchTitleField.setPromptText("Search Title");
        searchCategoryField = new TextField();
        searchCategoryField.setPromptText("Search Category");
        searchPriorityField = new TextField();
        searchPriorityField.setPromptText("Search Priority");
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5;");
        searchButton.setOnAction(e -> {
            String title = searchTitleField.getText();
            String category = searchCategoryField.getText();
            String priority = searchPriorityField.getText();
            controller.updateTaskDisplay(controller.getTaskManager().searchTasks(title, category, priority));
        });
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> {
            searchTitleField.clear();
            searchCategoryField.clear();
            searchPriorityField.clear();
            controller.updateTaskDisplay();
        });
        searchPanel.getChildren().addAll(searchLabel, searchTitleField, searchCategoryField, searchPriorityField, searchButton, clearButton);
        return searchPanel;
    }

    @Override
    public void stop() throws Exception {
        // save all changes made during the app was open to json files
        controller.getTaskManager().saveToJson();
        controller.getCategoryManager().saveToJson();
        controller.getPriorityManager().saveToJson();
        controller.getReminderManager().saveToJson();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

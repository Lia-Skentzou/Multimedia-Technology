package com.taskmanager;

import java.time.LocalDate;
import java.util.UUID;

    
public class Task {
        private String id; // we give tasks an id in order to be able to trace them afterwards
        private String title;
        private String description;
        private Category category;
        private Priority priority;
        private LocalDate dueDate;
        private TaskStatus status;
    
        // default constructor 
        public Task() {}
    
        // constructor
        public Task(String title, String description, Category category, Priority priority, LocalDate dueDate, TaskStatus status) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.description = description;
            this.category = category;
            this.priority = (priority != null) ? priority : new Priority("Default");
            this.dueDate = dueDate;
            this.status = (status != null) ? status : TaskStatus.OPEN;
        }

        // getters and setters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }
        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { 
            this.priority = (priority != null) ? priority : new Priority("Default"); 
        }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
}

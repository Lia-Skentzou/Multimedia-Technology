package com.taskmanager;

import java.time.LocalDate;

public class Reminder {
    private Task task;
    private LocalDate reminderDate;
    private ReminderType reminderType;

    // default constructor 
    public Reminder() {}

    // constructor of reminder of specific type
    public Reminder(Task task, ReminderType reminderType) {
        this.task = task;
        this.reminderType = reminderType;
        this.reminderDate = calculateReminderDate();

        validateReminderDate(this.reminderDate);
    }

    // constructor of reminder of specific date
    public Reminder(Task task, LocalDate specificDate) {
        this.task = task;
        this.reminderType = ReminderType.SPECIFIC_DATE;
        this.reminderDate = specificDate;

        validateReminderDate(this.reminderDate);
    }

    // calculate date of the reminder based on type of reminder selected 
    private LocalDate calculateReminderDate() {
        switch (reminderType) {
            case ONE_DAY_BEFORE:
                return task.getDueDate().minusDays(1);
            case ONE_WEEK_BEFORE:
                return task.getDueDate().minusWeeks(1);
            case ONE_MONTH_BEFORE:
                return task.getDueDate().minusMonths(1);
            default:
                return task.getDueDate();
        }
    }

    // check if the reminder date user selected is reasonable -> not before the current date or past the due date 
    private void validateReminderDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid reminder date: Date must not be in the past.");
        }
        if (date.isAfter(task.getDueDate())) {
            throw new IllegalArgumentException("Invalid reminder date: Reminder date cannot be after the task's due date.");
        }
    }
    

    // getters
    public Task getTask() { return task; }
    public LocalDate getReminderDate() { return reminderDate; }
    public ReminderType getReminderType() { return reminderType; }
}

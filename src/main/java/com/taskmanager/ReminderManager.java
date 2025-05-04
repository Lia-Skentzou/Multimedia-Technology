package com.taskmanager;

import com.taskmanager.utils.JsonUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderManager {
    private List<Reminder> reminders;
    private static final String FILE_PATH = "medialab/reminders.json"; // reminders info will be saved to this json file: reminders.json

    // constructor
    public ReminderManager() {
        this.reminders = new ArrayList<>();
    }

    // load reminder information from json file
    public void loadFromJson() {
        this.reminders = new ArrayList<>(JsonUtil.readFromJsonFile(FILE_PATH, Reminder[].class));
        System.out.println("Loaded " + reminders.size() + " reminders from JSON.");
    }

    // save reminder information to json file
    public void saveToJson() {
        JsonUtil.writeToJsonFile(FILE_PATH, reminders);
        System.out.println("Saved reminders to JSON.");
    }

    // add a new reminder if it does not already exist
    public boolean addReminder(Reminder reminder) {
        if (reminder.getTask().getStatus() == TaskStatus.COMPLETED) {
            return false; 
        }
        if (reminderExists(reminder)) {
            return false;
        }
        reminders.add(reminder);
        return true;
    }
    
     // update an existing reminder with a new one.
     public boolean updateReminder(Reminder oldReminder, Reminder newReminder) {
        int index = reminders.indexOf(oldReminder);
        if (index != -1) {
            reminders.set(index, newReminder);
            return true;
        }
        return false;
    }

    // delete a reminder 
    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }
    
    // delete all reminders for a specific task 
    public void removeRemindersForTask(Task task) {
        reminders.removeIf(reminder -> reminder.getTask().getId().equals(task.getId()));
    }    

    // get all the reminders
    public List<Reminder> getAllReminders() {
        return reminders;
    }

    // check if a duplicate reminder exists.
    public boolean reminderExists(Reminder reminder) {
        for (Reminder r : reminders) {
            if (r.getTask().getId().equals(reminder.getTask().getId()) &&
                r.getReminderType() == reminder.getReminderType() &&
                r.getReminderDate().equals(reminder.getReminderDate())) {
                return true;
            }
        }
        return false;
    }

     // remove past reminders (reminders whose date is before today)
    public void removePastReminders() {
        LocalDate today = LocalDate.now();
        int beforeCount = reminders.size();
        reminders.removeIf(reminder -> reminder.getReminderDate().isBefore(today));
        int removed = beforeCount - reminders.size();
        if (removed > 0) {
            System.out.println("Removed " + removed + " past reminders.");
        }
    }
    
}

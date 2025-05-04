package com.taskmanager;

import com.taskmanager.utils.JsonUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * The {@code CategoryManager} class manages a collection of {@link Category} objects.
 * It provides methods for loading and saving categories to a JSON file, as well as adding,
 * removing, updating, and retrieving categories.
 */
public class CategoryManager {
    private List<Category> categories;
    private TaskManager taskManager;
    private static final String FILE_PATH = "medialab/categories.json"; // Categories are saved to this JSON file

    /**
     * Constructs a new {@code CategoryManager} with the specified {@link TaskManager}.
     *
     * @param taskManager the {@link TaskManager} associated with this manager
     */
    public CategoryManager(TaskManager taskManager) {
        this.categories = new ArrayList<>();
        this.taskManager = taskManager;
    }

    /**
     * Sets the {@link TaskManager} associated with this {@code CategoryManager}.
     *
     * @param taskManager the {@link TaskManager} to be set
     */
    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Loads category information from the JSON file specified by {@code FILE_PATH}.
     * If the file is empty or does not exist, the internal list of categories remains empty.
     */
    public void loadFromJson() {
        this.categories = new ArrayList<>(JsonUtil.readFromJsonFile(FILE_PATH, Category[].class));
        System.out.println("Loaded " + categories.size() + " categories from JSON.");
    }

    /**
     * Saves the current category information to the JSON file specified by {@code FILE_PATH}.
     */
    public void saveToJson() {
        JsonUtil.writeToJsonFile(FILE_PATH, categories);
        System.out.println("Saved categories to JSON.");
    }

    /**
     * Adds a new category if it does not already exist.
     *
     * @param category the {@link Category} to be added
     */
    public void addCategory(Category category) {
        if (!categoryExists(category.getName())) {
            categories.add(category);
        }
    }

    /**
     * Removes the category with the specified name and deletes all tasks that belong to that category.
     *
     * @param categoryName the name of the category to be removed
     */
    public void removeCategory(String categoryName) {
        taskManager.removeTasksByCategory(categoryName);
        categories.removeIf(category -> category.getName().equalsIgnoreCase(categoryName));
    }

    /**
     * Updates the name of an existing category.
     *
     * @param oldName the current name of the category
     * @param newName the new name to be assigned to the category
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    public boolean updateCategory(String oldName, String newName) {
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(oldName)) {
                category.setName(newName);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves all the categories managed by this {@code CategoryManager}.
     *
     * @return a {@code List} of {@link Category} objects
     */
    public List<Category> getAllCategories() {
        return categories;
    }

    /**
     * Retrieves a specific category by its name.
     *
     * @param name the name of the category to retrieve
     * @return an {@code Optional} containing the {@link Category} if found, or an empty {@code Optional} if not found
     */
    public Optional<Category> getCategoryByName(String name) {
        return categories.stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Checks whether a category with the specified name already exists.
     *
     * @param name the name of the category to check
     * @return {@code true} if a category with that name exists; {@code false} otherwise
     */
    public boolean categoryExists(String name) {
        return categories.stream().anyMatch(category -> category.getName().equalsIgnoreCase(name));
    }

    /**
     * Retrieves an existing category with the specified name, or creates and adds a new one if it does not exist.
     *
     * @param name the name of the category to retrieve or create
     * @return the existing or newly created {@link Category} object
     */
    public Category getOrCreateCategory(String name) {
        String normalized = name.trim();
        if (categoryExists(normalized)) {
            return getCategoryByName(normalized).get();
        } else {
            Category newCategory = new Category(normalized);
            addCategory(newCategory);
            return newCategory;
        }
    }

    
    
}

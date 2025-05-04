package com.taskmanager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> List<T> readFromJsonFile(String filePath, Class<T[]> clazz) {
        File file = new File(filePath);
        if (!file.exists()) {
            return List.of();
        }
        try {
            T[] array = objectMapper.readValue(file, clazz);
            return List.of(array);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static <T> void writeToJsonFile(String filePath, List<T> data) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            System.out.println("Data saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

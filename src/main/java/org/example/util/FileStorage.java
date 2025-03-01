package org.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class FileStorage<T> {
    private final Class<T> type;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileStorage(Class<T> type) {
        this.type = type;
    }

    public T load(String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists()) {
            return null;
        }

        return mapper.readValue(file, type);
    }

    public void save(String filename, T data) throws IOException {
        File file = new File(filename);
        mapper.writeValue(file, data);
    }
}

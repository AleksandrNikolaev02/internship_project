package org.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class FileStorage<T> {
    private final Class<T> type;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileStorage(Class<T> type) {
        this.type = type;
    }

    public T load(String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists()) {
            log.error("File with name '{}' not found!", filename);
            return null;
        }

        log.info("File uploaded successfully!");

        return mapper.readValue(file, type);
    }

    public void save(String filename, T data) throws IOException {
        File file = new File(filename);

        log.info("File saved successfully!");

        mapper.writeValue(file, data);
    }
}

package org.example.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.model.TopicList;
import org.example.repository.DataRepository;
import org.example.util.FileStorage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class ConsoleCommandListener implements Runnable {
    private final FileStorage<TopicList> topicsList = new FileStorage<>(TopicList.class);
    private final DataRepository dataRepository;

    public ConsoleCommandListener(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String command = reader.readLine();
                if (command.equalsIgnoreCase("exit")) {
                    log.info("Gracefully shutdown server...");
                    System.exit(0);
                    break;
                } else if (command.startsWith("save ")) {
                    String filename = command.substring(5);
                    topicsList.save(filename, new TopicList(dataRepository.getTopics()));
                } else if (command.startsWith("load ")) {
                    String filename = command.substring(5);
                    var topics = topicsList.load(filename);
                    dataRepository.getTopics().clear();
                    dataRepository.getTopics().addAll(topics.getTopicList());
                } else {
                    log.error("Unknown command!");
                }
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }

        }
    }
}

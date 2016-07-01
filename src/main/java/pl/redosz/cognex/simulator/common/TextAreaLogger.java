package pl.redosz.cognex.simulator.common;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextAreaLogger {

    private final TextArea logs;

    public TextAreaLogger(TextArea logs) {
        this.logs = logs;
    }

    public void log(String text) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();

        Platform.runLater(() -> logs.appendText(String.format("%s %s\n", now.format(formatter), text)));
    }
}

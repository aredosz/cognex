package pl.redosz.cognex.simulator.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pl.redosz.cognex.simulator.common.TextAreaLogger;
import pl.redosz.cognex.simulator.server.ServerTask;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TextField manualPort;
    @FXML
    private TextField manualPrefix;
    @FXML
    private TextField manualPostfix;
    @FXML
    private TextField manualValue;

    @FXML
    private TextField remotelyPort;
    @FXML
    private TextField remotelyPrefix;
    @FXML
    private TextField remotelyPostfix;
    @FXML
    private TextField remotelyTriggerPhrase;
    @FXML
    private TextField remoteValue;

    @FXML
    private TextArea logs;
    @FXML
    private Button startReadersBtn;

    private TextAreaLogger logger;
    private boolean isReadersActive;

    private ServerTask manualReaderTask;
    private ServerTask remoteReaderTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger = new TextAreaLogger(logs);
    }

    public void onManualTrigger(Event event) {
        manualReaderTask.trigger();
    }

    public void onStartReaders(Event event) {
        if (isReadersActive) {
            logger.log("stop readers");

            manualReaderTask.stopServer();

            isReadersActive = false;
            startReadersBtn.setText("START READERS");
        } else {
            logger.log("start readers");

            manualReaderTask = new ServerTask(manualPort, manualPrefix, manualPostfix, manualValue, remotelyTriggerPhrase, logger);
            Thread manualReaderThread = new Thread(manualReaderTask);
            manualReaderThread.setDaemon(true);
            manualReaderThread.start();

            remoteReaderTask = new ServerTask(remotelyPort, remotelyPrefix, remotelyPostfix, remoteValue, remotelyTriggerPhrase, logger);
            Thread remoteReaderThread = new Thread(remoteReaderTask);
            remoteReaderThread.setDaemon(true);
            remoteReaderThread.start();

            isReadersActive = true;
            startReadersBtn.setText("STOP READERS");
        }
    }

    public void onClearLogs(Event event) {
        logs.clear();
    }
}

package pl.ynleborg;

import gnu.io.*;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static gnu.io.SerialPort.*;

@Slf4j
public class ArduinoDataListener implements SerialPortEventListener {
    private static final int OPEN_PORT_TIME_OUT = 10000;
    private static final int SERIAL_PORT_DATA_RATE = 9600;
    private static String endpoint;
    private BufferedReader input;
    private RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            endpoint = args[0];
        }
        new ArduinoDataListener().initialize();
        log.info("Port listener started, sending data to {}", endpoint);
    }

    private void initialize() throws NoSuchPortException {
        try {
            SerialPort serialPort = openPort();
            serialPort.setSerialPortParams(SERIAL_PORT_DATA_RATE, DATABITS_8, STOPBITS_1, PARITY_NONE);
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    private SerialPort openPort() throws NoSuchPortException, PortInUseException {
        String comPort = System.getProperty("gnu.io.rxtx.SerialPorts");
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(comPort);
        if (portId == null) {
            log.error("Could not find COM port.");
            throw new NoSuchPortException();
        } else {
            log.info("Successfully connected to {}", comPort);
        }
        return (SerialPort) portId.open(this.getClass().getName(), OPEN_PORT_TIME_OUT);
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();
                log.debug("received from COM: [{}]", inputLine);
                if (endpoint != null) {
                    sentData(inputLine);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void sentData(String inputLine) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(inputLine, headers);
        String answer = restTemplate.postForObject(endpoint, entity, String.class);
        log.debug("response from WS: [{}]", answer);
    }
}

package org.bibliarij.tradeshiftassignment.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class ClientApplication {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private static final Logger log = LogManager.getLogger(ClientApplication.class);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {

        new Thread(() -> sendRequest("IntegerService", "add", 1, 2)).start();
        new Thread(() -> sendRequest("DoNothingService", "doNothing")).start();
        new Thread(() -> sendRequest("DoNothingServic", "doNothing")).start();
    }

    private static Object sendRequest(String serviceName, String methodName, Object... arguments) {
        try (Socket socket = new Socket(HOST, PORT)){
            log.info("Connected to {}:{}", HOST, PORT);
            try (OutputStream outputStream = socket.getOutputStream()) {
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){
                    objectOutputStream.writeObject(atomicInteger.getAndIncrement());
                    objectOutputStream.writeObject(serviceName);
                    objectOutputStream.writeObject(methodName);

                    for (Object argument : arguments) {
                        objectOutputStream.writeObject(argument);
                    }
                    objectOutputStream.writeObject(null);

                    objectOutputStream.flush();
                    String command = String.format("%s.%s(%s)", serviceName, methodName, Arrays.asList(arguments));
                    log.info("Command sent: {})", command);

                    try (InputStream inputStream = socket.getInputStream()){
                        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
                            Integer number = (Integer) objectInputStream.readObject();
                            Object result = objectInputStream.readObject();

                            if (result instanceof Exception){
                                String message = String.format("Exception for command %s: ", command);
                                log.error(message, (Exception)result);
                            } else {
                                log.info("Result {} for command {}", result, command);
                            }

                            return result;
                        } catch (ClassNotFoundException e) {
                            log.error("Exception: ", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Exception: ", e);
        }

        return null;
    }
}

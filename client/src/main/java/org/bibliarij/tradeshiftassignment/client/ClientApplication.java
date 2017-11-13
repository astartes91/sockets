package org.bibliarij.tradeshiftassignment.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class ClientApplication {

    private static final Logger log = LogManager.getLogger(ClientApplication.class);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {

        String serviceName = "IntegerService";
        String methodName = "add";

        sendRequest(serviceName, methodName, 1, 2);
    }

    private static void sendRequest(String serviceName, String methodName, Object... arguments) {
        int port = 8080;
        String host = "localhost";
        try (Socket socket = new Socket(host, port)){
            log.info("Connected to {}:{}", host, port);
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
                    log.info("Command sent: {}.{}({})", serviceName, methodName, arguments);

                    try (InputStream inputStream = socket.getInputStream()){
                        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
                            Object object = objectInputStream.readObject();
                            log.info("Result: {}", object);
                        } catch (ClassNotFoundException e) {
                            log.error(e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }
}

package org.bibliarij.tradeshiftassignment.server;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class ServerApplication {

    private static final Logger log = LogManager.getLogger(ServerApplication.class);
    private static Map<String, Object> services = new HashMap<>();

    public static void main(String[] args) throws IOException {

        int port = 8080;
        if(args.length != 0){
            port = Integer.valueOf(args[0]);
        }
        new ServerApplication().startServer(port);
    }

    private void startServer(int port) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("server.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        properties.forEach((o, o2) -> {
            try {
                services.put((String) o, Class.forName((String) o2).newInstance());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                log.error("Exception: ", e);
            }
        });

        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("Server started on port {}", port);

            while (true){
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> processRequest(clientSocket)).start();
            }
        }
    }

    private static void processRequest(Socket socket){

        log.info("Client {}:{} connected", socket.getInetAddress(), socket.getPort());
        Integer number = null;

        try (InputStream inputStream = socket.getInputStream()){
            try(ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                number = (Integer) objectInputStream.readObject();
                String serviceName = (String) objectInputStream.readObject();
                String methodName = (String) objectInputStream.readObject();

                List arguments = new ArrayList();
                Object object = null;
                while ((object = objectInputStream.readObject()) != null){
                    arguments.add(object);
                }

                String command = String.format("%s.%s(%s)", serviceName, methodName, arguments);
                log.info("Command received: {}", command);

                try (OutputStream outputStream = socket.getOutputStream()) {
                    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){

                        objectOutputStream.writeObject(number);
                        try {
                            Object result = MethodUtils.invokeExactMethod(
                                    services.get(serviceName), methodName, arguments.toArray()
                            );
                            log.info("Result {} for command {}", result, command);
                            objectOutputStream.writeObject(result);
                        } catch (Exception e){
                            String message = String.format("Exception for command %s: ", command);
                            log.error("Exception: ", e);
                            e.setStackTrace(new StackTraceElement[]{});
                            objectOutputStream.writeObject(e);
                        }

                        objectOutputStream.flush();
                    }
                } catch (IOException e) {
                    log.error("Exception: ", e);
                }
            } catch (ClassNotFoundException e) {
                log.error("Exception: ", e);
            }
        } catch (IOException e) {
            log.error("Exception: ", e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            log.error("Exception: ", e);
        }
    }
}

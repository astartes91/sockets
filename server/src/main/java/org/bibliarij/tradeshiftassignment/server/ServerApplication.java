package org.bibliarij.tradeshiftassignment.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class ServerApplication {

    private static final Logger log = LogManager.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("Server started on port {}", port);

            while (true){
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> processRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            log.error(e);
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
                log.info("Command received: {}.{}({})", serviceName, methodName, arguments);

                try (OutputStream outputStream = socket.getOutputStream()) {
                    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){
                        objectOutputStream.writeObject(number);
                        objectOutputStream.flush();
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            } catch (ClassNotFoundException e) {
                log.error(e);
            }
        } catch (IOException e) {
            log.error(e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            log.error(e);
        }
    }
}

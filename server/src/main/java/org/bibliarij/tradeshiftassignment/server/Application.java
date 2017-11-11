package org.bibliarij.tradeshiftassignment.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("Server started on port {}", port);

            while (true){
                Socket clientSocket = serverSocket.accept();
                log.info("Client connected");
            }
        } catch (IOException e) {
            log.error(e);
        }
    }
}

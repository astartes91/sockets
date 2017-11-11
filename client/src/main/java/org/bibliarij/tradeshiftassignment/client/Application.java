package org.bibliarij.tradeshiftassignment.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Vladimir Nizamutdinov (astartes91@gmail.com)
 */
public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        int port = 8080;
        String host = "localhost";
        try (Socket socket = new Socket(host, port)){
            log.info("Connected to {}:{}", host, port);
        } catch (IOException e) {
            log.error(e);
        }
    }
}

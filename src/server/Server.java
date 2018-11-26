package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.*;

public class Server {
    static LinkedList<Socket> list = new LinkedList<>();
    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new File(System.nanoTime() + ".log")));
        ServerSocket sock = new ServerSocket(1982);
        while(true) {
            Socket s = sock.accept();
            list.add(s);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Scanner scanner = new Scanner(s.getInputStream());
                        while(scanner.hasNextLine()) {
                            String string = scanner.nextLine().trim();
                            for(Socket so : list) {
                                PrintStream thing = new PrintStream(so.getOutputStream());
                                thing.println(string);
                            }
                            System.out.println(string);
                        }
                    } catch (IOException ex) {
                        
                    }
                    list.remove(s);
                }
            }).start();
        }
    }
}

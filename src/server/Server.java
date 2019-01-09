package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.*;

public class Server {
    
    public static double version = 1.1;
    
    static LinkedList<Socket> list = new LinkedList<>();
    
    public static void main(String[] args) throws IOException {
        int limit = 20;
        if(args.length == 1) {
            limit = Integer.parseInt(args[0]);
        }
        
        File f = new File(System.nanoTime() + ".log");
        System.setOut(new PrintStream(f));
        ServerSocket sock = new ServerSocket(1982);
        while(true) {
            Socket s = sock.accept();
            list.add(s);
            new PrintStream(s.getOutputStream()).println("Version: " + version + "\n");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(list.size() > limit) (new PrintStream(s.getOutputStream())).println("too many connections!");
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

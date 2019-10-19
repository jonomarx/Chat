package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    public static double version = 1.3;
    
    static LinkedList<Socket> list = new LinkedList<>();
    
    public static void main(String[] args) throws IOException {
        int limita = 20;
        if(args.length == 1) {
            limita = Integer.parseInt(args[0]);
        }
        final int limit = limita;
        File f = new File(System.nanoTime() + ".log");
        System.setOut(new PrintStream(f));
        ServerSocket sock = new ServerSocket(1982);
        
        Thread t = new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   DatagramSocket s = new DatagramSocket(1819);
                   s.setBroadcast(true);
                   
                   while(true) {
                       byte[] buffer = new byte[12000];
                       DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                       s.receive(packet);
                       
                       String message = new String(packet.getData()).trim();
                       byte[] sendData;
                       if(message.equals("DISCOVER_SERVER_REQUEST")) {
                           sendData = "DISCOVER_SERVER_RESPONSE".getBytes();
                       } else {
                           sendData = "MESSAGE_NOT_FOUND".getBytes();
                       }
                       DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                       s.send(sendPacket);
                       
                       System.out.println(packet.getAddress());
                   }
               } catch (SocketException ex) {
                   Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                   System.exit(1);
               } catch (IOException ex) {
                   Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
               }
           } 
        });
        t.start();
        
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

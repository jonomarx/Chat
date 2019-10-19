package client;

import java.awt.Desktop;
import java.awt.HeadlessException;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Client extends JFrame {
    
    public static double version = 1.3;
    
    ChatWindow chat = new ChatWindow();
    JoinPanel join = new JoinPanel();
    String name;
    String room;
    
    public static boolean isBrowsingSupported = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
    
    public void start() {
        setVisible(true);
        setContentPane(join);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        join.jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinkedList<String> list = new LinkedList<>();
                try {
                    DatagramSocket c = new DatagramSocket();
                    c.setBroadcast(true);
                    c.setSoTimeout(100);
                    
                    byte[] sendData = "DISCOVER_SERVER_REQUEST".getBytes();
                    
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 1819);
                        c.send(sendPacket);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while(interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = interfaces.nextElement();
                        if(networkInterface.isLoopback() || !networkInterface.isUp()) {
                            continue;
                        }
                        
                        for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if(broadcast == null) continue;
                            System.out.println(interfaceAddress.getAddress().getHostAddress());
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 1819);
                                c.send(sendPacket);
                            } catch(Exception ex) {ex.printStackTrace();}
                        }
                        byte[] recBuf = new byte[12000];
                        DatagramPacket recievePacket = new DatagramPacket(recBuf, recBuf.length);
                        try {c.receive(recievePacket);} catch(Exception ex) {}
                            
                        String message = new String(recievePacket.getData()).trim();
                        if(message.equals("DISCOVER_SERVER_RESPONSE")) {
                            list.add(recievePacket.getAddress().getHostAddress());
                        }
                    }
                    c.close();
                    String message = "Available Servers: \n";
                    for(String string : list) {
                        message += string + "\n";
                    }
                    JOptionPane.showMessageDialog(rootPane, message);
                } catch (SocketException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(rootPane, "Serach Failed");
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        join.jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(join.jTextField3.getText().length() > 16) {
                        JOptionPane.showMessageDialog(rootPane, "Chat Room exceeds max. limit of 16 characters");
                        return;
                    }
                    room = join.jTextField3.getText();
                    if(join.jTextField3.getText().equals("")) room = "Public";
                    Socket s = new Socket("".equals(join.jTextField2.getText().trim()) ? "localhost" : join.jTextField2.getText().trim(), 1982);
                    Scanner in = new Scanner(s.getInputStream());
                    PrintStream out = new PrintStream(s.getOutputStream());
                    name = join.jTextField1.getText();
                    out.println("<" + room + "> " + name + " entered the room");
                    setContentPane(chat);
                    Thread.sleep(100);
                    repaint();
                    pack();
                    
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LinkedList<String> list = new LinkedList<>();
                            boolean ok = false;
                            while(in.hasNextLine()) {
                                String string = in.nextLine().trim();
                                if(string.equals("too many connections!")) {
                                    JOptionPane.showMessageDialog(Client.this, "Sorry, there are too many connections!");
                                    ok = false;
                                } else ok = true;
                                if(!string.startsWith("Version: " + version + "") && string.startsWith("Version: ")) {
                                    //JOptionPane.showMessageDialog(rootPane, "please update your client version (or downgrade) to version " + string.substring(9, 12));
                                    //System.exit(0);
                                }
                                if(string.startsWith("Version:")) string = string.substring(12);
                                string = findHyperLinks(string);
                                list.add(string + "<br>");
                                String listString = arrayToList(list.toArray());
                                String stuff = listString;
                                if(stuff.startsWith("<br> <br>")) stuff.substring(4);
                                System.out.println(stuff);
                                chat.jEditorPane1.setText("<html><body>" + stuff.substring(5) + "</body></html>");
                                chat.jEditorPane1.setCaretPosition(chat.jEditorPane1.getDocument().getLength());
                                if(!ok) break;
                            }
                            System.exit(0);
                        }
                    }).start();
                        
                    chat.jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            try {
                                if(!Client.isBrowsingSupported) {
                                    JOptionPane.showMessageDialog(rootPane, "Browing isn't supported :( ");
                                    return;
                                }
                                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                    Desktop.getDesktop().browse(e.getURL().toURI());
                                }
                            } catch(HeadlessException | IOException | URISyntaxException ep) {
                                JOptionPane.showMessageDialog(rootPane, "Oops! The Url didn't work!");
                            }
                        }
                    });
                    chat.jButton1.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            out.println("<"+room+"> " + name + ": " + chat.jTextField1.getText() + "");
                            chat.jTextField1.setText("");
                        }
                    });
                    Client.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            out.println("<" + room + "> " + name + " left the room<br>");
                            System.exit(0);
                         }
                    });
                    Client.this.chat.jButton2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        out.println("<" + room + "> " + name + " left the room<br>");
                        System.exit(0);
                    }
                });
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Client.this, "invalid server. Please check the server name or connect to the internet");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                private String arrayToList(Object[] string) {
                            return Client.arrayToString(string);
                        }
                        private String findHyperLinks(String str) {
                            String stri;
                            String endString = "";
                            for(String i : (String[]) str.split(" ")) {
                                stri = i;
                                if(i.startsWith("https://")) {
                                    stri = "<a href=" + stri + ">" + stri + "</a>";
                                }
                                endString += stri + " ";
                            }
                            return endString;
                        }
        });
        pack();
    }
    public static String arrayToString(Object[] array) {
        String stri = "";
        for(Object str : array) {
            stri += (String) str;
        }
        return stri;
    }
}

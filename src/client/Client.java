package client;

import java.awt.Desktop;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Client extends JFrame {
    
    public static double version = 1.1;
    
    ChatWindow chat = new ChatWindow();
    JoinPanel join = new JoinPanel();
    String name;
    public static boolean isBrowsingSupported = Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
    
    public void start() {
        setVisible(true);
        setContentPane(join);
        join.jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Socket s = new Socket("".equals(join.jTextField2.getText().trim()) ? "localhost" : join.jTextField2.getText().trim(), 1982);
                    Scanner in = new Scanner(s.getInputStream());
                    PrintStream out = new PrintStream(s.getOutputStream());
                    name = join.jTextField1.getText();
                    out.println(name + " entered the room");
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
                                    JOptionPane.showMessageDialog(rootPane, "please update your client version (or downgrade) to version " + string.substring(9, 12));
                                    System.exit(0);
                                }
                                if(string.startsWith("Version:")) string = string.substring(12);
                                string = findHyperLinks(string);
                                list.add(string + "<br>");
                                String listString = arrayToList(list.toArray());
                                String stuff = listString;
                                if(stuff.startsWith("<br> <br>")) stuff.substring(4);
                                System.out.println(stuff);
                                chat.jEditorPane1.setText("<html><body>" + stuff.substring(5) + "</body></html>");
                                if(!ok) break;
                            }
                            System.exit(0);
                        }
                    }).start();
                        
                    chat.jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            try {
                                if(!Client.isBrowsingSupported) {
                                    JOptionPane.showMessageDialog(rootPane, "Browing isn't supported :( ");
                                    return;
                                }
                                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                    Desktop.getDesktop().browse(e.getURL().toURI());
                                }
                            } catch(Exception ep) {
                                JOptionPane.showMessageDialog(rootPane, "Oops! The Url didn't work!");
                            }
                        }
                    });
                    chat.jButton1.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            out.println(name + ": " + chat.jTextField1.getText() + "");
                            chat.jTextField1.setText("");
                        }
                    });
                    Client.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            out.println(name + " left the room<br>");
                            System.exit(0);
                         }
                    });
                    Client.this.chat.jButton2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        out.println(name + " left the room<br>");
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

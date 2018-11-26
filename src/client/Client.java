package client;

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

public class Client extends JFrame {
    
    public static double version = 1.1;
    
    ChatWindow chat = new ChatWindow();
    JoinPanel join = new JoinPanel();
    String name;
    
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
                            while(in.hasNextLine()) {
                                String string = in.nextLine().trim();
                                if(!string.startsWith("Version: " + version + "") && string.startsWith("Version: ")) {
                                    JOptionPane.showMessageDialog(rootPane, "please update your client version (or downgrade) to version " + string.substring(9, 12));
                                    System.exit(0);
                                }
                                if(string.startsWith("Version:")) string = string.substring(12);
                                
                                String[] strings = string.split(" ");
                                for(int i = 0; i < strings.length; i++) {
                                    String str = strings[i];
                                    if (str.startsWith("http://") || str.startsWith("https://")) strings[i] = "<a href='"+ str + "'>"+str+"</a>";
                                }
                                StringBuilder stri = new StringBuilder();
                                Iterator<String> it = list.descendingIterator();
                                while (it.hasNext()) {
                                    stri.append(it.next());
                                }
                                String listString = stri.toString();
                                list.add(string + "<br>");
                                String stuff = listString.substring(1, listString.length() - 2 < 1 ? 0 : listString.length() - 2);
                                System.out.println(stuff);
                                chat.jEditorPane1.setText("<html><body>" + stuff + "</body></html>");
                            }
                            System.exit(0);
                        }
                    }).start();
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
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Client.this, "invalid server. Please check the server name or connect to the internet");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        pack();
    }
}

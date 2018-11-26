package client;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends JFrame {
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
                            while(in.hasNextLine()) {
                                chat.jTextArea1.append(in.nextLine().trim() + "\n");
                            }
                            System.exit(0);
                        }
                    }).start();
                    chat.jButton1.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            out.println(name + ": " + chat.jTextField1.getText());
                            chat.jTextField1.setText("");
                        }
                    });
                    Client.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            out.println(name + " left the room");
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

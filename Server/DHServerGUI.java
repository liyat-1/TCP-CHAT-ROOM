import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class DHServerGUI extends JFrame {
    private JButton startServerButton;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintStream out;
    private Cipher cipher;
    private SecretKeySpec sharedSecretKey;
    public static int keyLength = 128;

    public DHServerGUI() {
        setTitle("DH Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
            setLayout(new BorderLayout());
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new FlowLayout());
            topPanel.setLayout(new FlowLayout());
            startServerButton = new JButton("Start Server");
            startServerButton.setForeground(Color.WHITE);
            startServerButton.setBackground(new Color(66, 139, 202)); // Set your desired color
            topPanel.add(startServerButton);
            add(topPanel, BorderLayout.NORTH);
            topPanel.add(startServerButton);
            add(topPanel, BorderLayout.NORTH);
            JPanel chatPanel = new JPanel();
            chatPanel.setLayout(new BorderLayout());
            Border chatBorder = BorderFactory.createLineBorder(Color.BLUE, 2); 
            chatArea = new JTextArea();
            chatArea.setEditable(false);
            chatArea.setBackground(new Color(70, 70, 70)); 
            chatArea.setForeground(Color.WHITE);
            chatArea.setBorder(chatBorder); 
            JScrollPane chatScrollPane = new JScrollPane(chatArea);
            chatPanel.add(chatScrollPane, BorderLayout.CENTER);
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new FlowLayout());
            messageField = new JTextField(30);
            sendButton = new JButton("Send");
            sendButton.setForeground(Color.WHITE);
            sendButton.setBackground(new Color(66, 139, 202)); // Set your desired color
            messagePanel.add(messageField);
            messagePanel.add(sendButton);
            chatPanel.add(messagePanel, BorderLayout.SOUTH);
            add(chatPanel, BorderLayout.CENTER);

                    startServerButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                serverSocket = new ServerSocket(11111);
                                chatArea.append("Server started. Waiting for client connection...\n");
                                clientSocket = serverSocket.accept();
                                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                out = new PrintStream(clientSocket.getOutputStream());
                                performKeyExchange();

                                chatArea.append("Connected to client: " + clientSocket.getInetAddress().toString() + "\n");

            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            byte[] receivedMessage = (byte[]) inputStream.readObject();

                            // Decryption
                            cipher.init(Cipher.DECRYPT_MODE, sharedSecretKey);
                            byte[] decryptedMessage = cipher.doFinal(receivedMessage);

                            // Display the decrypted version of the message
                            chatArea.append("Received encrypted message: " + new String(receivedMessage) + "\n");
                            chatArea.append("Decrypted message: " + new String(decryptedMessage) + "\n");
                        }
                    } catch (IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            receiveThread.start();
                            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String message = messageField.getText();
                        cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey);
                        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
                        out.println(encryptedMessage.length); 
                        out.write(encryptedMessage, 0, encryptedMessage.length);
                        out.flush(); 
                        chatArea.append("Message: " + message + "\n");
                        chatArea.append("Sent encrypted message: " + new String(encryptedMessage) + "\n");
                        messageField.setText("");
                    } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            }
                private void performKeyExchange() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
                    SecureRandom sr = new SecureRandom();
                    BigInteger q = new BigInteger(in.readLine());
                    BigInteger a = new BigInteger(in.readLine());
                    BigInteger ya = new BigInteger(in.readLine());
                    BigInteger xb = new BigInteger(q.bitLength() - 1, sr);
                    BigInteger yb = a.modPow(xb, q);
                    out.println(yb);
                    BigInteger key = ya.modPow(xb, q);
                    cipher = Cipher.getInstance("AES");
                    byte[] sharedKeyBytes = key.toByteArray();
                    sharedSecretKey = new SecretKeySpec(sharedKeyBytes, "AES");
                    chatArea.append("The Secret key is: " + key + "\n");
                }
                public static void main(String[] args) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new DHServerGUI().setVisible(true);
                        }
                    });
                }
            }
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
public class DHClientGUI extends JFrame {

    private JTextField serverIPField;
    private JButton connectButton;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Socket link;
    private BufferedReader in;
    private PrintStream out;
    private Cipher cipher;
    private SecretKeySpec sharedSecretKey;
    public static int keyLength = 128;
    public DHClientGUI() {
        setTitle("DH Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        JLabel serverIPLabel = new JLabel("Server IP: ");
        serverIPField = new JTextField(15);
        connectButton = new JButton("Connect");
        connectButton.setForeground(Color.WHITE);
        connectButton.setBackground(new Color(66, 139, 202)); // Set your desired color
        topPanel.add(serverIPLabel);
        topPanel.add(serverIPField);
        topPanel.add(connectButton);
        add(topPanel, BorderLayout.NORTH);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
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

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String host = serverIPField.getText();
                    link = new Socket(host, 11111);
                    in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                    out = new PrintStream(link.getOutputStream());
                    performKeyExchange();

                    chatArea.append("Connected to server\n");
                    Thread receiveThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    ObjectInputStream inputStream = new ObjectInputStream(link.getInputStream());
                                    byte[] receivedMessage = (byte[]) inputStream.readObject();

                                    // Decryption
                                    cipher.init(Cipher.DECRYPT_MODE, sharedSecretKey);
                                    byte[] decryptedMessage = cipher.doFinal(receivedMessage);
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
        }
        );

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = messageField.getText();
                    cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey);
                    byte[] encryptedMessage = cipher.doFinal(message.getBytes());

                    ObjectOutputStream outputStream = new ObjectOutputStream(link.getOutputStream());
                    outputStream.writeObject(encryptedMessage);
                    chatArea.append("message: " + new String(message) + "\n");
                    chatArea.append("Sent encrypted message: " + new String(encryptedMessage) + "\n");

                    messageField.setText("");
                } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
   
    private void performKeyExchange() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
        SecureRandom sr = new SecureRandom();
        BigInteger q = new BigInteger(keyLength, 10, sr);
        BigInteger a = new BigInteger(keyLength - 1, sr);
        BigInteger xa = new BigInteger(keyLength - 1, sr);
        BigInteger ya = a.modPow(xa, q);


        out.println(q);
        out.println(a);
        out.println(ya);

        BigInteger yb = new BigInteger(in.readLine());
        BigInteger key = yb.modPow(xa, q);
        cipher = Cipher.getInstance("AES");
        byte[] sharedKeyBytes = key.toByteArray();
        sharedSecretKey = new SecretKeySpec(sharedKeyBytes, "AES");
        chatArea.append("The Secret key is: " + key + "\n");
                    
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DHClientGUI().setVisible(true);
            }
        });
    }
}
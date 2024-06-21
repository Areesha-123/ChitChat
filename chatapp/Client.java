import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

 public class Client {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    public Client() {
        initializeGUI();
        initializeConnection();
        startMessageListener();
    }


    private void initializeGUI() {
        frame = new JFrame("Client Messenger");
        frame.setSize(400, 650); // Set the frame size to be rectangular

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JLabel heading = new JLabel("Client Area");

        textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        textField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.getContentPane().setBackground(Color.pink);
        textArea.setBackground(Color.white);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(heading, BorderLayout.NORTH);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        heading.setHorizontalAlignment(SwingConstants.CENTER);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.setVisible(true);
    }

    private void initializeConnection() {
        try {
            socket = new Socket("localhost", 7777);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMessageListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromServer;
                try {
                    while ((msgFromServer = bufferedReader.readLine()) != null) {
                        textArea.append("Server: " + msgFromServer + "\n");
                        if (msgFromServer.equalsIgnoreCase("BYE")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnections();
                }
            }
        }).start();
    }

    private void sendMessage() {
        String msgToSend = textField.getText();
        try {
            bufferedWriter.write(msgToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            textArea.append("Client: " + msgToSend + "\n");
            textField.setText("");
            if (msgToSend.equalsIgnoreCase("BYE")) {
                closeConnections();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnections() {
        try {
            if (bufferedWriter != null) bufferedWriter.close();
            if (bufferedReader != null) bufferedReader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}




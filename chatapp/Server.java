import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    private JFrame frame;
    private JTextArea textArea;
    private JButton startButton, stopButton;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public Server() {
        createGUI();
        getMessages();
    }

    private void createGUI() {
        frame = new JFrame("Server Messanger");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JLabel heading = new JLabel("Server Area");
        textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);  // Set background color for text area
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.darkGray);  // Set background color for button panel

        startButton = new JButton("Start");
        startButton.addActionListener(new StartButtonListener());
        buttonPanel.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(new StopButtonListener());
        stopButton.setEnabled(false);
        buttonPanel.add(stopButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.getContentPane().setBackground(Color.pink); // Set background color for frame
        frame.add(heading, BorderLayout.NORTH);
        heading.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        frame.pack();
        frame.setSize(350, 650);
        frame.setVisible(true);
    }

    static void insertMessges(String message, String time) {
        String password = "Areesh@123";

        String sql = "INSERT INTO message (messages, TIME) VALUES (?, ?)";

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/messages", "root", "Areesh@123");
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, message);
            preparedStatement.setString(2, time);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            isRunning = true;
            startServer();
        }
    }


    void getMessages(){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/messages", "root", "Areesh@123");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM message");

            int index=1;
            textArea.append("Previous Messgaes :-\n");
            while (resultSet.next()) {
                             textArea.append(  (  " Client: " + resultSet.getString("messages")+" ["+ resultSet.getString("TIME") + "]" + "\n")
                             );
                System.out.println("Message " + index++ +" : " + resultSet.getString("messages") + "Time:-" + resultSet.getString("TIME"));
            }
            textArea.append("Current Messgaes :-\n");
            // Close the result set, statement, and connection
            resultSet.close();
            statement.close();
            connection.close();

        }
        catch (Exception ex){
            System.out.println(ex.getStackTrace().toString());
        }

    }



    private class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            isRunning = false;
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(7777);
                    while (isRunning) {
                        Socket socket = serverSocket.accept();
                        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                        while (true) {
                            String msgFromClient = bufferedReader.readLine();
                            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

                            try {
                              insertMessges(msgFromClient,timeStamp);
                                System.out.println("DataSaved");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            textArea.append(  " Client: " + msgFromClient+"["+ timeStamp + "]" + "\n");
                            bufferedWriter.write("msg received");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            if (msgFromClient.equalsIgnoreCase("BYE")) {
                                break;
                            }
                        }
                        socket.close();
                        inputStreamReader.close();
                        outputStreamWriter.close();
                        bufferedWriter.close();
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Server();
            }
        });
    }
}

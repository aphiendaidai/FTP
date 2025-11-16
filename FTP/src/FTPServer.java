import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class FTPServer extends JFrame {
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private HashMap<String, String> users;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    private static final int PORT = 2121;
    private static final String FILES_DIR = "server_files/";
    
    public FTPServer() {
        users = new HashMap<>();
        loadUsers();
        threadPool = Executors.newCachedThreadPool();
        
        setTitle("FTP Server");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel("FTP SERVER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        JButton startBtn = new JButton("Start Server");
        JButton stopBtn = new JButton("Stop Server");
        stopBtn.setEnabled(false);
        
        startBtn.addActionListener(e -> {
            startServer();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        });
        
        stopBtn.addActionListener(e -> {
            stopServer();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
        
        bottomPanel.add(startBtn);
        bottomPanel.add(stopBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        new File(FILES_DIR).mkdirs();
    }
    
    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            log("Server started on port " + PORT);
            
            threadPool.execute(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log("Client connected: " + clientSocket.getInetAddress());
                        threadPool.execute(new ClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (isRunning) {
                            log("Error accepting client: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (IOException e) {
            log("Failed to start server: " + e.getMessage());
        }
    }
    
    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            log("Server stopped");
        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.util.Date() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void loadUsers() {
        try {
            File file = new File("server_users.dat");
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                users = (HashMap<String, String>) ois.readObject();
                ois.close();
            } else {
                users.put("admin", "admin123");
                saveUsers();
            }
        } catch (Exception e) {
            users = new HashMap<>();
            users.put("admin", "admin123");
        }
    }
    
    private void saveUsers() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("server_users.dat"));
            oos.writeObject(users);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String currentUser = null;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                out.println("220 FTP Server Ready");
                
                String line;
                while ((line = in.readLine()) != null) {
                    log("Command received: " + line);
                    handleCommand(line);
                }
            } catch (IOException e) {
                log("Client disconnected");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void handleCommand(String command) throws IOException {
            String[] parts = command.split(" ", 2);
            String cmd = parts[0].toUpperCase();
            String arg = parts.length > 1 ? parts[1] : "";
            
            switch (cmd) {
                case "USER":
                    currentUser = arg;
                    out.println("331 Password required for " + arg);
                    break;
                    
                case "PASS":
                    if (currentUser != null && users.containsKey(currentUser) 
                        && users.get(currentUser).equals(arg)) {
                        out.println("230 User logged in");
                        log("User " + currentUser + " logged in");
                    } else {
                        out.println("530 Login incorrect");
                        currentUser = null;
                    }
                    break;
                    
                case "REGISTER":
                    String[] creds = arg.split(" ", 2);
                    if (creds.length == 2) {
                        if (users.containsKey(creds[0])) {
                            out.println("550 User already exists");
                        } else {
                            users.put(creds[0], creds[1]);
                            saveUsers();
                            out.println("200 Registration successful");
                            log("New user registered: " + creds[0]);
                        }
                    } else {
                        out.println("500 Invalid registration format");
                    }
                    break;
                    
                case "LIST":
                    if (currentUser == null) {
                        out.println("530 Not logged in");
                        break;
                    }
                    File dir = new File(FILES_DIR);
                    File[] files = dir.listFiles();
                    StringBuilder fileList = new StringBuilder();
                    if (files != null) {
                        for (File f : files) {
                            fileList.append(f.getName()).append(":").append(f.length()).append(";");
                        }
                    }
                    out.println("150 " + fileList.toString());
                    break;
                    
                case "UPLOAD":
                    if (currentUser == null) {
                        out.println("530 Not logged in");
                        break;
                    }
                    handleUpload(arg);
                    break;
                    
                case "DOWNLOAD":
                    if (currentUser == null) {
                        out.println("530 Not logged in");
                        break;
                    }
                    handleDownload(arg);
                    break;
                    
                case "QUIT":
                    out.println("221 Goodbye");
                    socket.close();
                    break;
                    
                default:
                    out.println("500 Unknown command");
            }
        }
        
        private void handleUpload(String filename) throws IOException {
            out.println("150 Ready to receive");
            
            String sizeStr = in.readLine();
            long fileSize = Long.parseLong(sizeStr);
            
            FileOutputStream fos = new FileOutputStream(FILES_DIR + filename);
            InputStream is = socket.getInputStream();
            
            byte[] buffer = new byte[4096];
            long totalRead = 0;
            int bytesRead;
            
            while (totalRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            
            fos.close();
            out.println("226 Transfer complete");
            log("File uploaded: " + filename);
        }
        
        private void handleDownload(String filename) throws IOException {
            File file = new File(FILES_DIR + filename);
            if (!file.exists()) {
                out.println("550 File not found");
                return;
            }
            
            out.println("150 " + file.length());
            
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = socket.getOutputStream();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            
            fis.close();
            os.flush();
            out.println("226 Transfer complete");
            log("File downloaded: " + filename);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FTPServer server = new FTPServer();
            server.setVisible(true);
        });
    }
}
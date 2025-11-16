import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FTPClient extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JTextField serverHost;
    private JTextField serverPort;
    
    private JTextField regUsername;
    private JPasswordField regPassword;
    private JPasswordField regConfirmPassword;
    
    private JTextArea fileListArea;
    private JTextField uploadFilePath;
    private JTextField downloadFileName;
    private JTextArea fileContentArea;
    private JTextField openFileName;
    
    public FTPClient() {
        setTitle("FTP Client");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");
        mainPanel.add(createFTPPanel(), "ftp");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("FTP CLIENT LOGIN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Server Host:"), gbc);
        
        gbc.gridx = 1;
        serverHost = new JTextField("localhost", 20);
        panel.add(serverHost, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Server Port:"), gbc);
        
        gbc.gridx = 1;
        serverPort = new JTextField("2121", 20);
        panel.add(serverPort, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        loginUsername = new JTextField(20);
        panel.add(loginUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        loginPassword = new JPasswordField(20);
        panel.add(loginPassword, gbc);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(25, 118, 210));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> handleLogin());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);
        
        JButton registerBtn = new JButton("Create Account");
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        gbc.gridy = 6;
        panel.add(registerBtn, gbc);
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(67, 160, 71));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        regUsername = new JTextField(20);
        panel.add(regUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        regPassword = new JPasswordField(20);
        panel.add(regPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        regConfirmPassword = new JPasswordField(20);
        panel.add(regConfirmPassword, gbc);
        
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(67, 160, 71));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> handleRegister());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(registerBtn, gbc);
        
        JButton backBtn = new JButton("Back to Login");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        gbc.gridy = 5;
        panel.add(backBtn, gbc);
        
        return panel;
    }
    
    private JPanel createFTPPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Connected to FTP Server");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel);
        
        JButton logoutBtn = new JButton("Disconnect");
        logoutBtn.addActionListener(e -> handleLogout());
        topPanel.add(logoutBtn);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Split pane for file list and content viewer
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left panel - File List
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Server Files"));
        
        fileListArea = new JTextArea(15, 30);
        fileListArea.setEditable(false);
        fileListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane fileListScroll = new JScrollPane(fileListArea);
        leftPanel.add(fileListScroll, BorderLayout.CENTER);
        
        JButton listBtn = new JButton("Refresh File List");
        listBtn.addActionListener(e -> listFiles());
        leftPanel.add(listBtn, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);
        
        // Right panel - File Content Viewer
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("File Viewer"));
        
        fileContentArea = new JTextArea();
        fileContentArea.setEditable(false);
        fileContentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        fileContentArea.setLineWrap(true);
        fileContentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(fileContentArea);
        rightPanel.add(contentScroll, BorderLayout.CENTER);
        
        JPanel openPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        openPanel.add(new JLabel("Open File:"));
        openFileName = new JTextField(15);
        openPanel.add(openFileName);
        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(e -> openFile());
        openPanel.add(openBtn);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> fileContentArea.setText(""));
        openPanel.add(clearBtn);
        rightPanel.add(openPanel, BorderLayout.NORTH);
        
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(300);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        uploadPanel.add(new JLabel("Upload File Path:"));
        uploadFilePath = new JTextField(25);
        uploadPanel.add(uploadFilePath);
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.addActionListener(e -> uploadFile());
        uploadPanel.add(uploadBtn);
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> browseFile());
        uploadPanel.add(browseBtn);
        bottomPanel.add(uploadPanel);
        
        JPanel downloadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        downloadPanel.add(new JLabel("Download File Name:"));
        downloadFileName = new JTextField(25);
        downloadPanel.add(downloadFileName);
        JButton downloadBtn = new JButton("Download");
        downloadBtn.addActionListener(e -> downloadFile());
        downloadPanel.add(downloadBtn);
        bottomPanel.add(downloadPanel);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void connectToServer() {
        try {
            String host = serverHost.getText().trim();
            int port = Integer.parseInt(serverPort.getText().trim());
            
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            String response = in.readLine();
            System.out.println(response);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            connectToServer();
            
            out.println("USER " + username);
            String response = in.readLine();
            System.out.println(response);
            
            out.println("PASS " + password);
            response = in.readLine();
            System.out.println(response);
            
            if (response.startsWith("230")) {
                cardLayout.show(mainPanel, "ftp");
                listFiles();
                JOptionPane.showMessageDialog(this, "Login successful!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Login failed!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                socket.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleRegister() {
        String username = regUsername.getText().trim();
        String password = new String(regPassword.getPassword());
        String confirmPassword = new String(regConfirmPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            connectToServer();
            
            out.println("REGISTER " + username + " " + password);
            String response = in.readLine();
            System.out.println(response);
            
            if (response.startsWith("200")) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                regUsername.setText("");
                regPassword.setText("");
                regConfirmPassword.setText("");
                cardLayout.show(mainPanel, "login");
            } else {
                JOptionPane.showMessageDialog(this, response.substring(4), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            socket.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void listFiles() {
        try {
            out.println("LIST");
            String response = in.readLine();
            
            fileListArea.setText("");
            fileListArea.append("Files on server:\n");
            fileListArea.append("=================\n\n");
            
            if (response.startsWith("150")) {
                String fileList = response.substring(4);
                if (!fileList.isEmpty()) {
                    String[] files = fileList.split(";");
                    for (String file : files) {
                        if (!file.isEmpty()) {
                            String[] parts = file.split(":");
                            if (parts.length == 2) {
                                fileListArea.append(parts[0] + " (" + parts[1] + " bytes)\n");
                            }
                        }
                    }
                } else {
                    fileListArea.append("No files on server\n");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error listing files: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            uploadFilePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void uploadFile() {
        String filePath = uploadFilePath.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a file!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "File not found!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            out.println("UPLOAD " + file.getName());
            String response = in.readLine();
            
            if (response.startsWith("150")) {
                out.println(file.length());
                
                FileInputStream fis = new FileInputStream(file);
                OutputStream os = socket.getOutputStream();
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                
                fis.close();
                os.flush();
                
                response = in.readLine();
                if (response.startsWith("226")) {
                    JOptionPane.showMessageDialog(this, "File uploaded successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    uploadFilePath.setText("");
                    listFiles();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Upload error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void downloadFile() {
        String fileName = downloadFileName.getText().trim();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter filename!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            out.println("DOWNLOAD " + fileName);
            String response = in.readLine();
            
            if (response.startsWith("150")) {
                long fileSize = Long.parseLong(response.substring(4));
                
                File downloadDir = new File("client_downloads");
                downloadDir.mkdirs();
                File file = new File(downloadDir, fileName);
                
                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = socket.getInputStream();
                
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                int bytesRead;
                
                while (totalRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                
                fos.close();
                
                response = in.readLine();
                if (response.startsWith("226")) {
                    JOptionPane.showMessageDialog(this, 
                        "File downloaded to: " + file.getAbsolutePath(), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    downloadFileName.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(this, "File not found on server!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Download error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openFile() {
        String fileName = openFileName.getText().trim();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter filename!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            out.println("DOWNLOAD " + fileName);
            String response = in.readLine();
            
            if (response.startsWith("150")) {
                long fileSize = Long.parseLong(response.substring(4));
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = socket.getInputStream();
                
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                int bytesRead;
                
                while (totalRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                
                response = in.readLine();
                if (response.startsWith("226")) {
                    String content = baos.toString("UTF-8");
                    fileContentArea.setText(content);
                    fileContentArea.setCaretPosition(0);
                    openFileName.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(this, "File not found on server!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleLogout() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.println("QUIT");
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        loginUsername.setText("");
        loginPassword.setText("");
        fileListArea.setText("");
        fileContentArea.setText("");
        cardLayout.show(mainPanel, "login");
        
        JOptionPane.showMessageDialog(this, "Disconnected from server", 
            "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FTPClient client = new FTPClient();
            client.setVisible(true);
        });
    }
}
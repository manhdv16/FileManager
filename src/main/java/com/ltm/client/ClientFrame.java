package com.ltm.client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.ltm.data.DataFile;
import com.ltm.encrypt.ZipUtility;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JPasswordField;
import org.apache.commons.io.FileUtils;

public class ClientFrame extends JFrame implements ActionListener, ISocketListener {

    JTextField ipInput, portInput, searchInput;
    JLabel ipLabel, portLabel;
    JButton connectButton, disconnectButton, searchButton, downLoadFile, uploadFileButton, ModeSelfbuild, ModeFTP;
    JList<String> list;
    JProgressBar jb;
    ClientSocketThread clientSocketThread = null;

    // Thêm các trường FTP
    JTextField ftpServerInput, ftpUsernameInput, ftpPasswordInput, ftpPortInput;
    JLabel modeLabel, ftpServerLabel, ftpUsernameLabel, ftpPasswordLabel, ftpPortLabel;
    ftpClient c = null;
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientFrame frame = new ClientFrame();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ClientFrame() {

        ModeSelfbuild = new JButton("Mode Self-build");
        ModeFTP = new JButton("Mode FTP");
        ModeSelfbuild.setBounds(125, 50, 125, 25);
        ModeFTP.setBounds(275, 50, 125, 25);
        this.add(ModeSelfbuild);
        this.add(ModeFTP);

        // Connect Sever Socket
        ipLabel = new JLabel("IP: ");
        ipInput = new JTextField("127.0.0.1");
        ipLabel.setBounds(100, 100, 150, 25);
        ipInput.setBounds(200, 100, 200, 25);
        this.add(ipLabel);
        this.add(ipInput);
        portLabel = new JLabel("PORT: ");
        portInput = new JTextField("8080");
        portLabel.setBounds(100, 150, 150, 25);
        portInput.setBounds(200, 150, 200, 25);
        this.add(portLabel);
        this.add(portInput);

        //FTP connect
        ftpServerLabel = new JLabel("FTP Server: ");
        ftpServerInput = new JTextField("127.0.0.1");
        ftpPortLabel = new JLabel("PORT: ");
        ftpPortInput = new JTextField("21");
        ftpUsernameLabel = new JLabel("Username: ");
        ftpUsernameInput = new JTextField();
        ftpPasswordLabel = new JLabel("Password: ");
        ftpPasswordInput = new JPasswordField();

        ftpServerLabel.setBounds(100, 100, 150, 25);
        ftpServerInput.setBounds(200, 100, 200, 25);
        ftpPortLabel.setBounds(100, 130, 150, 25);
        ftpPortInput.setBounds(200, 130, 200, 25);
        ftpUsernameLabel.setBounds(100, 165, 150, 25);
        ftpUsernameInput.setBounds(200, 165, 200, 25);
        ftpPasswordLabel.setBounds(100, 200, 150, 25);
        ftpPasswordInput.setBounds(200, 200, 200, 25);
        this.add(ftpServerLabel);
        this.add(ftpServerInput);
        this.add(ftpPortLabel);
        this.add(ftpPortInput);
        this.add(ftpUsernameLabel);
        this.add(ftpUsernameInput);
        this.add(ftpPasswordLabel);
        this.add(ftpPasswordInput);

        hiddenAllForm();

        // Button Con | Dis
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        connectButton.setBounds(125, 235, 100, 25);
        disconnectButton.setBounds(275, 235, 100, 25);
        this.add(disconnectButton);
        this.add(connectButton);

        // Search Form
        JLabel searchLabel = new JLabel("Search: ");
        searchInput = new JTextField();
        searchButton = new JButton("Search");
        searchLabel.setBounds(100, 270, 75, 25);
        searchInput.setBounds(200, 270, 200, 25);
        searchButton.setBounds(400, 270, 75, 25);
        this.add(searchButton);
        this.add(searchInput);
        this.add(searchLabel);

        // Result List
        list = new JList<>();
        String[] dataa = {"File sẽ hiển thị khi connect với server"};
        list.setListData(dataa);
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setBounds(500, 100, 650, 400);
        this.add(listScrollPane);

        // JB
        downLoadFile = new JButton("Download File");
        downLoadFile.setBounds(125, 310, 125, 25);
        this.add(downLoadFile);

        uploadFileButton = new JButton("Upload File");
        uploadFileButton.setBounds(275, 310, 125, 25);
        this.add(uploadFileButton);
        jb = new JProgressBar(0, 100);
        jb.setBounds(125, 350, 275, 25);
        jb.setValue(0);
        jb.setStringPainted(true);
        this.add(jb);

        // Add event
        connectButton.addActionListener(this);
        disconnectButton.addActionListener(this);
        searchButton.addActionListener(this);
        downLoadFile.addActionListener(this);
        uploadFileButton.addActionListener(this);

        ModeSelfbuild.addActionListener(this);
        ModeFTP.addActionListener(this);

        // setting Frame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Client Frame");
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setBounds(0, 0, 1200, 800);
        this.setVisible(true);
    }

    public void hiddenAllForm() {
        ipLabel.setVisible(false);
        ipInput.setVisible(false);
        portLabel.setVisible(false);
        portInput.setVisible(false);

        ftpServerLabel.setVisible(false);
        ftpServerInput.setVisible(false);
        ftpUsernameLabel.setVisible(false);
        ftpUsernameInput.setVisible(false);
        ftpPasswordLabel.setVisible(false);
        ftpPasswordInput.setVisible(false);
        ftpPortLabel.setVisible(false);
        ftpPortInput.setVisible(false);
    }

    public void displayFormFTP() {
        ftpServerLabel.setVisible(true);
        ftpServerInput.setVisible(true);
        ftpUsernameLabel.setVisible(true);
        ftpUsernameInput.setVisible(true);
        ftpPasswordLabel.setVisible(true);
        ftpPasswordInput.setVisible(true);
        ftpPortLabel.setVisible(true);
        ftpPortInput.setVisible(true);

        ipLabel.setVisible(false);
        ipInput.setVisible(false);
        portLabel.setVisible(false);
        portInput.setVisible(false);
    }

    public void displayFormSelfbuild() {
        ipLabel.setVisible(true);
        ipInput.setVisible(true);
        portLabel.setVisible(true);
        portInput.setVisible(true);

        ftpServerLabel.setVisible(false);
        ftpServerInput.setVisible(false);
        ftpUsernameLabel.setVisible(false);
        ftpUsernameInput.setVisible(false);
        ftpPasswordLabel.setVisible(false);
        ftpPasswordInput.setVisible(false);
        ftpPortLabel.setVisible(false);
        ftpPortInput.setVisible(false);
    }
    private boolean isFTPMode = true;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == ModeFTP) {
            isFTPMode = true;
            displayFormFTP();
        } else if (e.getSource() == ModeSelfbuild) {
            isFTPMode = false;
            displayFormSelfbuild();
        }

        if (e.getSource() == connectButton) {
            if (isFTPMode) {
                String server = ftpServerInput.getText();
                String port = ftpPortInput.getText();
                String user = ftpUsernameInput.getText();
                String pass = ftpPasswordInput.getText();
                c = new ftpClient(server, port, user, pass);
                c.connectToServer();
                JOptionPane.showMessageDialog( null, "Connected to server");
//                
            } else {
                String ip = ipInput.getText();
                String port = portInput.getText();
                System.out.println(ip + " : " + port);
                try {
                    clientSocketThread = new ClientSocketThread(this);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                clientSocketThread.setSocket(ip, Integer.parseInt(port));
                clientSocketThread.start();
                clientSocketThread.sendString("VIEW_ALL_FILE");
            }

        } else if (e.getSource() == disconnectButton) {
            if (isFTPMode) {
                if(c!= null) c.disconnect();
            } else {
                String[] data = {"Chưa kết nối với server"};
                list.setListData(data);
                clientSocketThread.closeSocket();
            }
        } else if (e.getSource() == searchButton) {
            String search = searchInput.getText();

            if (clientSocketThread != null) {
                if (!search.isEmpty()) {
                    clientSocketThread.sendString("SEARCH_FILE" + "--" + search);
                } else {
                    clientSocketThread.sendString("VIEW_ALL_FILE");
                }
            }
        } else if (e.getSource() == downLoadFile) {
            if (list.getSelectedIndex() != -1) {
                String str = list.getSelectedValue();
//                List<String> lists = list.getSelectedValuesList();
                clientSocketThread.sendString("DOWNLOAD_FILE" + "--" + str);
            }
        } else if (e.getSource() == uploadFileButton) {
            if (isFTPMode) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                int returnVal = fileChooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String pathFile = selectedFile.getPath();
                    if(c!= null)   c.uploadFile(pathFile);
                    else    JOptionPane.showMessageDialog( null, "Not connected to the server");
                }
            } else {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setMultiSelectionEnabled(true);
                int returnVal = fileChooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    List<String> listFilePaths = new ArrayList<>();
                    for (File file : selectedFiles) {
                        if (file.isDirectory()) {
                            try {
                                long folderSize = FileUtils.sizeOfDirectory(file);
                                String zipFilePath = file.getPath() + ".zip";
                                ZipUtility.zipFolder(file.getPath(), zipFilePath);
                                System.out.println("zipfilepath: " + zipFilePath);
                                listFilePaths.add(zipFilePath + "--" + folderSize);
                            } catch (IOException er) {
                                er.printStackTrace();
                            }
                        } else {
                            listFilePaths.add(file.getPath());
                        }
                    }
                    clientSocketThread.sendFile(listFilePaths);
                }

            }
        }
    }

    @Override
    public void updateListFile(String[] listFile) {
        list.setListData(listFile);
    }

    @Override
    public void setProgress(int n) {
        // TODO Auto-generated method stub
        jb.setValue(n);
    }

    @Override
    public void showDialog(String str, String type) {
        if (type.equals("ERROR")) {
            JOptionPane.showMessageDialog(this, str, type, JOptionPane.ERROR_MESSAGE);
        } else if (type.equals("INFOR")) {
            JOptionPane.showMessageDialog(this, str, type, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public String chooserFileToSave(DataFile dataFile, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));

        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getPath();
            try {
                dataFile.saveFile(filePath);
                JOptionPane.showMessageDialog(null, "File Saved");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
        return null;
    }

}

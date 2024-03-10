package com.ltm.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JOptionPane;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class ftpClient {

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    private String server;
    private int port;
    private String user;
    private String pass;
    private FTPClient ftp;
    public ftpClient(String server, String port, String user, String pass) {
        this.server = server;
        this.port = Integer.parseInt(port);
        this.user = user;
        this.pass = pass;
    }

    public ftpClient() {
    }
    
    public void connectToServer() {
        ftp = new FTPClient();
        try {
            ftp.connect(server, port);
            showServerReply(ftp);
            int replyCode = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Server failed : " + replyCode);
                return;
            }

            boolean success = ftp.login(user, pass);
            showServerReply(ftp);

            if (!success) {
                System.out.println("Could not login to the server");
                return;
            } else {
                System.out.println("LOGGED IN SERVER");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void uploadFile(String pathFile){
        System.out.println("pathFile: "+ pathFile);
        String[] listPathFile = pathFile.split("\\\\");
        String fileNameToSave = listPathFile[listPathFile.length-1];
        System.out.println("fileNameToSave: "+ fileNameToSave);
        try{
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            
            // File cần gửi lên máy chủ FTP
            File localFile = new File(pathFile);
            String remoteFile = "/"+fileNameToSave;

            // Mở InputStream để đọc dữ liệu từ file local
            InputStream inputStream = new FileInputStream(localFile);
            System.out.println("Start uploading file");

            // Thực hiện lệnh storeFile để gửi file lên máy chủ
            boolean uploadStatus = ftp.storeFile(remoteFile, inputStream);

            if (uploadStatus) {
                System.out.println("File has been uploaded successfully.");
                JOptionPane.showMessageDialog( null, "File uploaded successfully");
            } else {
                System.out.println("Failed to upload the file.");
                JOptionPane.showMessageDialog(null, "Failed to upload the file");
            }
        }catch(IOException e){
            e.printStackTrace();  
        }
    }
    public void disconnect(){
        try {
            ftp.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

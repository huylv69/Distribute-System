/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myftpstorage;

import com.healthmarketscience.rmiio.SerializableInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import com.healthmarketscience.rmiio.SerializableOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;

/**
 *
 * @author HuyLV
 */
public class ServerImplement extends UnicastRemoteObject implements ServerInterface {

    private final String serverDir;
    private File serverFile;
    private final FileSystemView fileSystemView;
    private final int FILE_SPLIT_SIZE = 1024 * 1024 * 2; // 1MB

    ServerImplement(File defaultFile) throws RemoteException {
        super();
        this.serverFile = defaultFile;
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.serverDir = defaultFile.getAbsolutePath();
    }

    @Override
    public File getServerFile() throws RemoteException {
        return serverFile;
    }

    @Override
    public OutputStream getOutputStreamFile(File file) throws Exception {
        return new SerializableOutputStream(
                new FileOutputStream(file));
    }

    @Override
    public InputStream getInputStreamFile(File file) throws Exception {
        return new SerializableInputStream(
                new FileInputStream(file));
    }

    @Override
    public ArrayList<String> getServerFileList(String directory) throws IOException {
        File listF = new File(serverDir + "/" + directory);
        ArrayList<String> listFile = new ArrayList<>(Arrays.asList(listF.list()));
        return listFile;
    }

    @Override
    public String getFileType(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return fileSystemView.getSystemTypeDescription(file);
    }

    @Override
    public long getFileLastModified(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return file.lastModified();
    }

    @Override
    public String getFileName(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return file.getName();
    }

    @Override
    public Icon getFileIcon(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return fileSystemView.getSystemIcon(file);
    }

    @Override
    public long getFileSize(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return file.length();
    }

    @Override
    public File getFile(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        return file;
    }

    @Override
    public boolean isFile(String directory) throws IOException {
        File file = new File(serverDir + "/" + directory);
        if (file.exists()) {
            return file.isFile();
        } else {
            return false;
        }
    }

    @Override
    public void deleteFile(String directory) throws IOException {
        new File(serverDir + "/" + directory).delete();
    }

    @Override
    public void delList(List<File> delList) throws IOException {
        for (int i = 0; i < delList.size(); i++) {
            delList.get(i).delete();
        }
    }

    @Override
    public void merge(List<File> files, File dst) throws Exception {
        FileOutputStream os = new FileOutputStream(dst);
        byte[] buffer = new byte[FILE_SPLIT_SIZE];
        for (int i = 0; i < files.size(); i++) {
            File file = new File(files.get(i).getPath());
            FileInputStream is = new FileInputStream(file);
            int c = is.read(buffer);
            if (c >= 0) {
                os.write(buffer, 0, c);
            }
            is.close();
        }
        os.close();
    }

    @Override
    public File createFilePart(String nameFile) throws IOException {
        File file = new File("server_tmp", nameFile);
        System.out.println("ok");
        return file;
    }

    @Override
    public List<File> split(File f) throws Exception {
        ArrayList<File> output = new ArrayList<>();
        int partCounter = 0;

        byte[] buffer = new byte[FILE_SPLIT_SIZE];
        String fileName = f.getName();
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int bytesAmount = 0;

        while ((bytesAmount = bis.read(buffer)) > 0) {
            String filePartName = String.format("%s.%s", fileName, partCounter);
            File newFile = new File("server_tmp", filePartName);
            FileOutputStream os = new FileOutputStream(newFile);
            os.write(buffer, 0, bytesAmount);
            output.add(newFile);
            os.close();
            partCounter++;
        }
        fis.close();
        bis.close();
        return output;
    }

}

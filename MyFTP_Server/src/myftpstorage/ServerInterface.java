/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myftpstorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

/**
 *
 * @author HuyLV
 */
public interface ServerInterface extends Remote {

    public File getServerFile() throws RemoteException;

    public void merge(List<File> files, File dst) throws Exception;

    public List<File> split(File file) throws Exception;

    public ArrayList<String> getServerFileList(String directory) throws IOException;

    public OutputStream getOutputStreamFile(File file) throws Exception;

    public InputStream getInputStreamFile(File file) throws Exception;

    public void deleteFile(String directory) throws IOException;

    public File getFile(String directory) throws IOException;

    public File createFilePart(String nameFile) throws IOException;

    public boolean isFile(String directory) throws IOException;

    public String getFileType(String directory) throws IOException;

    public long getFileLastModified(String directory) throws IOException;

    public String getFileName(String directory) throws IOException;

    public Icon getFileIcon(String directory) throws IOException;

    public long getFileSize(String directory) throws IOException;
}

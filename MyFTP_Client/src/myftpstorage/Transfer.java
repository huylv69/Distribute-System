/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myftpstorage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Math.log;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HuyLV
 */
public class Transfer {

    private final int FILE_SPLIT_SIZE = 1024 * 1024 * 10;//10MB
    private ServerInterface server;
    private ScreenClient screenClient;
    private Transmission currentTrans;

    Transfer(ServerInterface server, ScreenClient client) {
        this.screenClient = client;
        this.server = server;
    }

    public Transmission getTransmission() {
        return this.currentTrans;
    }

    void upload(File src, File dst) throws IOException {
        // Split to upload 
        List<File> clientFileParts = this.splitFile("client_tmp", src);
        //Send list File split
        Transmission transmission = new Transmission(Transmission.UPLOAD, clientFileParts, dst, src);
        this.currentTrans = transmission;
        this.processThread(transmission);
    }

    void download(File src, File dst) throws Exception {
        System.out.println("Download " + src + " to " + dst);
        List<File> serverFileParts = this.server.split(src);
        Transmission transmission = new Transmission(
                Transmission.DOWNLOAD, serverFileParts, dst, src);
        this.currentTrans = transmission;
        this.processThread(transmission);
    }

// Using thread to process transmission
    public void processThread(Transmission transmission) {
        Thread thread = new Thread(() -> {
            try {
                this.process(transmission);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        thread.start();
    }

    private void process(Transmission transmission) throws Exception {
        File clientFilePart = null;
        File serverFilePart = null;
        if (transmission.getType() == Transmission.UPLOAD) {
            do {
                if (transmission.getStatus() == transmission.PAUSE) {
                    return; // PAUSE
                }
                clientFilePart = transmission.getNextFilePart();
                if (clientFilePart != null) {
                    System.out.println(clientFilePart.getName());
                    serverFilePart = this.server.createFilePart(clientFilePart.getName());
                    InputStream is = new FileInputStream(clientFilePart);
                    OutputStream os = this.server.getOutputStreamFile(serverFilePart);
                    transfer(is, os);
                    transmission.addTransferredFilePart(serverFilePart);
                } else {
                    // End merge file
                    this.server.merge(transmission.getTransferredFileParts(),
                            transmission.getDestination());
                }
                screenClient.updateProgress("Uploading file " + transmission.getDestination().getCanonicalPath() + " : part"
                        + transmission.getTransferredCount()
                        + "/" + transmission.getTotalPartCount());
                System.out.println("Uploading file " + transmission.getDestination().getCanonicalPath() + " : part"
                        + transmission.getTransferredCount()
                        + "/" + transmission.getTotalPartCount());
                Thread.sleep(1000);
            } while (clientFilePart != null);
            List<File> delList = transmission.getTransferredFileParts();
            this.server.delList(delList);
            screenClient.resetGUI();
            screenClient.updateProgress("Done!");
        } else { // DOWNLOAD
            do {
                if (transmission.getStatus() == transmission.PAUSE) {
                    return;
                }
                serverFilePart = transmission.getNextFilePart();
                if (serverFilePart != null) {
                    clientFilePart = new File("client_tmp", serverFilePart.getName());
                    InputStream is = this.server.getInputStreamFile(serverFilePart);
                    OutputStream os = new FileOutputStream(clientFilePart);
                    transfer(is, os);
                    transmission.addTransferredFilePart(clientFilePart);
                } else {
                    this.mergeFiles(transmission.getTransferredFileParts(),
                            transmission.getDestination());
                }
                System.out.println("Download to " + transmission.getDestination().getPath() + ": "
                        + transmission.getTransferredCount()
                        + "/" + transmission.getTotalPartCount());
                Thread.sleep(1000);
            } while (serverFilePart != null);
            List<File> delList = transmission.getTransferredFileParts();
            delList(delList);
            screenClient.resetGUI();
            screenClient.updateProgress("Done!");
        }
    }

    private void transfer(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024 * 1024];
        int c = 0;
        while ((c = is.read(buffer)) >= 0) {
            os.write(buffer, 0, c);
        }
        is.close();
        os.close();
    }

    private List<File> splitFile(String dir, File f) throws IOException {
        ArrayList<File> output = new ArrayList<>();
        int partCounter = 0;
        byte[] buffer = new byte[FILE_SPLIT_SIZE];
        String fileName = f.getName();
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int bytesAmount = 0;
        while ((bytesAmount = bis.read(buffer)) > 0) {
            String filePartName = String.format("%s.%s", fileName, partCounter);
            File newFile = new File(dir, filePartName);
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

    private void mergeFiles(List<File> files, File dst) throws IOException {
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

    private void delList(List<File> delList) {
        for (int i = 0; i < delList.size(); i++) {
            delList.get(i).delete();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myftpstorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HuyLV
 */
public class Transmission {

    public static final int TRANSFERRING = 1;
    public static final int PAUSE = 2;
    public static final int DONE = 3;

    public static final int DOWNLOAD = 1;
    public static final int UPLOAD = 2;

    private int type;
    private int status;
    private int pending;
    List<File> fileParts;
    List<File> transferredFileParts;
    File dst;
    File src;

    public Transmission(int type, List<File> fileParts, File dst, File src) {
        this.fileParts = fileParts;
        this.pending = 0;
        this.status = TRANSFERRING;
        this.type = type;
        this.dst = dst;
        this.src = src;
        this.transferredFileParts = new ArrayList<>();
    }

    public void pauseTransfer() {
        this.status = PAUSE;
    }

    public void continueTransfer() {
        this.status = TRANSFERRING;
    }

    public File getNextFilePart() {
        if (this.pending == this.fileParts.size()) {
            this.status = DONE;
            return null;
        }
        File f = this.fileParts.get(this.pending);
        this.pending++;
        return f;
    }

    public File getDestination() {
        return this.dst;
    }

    public int getType() {
        return this.type;
    }

    public int getStatus() {
        return this.status;
    }

    public void addTransferredFilePart(File f) {
        this.transferredFileParts.add(f);
    }

    public List<File> getTransferredFileParts() {
        return this.transferredFileParts;
    }

    public int getTotalPartCount() {
        return this.fileParts.size();
    }

    public int getTransferredCount() {
        return this.transferredFileParts.size();
    }

    public File getSource() {
        return this.src;
    }
}

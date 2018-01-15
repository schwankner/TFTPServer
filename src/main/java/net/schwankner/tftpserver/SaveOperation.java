package net.schwankner.tftpserver;

import net.schwankner.tftplibrary.FileSystem;
import net.schwankner.tftplibrary.Messages.DataMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Schwankner on 15.01.18.
 */
public class SaveOperation {

    private String filename;
    private List<DataMessage> dataMap = new ArrayList<>();

    public SaveOperation(String filename) {
        this.filename = filename;
    }

    public boolean addDatapackage(DataMessage dataMessage) throws Exception {
        if (dataMessage.getSize() == 512) {
            this.dataMap.add(dataMessage);
            return false;
        } else if (dataMessage.getSize() < 512) {
            this.dataMap.add(dataMessage);
            writeFile();
            return true;
        } else {
            throw new Exception("Message payload greater than 512 byte, discarding!");
        }
    }

    private void writeFile() {
        int blobSize = 0;
        for (int i = 0; i < dataMap.size(); i++) {
            blobSize = blobSize + dataMap.get(i).getSize();
        }

        byte[] blob = new byte[blobSize];

        for (int i = 0; i < dataMap.size(); i++) {
            for (int j = 0; j < dataMap.get(i).getSize(); j++) {
                blob[j + i * 512] = dataMap.get(i).getPayload()[j];
            }
        }
        FileSystem.writeBlobToFile(blob,filename);
    }


}

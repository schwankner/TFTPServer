package net.schwankner.tftpserver;

import net.schwankner.tftplibrary.*;
import net.schwankner.tftplibrary.Messages.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Alexander Schwankner on 13.01.18.
 */
public class TFTPServer {

    private int port;
    private boolean verbose;
    private Map<InetAddress, ReceiveOperation> saveOperationsMap = new HashMap<>();

    public TFTPServer(int port, boolean verbose) {
        this.port = port;
        this.verbose = verbose;
    }

    public void run() {
        Network network = new Network(port);
        network.connect(true);

        System.out.println("Server waiting for packets...");
        while (true) {
            try {
                DatagramPacket packet = network.receivePacket();

                OpCode opcode = Utils.getOpCode(packet.getData());

                if (verbose) {
                    System.out.print("Packet type: " + opcode);
                }
                switch (opcode) {
                    case RRQ:
                        ReadMessage readMessage = new ReadMessage(packet.getData());
                        SendOperation sendOperation = new SendOperation();
                        System.out.println("Send file: " + readMessage.getFileName() + " to: " + packet.getAddress().toString());
                        sendOperation.createMessageListFromBin(FileSystem.readFileToBlob(readMessage.getFileName()));
                        for (DataMessage dataMessage : sendOperation.getMessageCollection()) {
                            network.sendPacket(dataMessage.buildBlob(), packet.getAddress(), packet.getPort(), false);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {

                            }
                        }
                        System.out.println("File " + readMessage.getFileName() + " send with: " + sendOperation.getDataSize() + " bytes");

                        break;
                    case WRQ:
                        WriteMessage writeMessage = new WriteMessage(packet.getData());
                        ReceiveOperation receiveOperation = new ReceiveOperation(writeMessage.getFileName());
                        saveOperationsMap.put(packet.getAddress(), receiveOperation);
                        if (verbose) {
                            System.out.print("\n");
                        }
                        System.out.println("Receive file: " + writeMessage.getFileName() + " from: " + packet.getAddress().toString());
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        (short) 0).buildBlob(),
                                        packet.getAddress(),
                                        packet.getPort(),
                                        false);
                        break;
                    case DATA:
                        DataMessage dataMessage = new DataMessage(packet.getData());
                        if (verbose) {
                            System.out.println(" #" + dataMessage.getPacketNumber());
                        }
                        try{
                            saveOperationsMap.get(packet.getAddress()).addDatapackage(dataMessage);
                        }catch (Exception e){
                            //@todo: return error message
                            System.out.println(e);
                        }
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        dataMessage.getPacketNumber()).buildBlob(),
                                        packet.getAddress(),
                                        packet.getPort(),
                                        false);
                        break;
                    case ACK:

                        break;
                    case ERROR:

                        break;
                    default:
                        break;
                }

            } catch (TimeoutException e) {
                break;
            }

        }

        network.close();
    }
}

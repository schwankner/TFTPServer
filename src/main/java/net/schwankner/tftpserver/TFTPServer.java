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
    private Map<InetAddress, ReceiveOperation> receiveOperationsMap = new HashMap<>();
    private Map<InetAddress, SendOperation> sendOperationsMap = new HashMap<>();

    public TFTPServer(int port, boolean verbose) {
        this.port = port;
        this.verbose = verbose;
    }

    public void run() {
        Network network = new Network(port);
        network.connect(true);

        System.out.println("Server waiting for requests...");
        while (true) {
            try {
                DatagramPacket packet = network.receivePacket();

                OpCode opcode = Utils.getOpCode(packet.getData());

                switch (opcode) {
                    case RRQ:
                        ReadMessage readMessage = new ReadMessage(packet.getData());
                        SendOperation sendOperation = new SendOperation();
                        sendOperation.createMessageListFromBin(FileSystem.readFileToBlob(readMessage.getFileName()));
                        verboseOutput("Got RRQ for " + readMessage.getFileName() + " from " + packet.getAddress().toString());
                        System.out.println("Write file: " + readMessage.getFileName() + " to: " + packet.getAddress().toString());
                        verboseOutput("File split in " + sendOperation.getMessageListSize() + " packets");
                        sendOperationsMap.put(packet.getAddress(), sendOperation);
                        try {
                            network.sendPacket(sendOperation.getMessageForSending().buildBlob(), packet.getAddress(), packet.getPort(), false);
                            verboseOutput("Send DATA #" + sendOperation.getLastSendMessage());
                        } catch (Exception e) {
                            System.err.println("Read file created no packets!");
                        }
                        break;
                    case WRQ:
                        WriteMessage writeMessage = new WriteMessage(packet.getData());
                        ReceiveOperation receiveOperation = new ReceiveOperation(packet.getAddress(), packet.getPort(), writeMessage.getFileName());
                        receiveOperationsMap.put(packet.getAddress(), receiveOperation);
                        verboseOutput("Got WRQ for " + writeMessage.getFileName() + " from " + packet.getAddress().toString());
                        System.out.println("Read file: " + writeMessage.getFileName() + " from: " + packet.getAddress().toString());
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        (short) 0).buildBlob(),
                                packet.getAddress(),
                                packet.getPort(),
                                false);
                        break;
                    case DATA:
                        DataMessage dataMessage = new DataMessage(packet.getData());
                        verboseOutput("Got DATA #" + dataMessage.getPacketNumber());
                        try {
                            verboseOutput("Send Ack #" + dataMessage.getPacketNumber());
                            receiveOperationsMap.get(packet.getAddress()).addDatapackage(dataMessage);
                            network.sendPacket(
                                    new AcknowledgementMessage(
                                            dataMessage.getPacketNumber()).buildBlob(),
                                    packet.getAddress(),
                                    packet.getPort(),
                                    false);
                        } catch (Exception e) {
                            //@todo: return error message
                            System.out.println(e);
                        }
                        break;
                    case ACK:
                        AcknowledgementMessage acknowledgementMessage = new AcknowledgementMessage(packet.getData());
                        verboseOutput("Got ACK #" + acknowledgementMessage.getPacketNumber());
                        SendOperation sendOperation1 = sendOperationsMap.get(packet.getAddress());
                        if (sendOperation1.getLastSendMessage() == acknowledgementMessage.getPacketNumber()) {
                            try {
                                network.sendPacket(sendOperation1.getMessageForSending().buildBlob(), packet.getAddress(), packet.getPort(), false);
                                verboseOutput("Send DATA #" + sendOperation1.getLastSendMessage());
                            } catch (Exception e) {
                                System.out.println("File with: " + sendOperation1.getDataSize() + " bytes send to: " + packet.getAddress());
                            }
                        } else {
                            System.err.println("illeagal ACK received!");
                        }

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

    private void verboseOutput(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }
}

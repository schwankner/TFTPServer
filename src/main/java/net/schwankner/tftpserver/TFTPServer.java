package net.schwankner.tftpserver;

import net.schwankner.tftplibrary.Messages.AcknowledgementMessage;
import net.schwankner.tftplibrary.Messages.DataMessage;
import net.schwankner.tftplibrary.Messages.OpCode;
import net.schwankner.tftplibrary.Messages.WriteMessage;
import net.schwankner.tftplibrary.Network;
import net.schwankner.tftplibrary.Utils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Alexander Schwankner on 13.01.18.
 */
public class TFTPServer {

    private Map<InetAddress,SaveOperation> saveOperationsMap = new HashMap<>();

    public void run(int port) {
        Network network = new Network(port);
        network.connect(true);

        while (true) {
            try {
                DatagramPacket packet = network.receivePacket();

                OpCode opcode = Utils.getOpCode(packet.getData());

                switch (opcode) {
                    case RRQ:

                        break;
                    case WRQ:
                        WriteMessage writeMessage = new WriteMessage(packet.getData());
                        SaveOperation saveOperation = new SaveOperation(writeMessage.getFileName());
                        saveOperationsMap.put(packet.getAddress(),saveOperation);
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        (short) 0).buildBlob(),
                                        packet.getAddress(),
                                        packet.getPort(),
                                        false);
                        break;
                    case DATA:
                        DataMessage dataMessage = new DataMessage(packet.getData());
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
                System.out.println(opcode);

            } catch (TimeoutException e) {
                break;
            }

        }

        network.close();
    }
}

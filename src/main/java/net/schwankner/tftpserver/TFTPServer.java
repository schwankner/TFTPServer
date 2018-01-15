package net.schwankner.tftpserver;

import net.schwankner.tftplibrary.Messages.AcknowledgementMessage;
import net.schwankner.tftplibrary.Messages.DataMessage;
import net.schwankner.tftplibrary.Messages.OpCode;
import net.schwankner.tftplibrary.Messages.WriteMessage;
import net.schwankner.tftplibrary.Network;
import net.schwankner.tftplibrary.Utils;

import java.net.DatagramPacket;
import java.util.concurrent.TimeoutException;

/**
 * Created by Alexander Schwankner on 13.01.18.
 */
public class TFTPServer {
    public static void main(String[] args) {
        Network network = new Network(69);
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
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        (short) 0).buildBlob(),
                                        packet.getAddress(),
                                        packet.getPort(),
                                        false);
                        break;
                    case DATA:
                        DataMessage dataMessage = new DataMessage(packet.getData());
                        network.sendPacket(
                                new AcknowledgementMessage(
                                        dataMessage.getPacketNumber()).buildBlob(),
                                        packet.getAddress(),
                                        packet.getPort(),
                        false);
                        System.out.println(dataMessage.getPacketNumber());
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

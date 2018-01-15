package net.schwankner.tftpserver;

import org.apache.commons.cli.*;
import java.net.UnknownHostException;

/**
 * Created by JH on 15.01.18.
 */
public class TFTPServerApplication {
    public static void main(String[] args) throws ParseException, UnknownHostException {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("n", "protocol", true, "use tcp or udp as transport protocol. Default: udp");
        options.addOption("p", "port", true, "port for connection with remote host. Default: 69");
        options.addOption("t", "timeout", true, "timeout between sending and retries. Default: 10");
        options.addOption("r", "retries", true, "How many times tftserver retries to send its messages. Default: 5");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.getArgs().length <= 0 && line.getOptions().length <= 0) {
                helpInformation(options);
                System.exit(1);
            }
            System.exit(0);

        } catch (
                ParseException exp)

        {
            System.out.println("Unexpected exception:" + exp.getMessage());
            System.exit(1);
        }
    }

    /**
     * automatically generate the help statement
     *
     * @param options
     */
    private static void helpInformation(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tftpserver", options);
    }
}
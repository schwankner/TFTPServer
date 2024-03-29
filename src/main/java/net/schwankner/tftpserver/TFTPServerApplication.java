package net.schwankner.tftpserver;

import org.apache.commons.cli.*;

/**
 * Created by JH on 15.01.18.
 */
public class TFTPServerApplication {
    public static void main(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("p", "port", true, "port for connection with remote host. Default: 69");
        options.addOption("t", "timeout", true, "timeout between sending and retries. Default: 10");
        options.addOption("r", "retries", true, "how many times tftserver retries to send its messages. Default: 5");
        options.addOption("v", "verbose", false, "verbose output for debuging");
        options.addOption("h", "help", false, "echos this help");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                helpInformation(options);
                System.exit(0);
            }

            boolean verbose = line.hasOption("verbose");
            TFTPServer tftpServer = new TFTPServer(
                    Integer.parseInt(line.getOptionValue("port", "69")),
                    Integer.parseInt(line.getOptionValue("timeout", "10")) * 1000,
                    Integer.parseInt(line.getOptionValue("retries", "5")),
                    verbose
            );
            tftpServer.run();
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
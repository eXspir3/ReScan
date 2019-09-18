import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;



public class ReScan {

    public static void main(String[] args) throws IOException {
        int mode = 0;
        CommandLine commandLine;
        File file = null;

        Option option_help = Option.builder("help").required(false).desc("Show HelpPage").build();
        Option option_RequestsFile = Option.builder("f").required(true).desc("Specify a File With Requests").hasArg().build();
        Option option_proxy = Option.builder("proxy").required(false).desc("Choose this option to use Proxy").build();
        Option option_host = Option.builder("host").required(false).desc("Choose Proxy Host").hasArg().build();
        Option option_port = Option.builder("port").required(false).desc("Choose Proxy Port").hasArg().build();
        Option option_user = Option.builder("user").required(false).desc("Choose proxy User").hasArg().build();
        Option option_pass = Option.builder("pass").required(false).desc("Choose Proxy Pass").hasArg().build();
        Option option_mode = Option.builder("m").required(true)
                .desc("Select 1 for Resending Requests with ResponseChecks or 0 for no Checks").hasArg().build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption(option_RequestsFile);
        options.addOption(option_mode);
        options.addOption(option_proxy);
        options.addOption(option_host);
        options.addOption(option_port);
        options.addOption(option_user);
        options.addOption(option_pass);
        options.addOption(option_help);

        try {
            commandLine = parser.parse(options, args);
            System.out.println("ReScan Version 1.0.5 - Author: Philipp Ensinger");
            System.out.println("===============================================");
            if (commandLine.hasOption("help")){
                System.out.println("ReScan Version 1.0.5\n\n" +
                        "CommandLine Arguments:\n" +
                        "\n" +
                        "-f Specify the .txt File containing your Requests and Options in special formatting (guide on github)\n" +
                        "-m Specify the Mode to be used, currently supported:\n" +
                        "-m 0 Send the Requests and Save to Responses with no further Checking\n" +
                        "-m 1 Send the Requests and Check the Responses with your Assertions and save only the Errors\n" +
                        "\n"
                +"For more Information see: https://github.com/eXspir3/ReScan");
                System.exit(0);
            }
            if (commandLine.hasOption("f")) {
                file = new File(commandLine.getOptionValue("f"));
                System.out.println("Using Requests-File: " + file.getAbsolutePath() + "\n");
            }
            if (commandLine.hasOption("m")) {
                mode = Integer.parseInt(commandLine.getOptionValue("m"));
            }
            if (commandLine.hasOption("proxy")) {
                System.out.println("proxy Not Implemented");
            }


        } catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
            System.exit(-1);
        }

        ReScanHandler handler = new ReScanHandler(file);
        if(mode==0){
            System.out.println("-m Mode = 0 --> Assertions will NOT be checked!\n");
            handler.replayNoOptions();
        }
        if(mode==1){
            System.out.println("-m Mode = 1 --> Assertions will be checked!\n");
            handler.replayWithOptions();
        }



    }

}

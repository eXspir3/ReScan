import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

public class ReScan {

    private final static String helpArgumentText = "ReScan Version 1.0.9\n\n" +
            "CommandLine Arguments:\n" +
            "\n" +
            "-f Specify the .txt File containing your Requests and Options in special formatting (guide on github)\n" +
            "-m Specify the Mode to be used, currently supported:\n" +
            "-m 0 Send the Requests and Save to Responses with no further Checking\n" +
            "-m 1 Send the Requests and Check the Responses with your Assertions and save only the Errors\n" +
            "\n"
            +"For more Information see: https://github.com/eXspir3/ReScan";

    private final static String greeting =
            "\n\n===============================================\n" +
            "ReScan Version 1.0.9 - Author: Philipp Ensinger\n" +
            "===============================================\n\n";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        System.out.println(greeting);
        int mode = 0;
        CommandLine commandLine;
        Path requestsFile = null;
        Path pubKey = null;
        Path privKey = null;
        Path aesKey = null;


        Option option_help = Option.builder("help").required(false).desc("Show HelpPage").build();
        Option option_RequestsFile = Option.builder("f").required(true).desc("Specify a File With Requests").hasArg().build();
        Option option_proxy = Option.builder("proxy").required(false).desc("Choose this option to use Proxy").build();
        Option option_host = Option.builder("host").required(false).desc("Choose Proxy Host").hasArg().build();
        Option option_port = Option.builder("port").required(false).desc("Choose Proxy Port").hasArg().build();
        Option option_user = Option.builder("user").required(false).desc("Choose proxy User").hasArg().build();
        Option option_pass = Option.builder("pass").required(false).desc("Choose Proxy Pass").hasArg().build();
        Option option_decryptRequests = Option.builder("decryptRequests").required(false).desc("decrypt the Requests File").build();
        Option option_privKey = Option.builder("privKey").required(false).desc("File with RSA PrivateKey").hasArg().build();
        Option option_pubKey = Option.builder("pubKey").required(false).desc("File with RSA PublicKey").hasArg().build();
        Option option_aesKey = Option.builder("aesKey").required(false).desc("File with RSA Encrypted AES-Key").hasArg().build();
        Option option_encryptResults = Option.builder("encryptResults").required(false).desc("Encrypt the Results File").build();
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
        options.addOption(option_encryptResults);
        options.addOption(option_decryptRequests);
        options.addOption(option_pubKey);
        options.addOption(option_privKey);
        options.addOption(option_aesKey);

        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")){
                System.out.println(helpArgumentText);
                System.exit(0);
            }
            if (commandLine.hasOption("f")) {
                requestsFile = Paths.get(commandLine.getOptionValue("f"));
                System.out.println("Using Requests-File: " + requestsFile.getFileName() + "\n");
                if (commandLine.hasOption("decryptRequests")) {
                    if(commandLine.hasOption("aesKey") && commandLine.hasOption("privKey")){
                        privKey = Paths.get(commandLine.getOptionValue("privKey"));
                        aesKey = Paths.get(commandLine.getOptionValue("aesKey"));
                    }else{
                        throw new ParseException("-aesKey and -privKey are required when using -decryptRequests");
                    }
                }
                if (commandLine.hasOption("encryptResults")) {
                    if (commandLine.hasOption("pubKey")) {
                        pubKey = Paths.get(commandLine.getOptionValue("pubKey"));
                    } else {
                        throw new ParseException("-pubKey has to be specified when using -encryptResults\nBe sure to pay Attention to case-sensitivity!");
                    }
                }
            }
            if (commandLine.hasOption("m")) {
                mode = Integer.parseInt(commandLine.getOptionValue("m"));
            }
            if (commandLine.hasOption("proxy")) {
                throw new ParseException("-proxy is currently not yet implemented");
            }

        } catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
            System.exit(-1);
        }

        ReScanHandler handler = new ReScanHandler(requestsFile, privKey, pubKey, aesKey);
        if(mode == 0){
            System.out.println("-m Mode = 0 --> Assertions will NOT be checked!");
            System.out.println("Running ...\n");
            handler.replayNoAssertions();
        }
        if(mode == 1){
            System.out.println("-m Mode = 1 --> Assertions will be checked!");
            System.out.println("Running ...\n");
            handler.replayWithAssertions();
        }
    }
}

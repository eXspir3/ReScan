import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;



public class ReqReTest {

    public static void main(String[] args) throws IOException, ParseException {
        Integer mode = 0;
        CommandLine commandLine;
        File file = null;

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

        try {
            commandLine = parser.parse(options, args);
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

        ReTestHandler handler = new ReTestHandler(file);
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

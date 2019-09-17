import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;



public class ReqReTest {
    private static final String testRequest = "GET https://www.google.at HTTP/1.1\n"+
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0\n"+
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"+
            "Accept-Language: de,en-US;q=0.7,en;q=0.3\n"+
            "Connection: keep-alive\n"+
            "Upgrade-Insecure-Requests: 1\n"+
            "Host: google.at";


    public static void main(String[] args) throws IOException {
        CommandLine commandLine;
        Option option_RequestsFile = Option.builder("f")
                .required(true)
                .desc("Specify a File With Requests")
                .longOpt("opt1")
                .build();
        Option option_proxy = Option.builder("proxy")
                .required(false)
                .desc("Choose this option to use Proxy")
                .longOpt("opt2")
                .build();
        Option option_mode = Option.builder("m")
                .required(true)
                .desc("Select 1 for Resending Requests with ResponseChecks or 0 for no Checks")
                .longOpt("opt3")
                .build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption(option_RequestsFile);
        options.addOption(option_mode);
        options.addOption(option_proxy);

        try
        {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption("f"))
            {
                System.out.print("Option f is present.  The value is: ");
                System.out.println(commandLine.getOptionValue("f"));
            }

            if (commandLine.hasOption("m"))
            {
                System.out.print("Option m is present.  The value is: ");
                System.out.println(commandLine.getOptionValue("m"));
            }

            if (commandLine.hasOption("proxy"))
            {
                System.out.print("Option proxy is present.  The value is: ");
                System.out.println(commandLine.getOptionValue("proxy"));
            }

            {
                String[] remainder = commandLine.getArgs();
                System.out.print("Remaining arguments: ");
                for (String argument : remainder)
                {
                    System.out.print(argument);
                    System.out.print(" ");
                }

                System.out.println();
            }

        }
        catch (ParseException exception)
        {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }

        File file = new File("Requests.txt");
        ReTestHandler handler = new ReTestHandler(file);
        handler.replayNoOptions();
    }

}

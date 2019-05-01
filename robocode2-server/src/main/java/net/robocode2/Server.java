package net.robocode2;

import net.robocode2.server.ConnHandler;
import net.robocode2.server.GameServer;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

@Command(
        name = "Server",
        versionProvider = Server.VersionFileProvider.class,
        header = {
                "              __________",
                "             //       \\ I============###",
                " ___________//_________\\__\\______________",
                "( ___ __O____O____O____O____O____O__ ___ )",
                " / _ ) ___  ___  ___  ___  ___  ___ / __)",
                " \\_\\_\\/ _ \\| __)/ _ \\/ __// _ \\| _ \\\\__|",
                "      \\___/|___)\\___/\\___|\\___/|___/",
                "",
                "@|green     Build the best. Destroy the rest!|@",
                ""
        },
        descriptionHeading = "Description:%n",
        description = "Runs a Robocode 2 server"
)
public class Server implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ConnHandler.class);

    private final static int DEFAULT_PORT = 55000;

    @Option(names = {"-V", "--version"}, description = "display version info")
    private static boolean isVersionInfoRequested = false;

    @Option(names = {"-h", "--help"}, description = "display this help message")
    private static boolean isUsageHelpRequested = false;

    @Option(names = {"-p", "--port"}, type = Integer.class, description = "port number (default: " + DEFAULT_PORT + ")")
    private static Integer port = DEFAULT_PORT;

    @Spec
    private CommandSpec spec;

    public static void main(String[] args) {
        CommandLine.run(new Server(), System.out, CommandLine.Help.Ansi.ON, args);
    }

    public void run() {
        CommandLine cmdLine = new CommandLine(new Server());

        if (Server.isUsageHelpRequested) {
            cmdLine.usage(System.out);
            System.exit(0);
        } else if (Server.isVersionInfoRequested) {
            cmdLine.printVersionHelp(System.out);
            System.exit(0);
        } else {
            String[] banner = this.spec.usageMessage().header();
            for (String line : banner) {
                println(line);
            }
            cmdLine.printVersionHelp(System.out);
        }

        // Handle port
        if (port == null) {
            port = DEFAULT_PORT;
        } else if (port < 1024 || port > 65535) {
            System.err.println("Port must not be lower than 1024 or bigger than 65535");
            System.exit(-1);
        }

        // Run thread that checks standard input (stdin) for an exit signal ("q")
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.trim().equalsIgnoreCase("q")) {
                    System.exit(1);
                }
            }
        }).start();

        // Start game server on main thread
        new GameServer().start();
    }

    public static int getPort() {
        return port;
    }

    private void println(String s) {
        if (s == null)
            s = "null";
        AnsiConsole.systemInstall();
        System.out.println(CommandLine.Help.Ansi.AUTO.string(s));
        AnsiConsole.systemUninstall();
    }

    static class VersionFileProvider implements CommandLine.IVersionProvider {

        public String[] getVersion() throws Exception {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("version.txt");
            String version = "?";
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    version = br.readLine();
                }
            }
            return new String[]{"Robocode Server " + version};
        }
    }
}

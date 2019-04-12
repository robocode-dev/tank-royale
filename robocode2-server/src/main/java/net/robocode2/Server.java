package net.robocode2;

import net.robocode2.server.GameServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;

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
public class Server {

    private final static int DEFAULT_PORT = 55000;

    @Option(names = {"-V", "--version"}, description = "display version info")
    private static boolean isVersionInfoRequested = false;

    @Option(names = {"-h", "--help"}, description = "display this help message")
    private static boolean isUsageHelpRequested = false;

    @Option(names = {"-p", "--port"}, type = Integer.class, description = "port number (default: " + DEFAULT_PORT + ")")
    private static Integer port;

    public static void main(String[] args) {
        CommandLine cmdLine = new CommandLine(new Server());
        cmdLine.parse(args);

        if (Server.isUsageHelpRequested) {
            cmdLine.usage(System.out);
            System.exit(0);
        } else if (Server.isVersionInfoRequested) {
            cmdLine.printVersionHelp(System.out);
            System.exit(0);
        }

        // Handle port
        if (port == null) {
            port = DEFAULT_PORT;
        } else if (port < 1024 || port > 65535) {
            System.err.println("Port must not be lower than 1024 or bigger than 65535");
            System.exit(-1);
        }

        new GameServer().start();
    }

    public static int getPort() {
        return port;
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

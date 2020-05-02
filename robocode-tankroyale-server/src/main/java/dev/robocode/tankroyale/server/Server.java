package dev.robocode.tankroyale.server;

import dev.robocode.tankroyale.server.server.GameServer;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import static dev.robocode.tankroyale.server.model.GameSetup.DEFAULT_GAME_TYPE;

@Command(
        name = "Server",
        versionProvider = Server.VersionFileProvider.class,
        header = {
                "               __________",
                "              /          |DDD==============",
                "    _________|___________|____________",
                "  _/_ ______________________________ _\\_",
                " / _ ) ___  ___  ___  ___  ___  ___ / __|",
                " \\_\\_\\/ _ \\| __)/ _ \\/ __// _ \\| _ \\\\__|",
                "      \\___/|___)\\___/\\___|\\___/|___/",
                "",
                "           Robocode Tank Royale",
                "",
        },
        descriptionHeading = "Description:%n",
        description = "Runs a Robocode Tank Royale server"
)
public class Server implements Runnable {

    private final static int DEFAULT_PORT = 80;

    @Option(names = {"-V", "--version"}, description = "Display version info")
    @SuppressWarnings("CanBeFinal")
    private static boolean isVersionInfoRequested = false;

    @Option(names = {"-h", "--help"}, description = "Display this help message")
    @SuppressWarnings("CanBeFinal")
    private static boolean isUsageHelpRequested = false;

    @Option(names = {"-p", "--port"}, type = Integer.class, description = "Port number (default: " + DEFAULT_PORT + ")")
    @SuppressWarnings("CanBeFinal")
    private static Integer port = DEFAULT_PORT;

    @SuppressWarnings("CanBeFinal")
    @Option(names = {"-g", "--games"}, type = String.class, description = "Comma-separated list of game types (default: " + DEFAULT_GAME_TYPE + ")")
    private static String gameTypes = DEFAULT_GAME_TYPE;

    @Option(names = {"-s", "--secret"}, description = "Client secret used for access control")
    @SuppressWarnings("CanBeFinal")
    private static String secret = null;

    @Spec
    @SuppressWarnings("unused")
    private CommandSpec spec;

    private static CommandLine cmdLine = new CommandLine(new Server());

    private GameServer gameServer;

    public static void main(String[] args) {
//        System.setProperty("picocli.ansi", "true");
        System.exit(cmdLine.execute(args));
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
        if (port < 1 || port > 65535) {
            System.err.println("Port must not be lower than 1 or bigger than 65535.\n" +
                    "Default port is 80 used for http.");
            System.exit(-1);
        }

        // Run thread that checks standard input (stdin) for an exit signal ("q")
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.trim().equalsIgnoreCase("q")) {
                    gameServer.stop();
                    System.exit(1);
                }
            }
        }).start();

        // Start game server on main thread
        gameServer = new GameServer(gameTypes, secret);
        gameServer.start();
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
            return new String[]{"Robocode Tank Royale Server " + version};
        }
    }
}

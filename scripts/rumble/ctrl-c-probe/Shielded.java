import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Starts CtrlcArgs through the worker's Ctrl+C shield (see InterruptShield), with arguments passed as variables read late by cmd, and records
 * cmd's exit code. Usage: java Shielded.java <dir> <exitcode-for-child>
 */
public class Shielded {
    public static void main(String[] a) throws Exception {
        // Like the command line: Ctrl+C is caught, not obeyed.
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), s -> { });
        Path dir = Path.of(a[0]);
        String java = System.getProperty("java.home") + "\\bin\\java.exe";
        List<String> command = List.of(java, "-Dprobe.dir=" + dir, dir.resolve("CtrlcArgs.java").toString(), "a&b", "c!d", "e^f", "g%PATH%h", "i j", a[1]);
        String cmd = System.getenv("SystemRoot") + "\\System32\\cmd.exe";
        StringBuilder line = new StringBuilder("start \"\" /b /wait");
        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> env = builder.environment();
        for (int i = 0; i < command.size(); i++) {
            env.put("RUMBLE_ARG_" + i, command.get(i));
            line.append(" \"!RUMBLE_ARG_").append(i).append("!\"");
        }
        line.append(" & exit !errorlevel!");
        List<String> all = new ArrayList<>(List.of(cmd, "/d", "/v:on", "/c"));
        all.add(line.toString());
        builder.command(all).redirectInput(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT);
        int code = builder.start().waitFor();
        Files.writeString(dir.resolve("cmd-exit.txt"), Integer.toString(code));
    }
}

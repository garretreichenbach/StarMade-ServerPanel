package server;

import server.data.Commands;
import server.data.config.PanelConfigManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class StarMadeUtils {

	private static Process process;
	private static String gameVersion;
	private static final ArrayList<String> args = new ArrayList<>();

	public static void start(String gameVersion, ArrayList<String> args) {
		StarMadeUtils.gameVersion = gameVersion;
		StarMadeUtils.args.addAll(args);
		int javaVersion = 23;
		if(gameVersion.startsWith("0.1") || gameVersion.startsWith("0.2")) javaVersion = 8;
		String javaPath = OperatingSystem.getJavaPath(javaVersion);

		String maxMemory = args.get(0);
		String minMemory = "Xms1024m";
		String port = args.get(1);

		try {
			ProcessBuilder builder = new ProcessBuilder(javaPath, "-jar", "StarMade.jar", "-Xmx" + maxMemory + "m", minMemory, "-force -port:" + port);
			builder.directory(getSMFolder());
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectError(ProcessBuilder.Redirect.INHERIT);
			process = builder.start();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	public static File getSMFolder() {
		return new File(String.valueOf(PanelConfigManager.ConfigValues.SM_FOLDER.getValue()));
	}

	public static void forceStop() {
		if(process != null) process.destroy();
	}

	private static void sendCommandRaw(String command, Object... args) {
		if(process != null) {
			try {
				process.getOutputStream().write((command + " " + args + "\n").getBytes(StandardCharsets.UTF_8));
				process.getOutputStream().flush();
			} catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public static void sendCommand(Commands command, Object... args) {
		sendCommandRaw(command.getCommand(), args);
	}

	public static void stop(int seconds) {
		sendCommand(Commands.STOP, seconds);
	}

	public static void restart(int seconds) {
		sendCommand(Commands.STOP, seconds);
		new Thread(() -> {
			try {
				Thread.sleep(seconds * 1000L);
				Thread.sleep(5000); //Wait for server to stop
				forceStop();
				Thread.sleep(1000);
				start(gameVersion, args);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}).start();
	}
}

package server.data;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public enum Commands {

	STOP("shutdown");

	private String command;

	Commands(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
}

package com.marcnuri.mnlauncher

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2017-10-07.
 */
@SuppressWarnings("GroovyUnusedAssignment")
class MenuEntry {
	private String name
	private Object command
	private List<MenuEntry> entries

	String getName() {
		return name
	}

	void setName(String name) {
		this.name = name
	}

	List<String> getCommand() {
		return command instanceof List ? command : Arrays.asList(command)
	}

	void setCommand(Object command) {
		this.command = command
	}

	String getFirstCommand() {
		return command instanceof List ? ++((List) command).iterator() : command
	}

	List<MenuEntry> getEntries() {
		return entries
	}

	void setEntries(List<MenuEntry> entries) {
		this.entries = entries
	}
}

package dk.dtu.tanktrouble.data.records;

import java.io.Serializable;

public record ChatRecord(String name, String message) implements Serializable {

	@Override
	public String toString() {
		if (name.equals(""))
			return message;
		return name + ": " + message;
	}
}

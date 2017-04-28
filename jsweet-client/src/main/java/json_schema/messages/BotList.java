package json_schema.messages;

public class ControllerHandshake extends Message {

	public ControllerHandshake() {
		super("controller-handshake");
	}

	public void setName(String name) {
		$set("name", name);
	}

	public void setVersion(String version) {
		$set("version", version);
	}

	public void setAuthor(String author) {
		$set("author", author);
	}
}
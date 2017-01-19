package json_schema;

public class ObserverHandshake extends Message {

	public ObserverHandshake() {
		super("observer-handshake");
	}

	public void setName(String name) {
		setField("name", name);
	}

	public void setVersion(String version) {
		setField("version", version);
	}

	public void setAuthor(String author) {
		setField("author", author);
	}
}
package json_schema;

public class ObserverHandshake extends Message {

	public ObserverHandshake() {
		super("observer-handshake");
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
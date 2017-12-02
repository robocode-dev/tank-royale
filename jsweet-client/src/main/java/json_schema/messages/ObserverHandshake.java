package json_schema.messages;

public class ObserverHandshake extends Message {

	public static final String TYPE = "observerHandshake";

	public ObserverHandshake() {
		super(TYPE);
	}

	public ObserverHandshake(String type) {
		super(type);
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
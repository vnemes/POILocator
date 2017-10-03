package datastructures;

public class RLeafEntry extends REntry {
	private String name;
	private String tag;

	public RLeafEntry(Box boundingBox, String name, String tag) {
		super(boundingBox);
		this.name = name;
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String toString() {
		return name.trim() + " " + tag.trim() + " " + super.toString();
	}

}

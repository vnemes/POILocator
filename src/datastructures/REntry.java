package datastructures;

public class REntry {
	private Box boundingBox;

	public REntry(Box boundingBox) {
		this.boundingBox = boundingBox;
	}

	public boolean contains(Box b) {
		return boundingBox.contains(b);
	}

	public boolean contains(Point x) {
		return boundingBox.contains(x);
	}

	public Box getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(Box boundingBox) {
		this.boundingBox = boundingBox;
	}

	@Override
	public String toString() {
		return boundingBox.toString();
	}
}

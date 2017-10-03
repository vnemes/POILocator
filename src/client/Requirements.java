package client;

public interface Requirements {

	public boolean buildTree(String name);

	public boolean buildTreeFromOSM(String filename);

	public Object find(String tag, double range, double x, double y);

	public void add(String name, String tag, double x, double y);

	public Object getAllTags();
}

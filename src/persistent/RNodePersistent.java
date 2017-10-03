package persistent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import datastructures.Box;
import datastructures.Point;
import datastructures.REntry;
import datastructures.RLeafEntry;

public class RNodePersistent {
	private static final int NODESIZE = 4096; // in Bytes
	private static final int LEAFBRANCHINGFACTOR = (NODESIZE - 2) / 178 / 2;
	private static final int NODEBRANCHINGFACTOR = (NODESIZE - 2) / 40 / 2;
	private ArrayList<REntry> entries;
	private boolean isLeaf;

	public RNodePersistent(ArrayList<REntry> entries, boolean isLeaf) {
		super();
		this.entries = entries;
		this.isLeaf = isLeaf;
	}

	public RNodePersistent() {
		this.entries = new ArrayList<REntry>();
		this.isLeaf = true;
	}

	public RNodePersistent(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		entries = new ArrayList<REntry>();
		isLeaf = buffer.get() == -1 ? true : false;
		int numberOfEntries = buffer.get();
		if (isLeaf) {
			for (int i = 0; i < numberOfEntries; i++) {
				byte[] nameBytes = new byte[128];
				buffer.get(nameBytes);
				String name = new String(nameBytes, StandardCharsets.UTF_8);
				byte[] tagBytes = new byte[33];
				buffer.get(tagBytes);
				String tag = new String(tagBytes, StandardCharsets.UTF_8);
				double x = buffer.getDouble();
				double y = buffer.getDouble();
				entries.add(new RLeafEntry(new Box(x, y), name, tag));
			}
		} else {
			for (int i = 0; i < numberOfEntries; i++) {
				double lx = buffer.getDouble();
				double ly = buffer.getDouble();
				double ux = buffer.getDouble();
				double uy = buffer.getDouble();
				long childOffset = buffer.getLong();
				entries.add(new RInternalEntry(new Box(new Point(lx, ly), new Point(ux, uy)), childOffset));
			}
		}
	}

	public void addEntry(REntry x) {
		entries.add(x);
	}

	public boolean isFull() {
		return entries.size() == (isLeaf ? LEAFBRANCHINGFACTOR * 2 + 1 : NODEBRANCHINGFACTOR * 2 - 1);
	}

	public byte[] toByte() {
		ByteBuffer buffer = ByteBuffer.allocate(NODESIZE);
		buffer.put((byte) (isLeaf ? 0xFF : 0x00));
		buffer.put((byte) entries.size());
		if (isLeaf) {
			for (int i = 0; i < entries.size(); i++) {
				try {
					buffer.put(((RLeafEntry) entries.get(i)).getName().getBytes(StandardCharsets.UTF_8));
					buffer.position(130 + 177 * i);
					buffer.put(((RLeafEntry) entries.get(i)).getTag().getBytes(StandardCharsets.UTF_8));
					buffer.position(163 + 177 * i);
					buffer.putDouble(entries.get(i).getBoundingBox().getLowerLeft().getX());
					buffer.putDouble(entries.get(i).getBoundingBox().getLowerLeft().getY());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			for (int i = 0; i < entries.size(); i++) {
				try {
					buffer.putDouble(entries.get(i).getBoundingBox().getLowerLeft().getX());
					buffer.putDouble(entries.get(i).getBoundingBox().getLowerLeft().getY());
					buffer.putDouble(entries.get(i).getBoundingBox().getUpperRight().getX());
					buffer.putDouble(entries.get(i).getBoundingBox().getUpperRight().getY());
					buffer.putLong(((RInternalEntry) entries.get(i)).getChild());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return buffer.array();
	}

	public static int getNodesize() {
		return NODESIZE;
	}

	public void remove(long nodeOffset) {
		for (int i = 0; i < entries.size(); i++) {
			if (((RInternalEntry) entries.get(i)).getChild() == nodeOffset) {
				entries.remove(i);
				return;
			}
		}
	}

	public static int getLeafbranchingfactor() {
		return LEAFBRANCHINGFACTOR;
	}

	public static int getNodebranchingfactor() {
		return NODEBRANCHINGFACTOR;
	}

	public ArrayList<REntry> getEntries() {
		return entries;
	}

	public void setEntries(ArrayList<REntry> entries) {
		this.entries = entries;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public static int getNodeSize() {
		return NODESIZE;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (REntry rie : entries) {
			sb.append(rie.toString() + "\n");
		}
		return sb.toString();
	}

}

package filehandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import persistent.RNodePersistent;

public class IndexFile {
	private RandomAccessFile indexHandle;
	private long rootOffset;

	public IndexFile(String name) {
		File indexfile = new File(name.substring(0, name.length() - 4) + ".idx");
		try {
			this.indexHandle = new RandomAccessFile(indexfile, "rwd");
			setRootOffset(8L);
		} catch (FileNotFoundException e) {
			try {
				indexfile.createNewFile();
				this.indexHandle = new RandomAccessFile(indexfile, "rwd");
				setRootOffset(8L);
			} catch (IOException e1) {
				System.out.print("An error has been encountered: " + e.getMessage());
				e1.printStackTrace();
			}
		}
	}

	public RNodePersistent getRoot() {
		return getNode(rootOffset);
	}

	public long getRootOffset() {
		return rootOffset;
	}

	public void setRootOffset(long offset) {
		rootOffset = offset;
		try {
			indexHandle.seek(0L);
			indexHandle.writeLong(rootOffset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RNodePersistent getNode(long offset) {
		try {
			indexHandle.seek(offset);
			byte[] bb = new byte[RNodePersistent.getNodeSize()];
			if (indexHandle.read(bb) != -1)
				return new RNodePersistent(bb);
			else
				return new RNodePersistent();
		} catch (

		IOException e) {
			return new RNodePersistent();
		}
	}

	public long insertNode(RNodePersistent node) {
		try {
			long offset = indexHandle.length();
			indexHandle.seek(offset);
			indexHandle.write(node.toByte());
			return offset;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void insertNodeAtOffset(RNodePersistent node, long offset) {
		try {
			indexHandle.seek(offset);
			indexHandle.write(node.toByte());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			indexHandle.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

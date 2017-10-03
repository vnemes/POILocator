package persistent;

import datastructures.Box;
import datastructures.REntry;

public class RInternalEntry extends REntry {
	private long childOffset;

	public RInternalEntry(Box boundingBox, long children) {
		super(boundingBox);
		this.childOffset = children;
	}

	public long getChild() {
		return childOffset;
	}

	public void setChild(long children) {
		this.childOffset = children;
	}
	
	public String toString(){
		return "Box: "+super.toString()+" with child offset:\n"+childOffset;
	}
	
	
}

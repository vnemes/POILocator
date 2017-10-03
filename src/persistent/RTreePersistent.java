package persistent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.Requirements;
import datastructures.Box;
import datastructures.Point;
import datastructures.REntry;
import datastructures.RLeafEntry;
import filehandling.IndexFile;

public class RTreePersistent implements Requirements {
	private IndexFile indexFile;
	private ArrayList<RLeafEntry> findResults;
	private Set<String> tagSet;

	public boolean buildTree(String filename) {
		indexFile = new IndexFile(filename);
		tagSet = new HashSet<String>();
		indexFile.insertNode(new RNodePersistent());
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				int endIndex = line.lastIndexOf(',');
				double y = Double.parseDouble(line.substring(endIndex + 1));
				line = line.substring(0, endIndex);
				endIndex = line.lastIndexOf(',');
				double x = Double.parseDouble(line.substring(endIndex + 1));
				line = line.substring(0, endIndex);
				endIndex = line.lastIndexOf(',');
				String tag = line.substring(endIndex + 1);
				line = line.substring(0, endIndex);
				endIndex = line.lastIndexOf(',');
				String name = line.substring(endIndex + 1);
				insert(indexFile.getRootOffset(), -1L,
						new RLeafEntry(new Box(new Point(x, y), new Point(x, y)), name, tag));
				// String[] parts = line.split("\\,"); /* Does not work, some
				// names contain commas */
				// Point p = new Point(Double.parseDouble(parts[2]),
				// Double.parseDouble(parts[3]));
				// REntry r = new RLeafEntry(new Box(p, p), parts[0], parts[1]);
				// root.insert(root, (RLeafEntry) r);
			}
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
	}

	public boolean buildTreeFromOSM(String filename) {
		indexFile = new IndexFile(filename);
		tagSet = new HashSet<String>();
		indexFile.insertNode(new RNodePersistent());
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
			Document xmlDocument = docBuilder.parse(filename);
			Node osmRoot = xmlDocument.getFirstChild();
			NodeList osmXMLNodes = osmRoot.getChildNodes();
			for (int i = 1; i < osmXMLNodes.getLength(); i++) {
				Node item = osmXMLNodes.item(i);
				if (item.getNodeName().equals("node")) {
					NamedNodeMap attributes = item.getAttributes();
					NodeList tagXMLNodes = item.getChildNodes();
					String name = null;
					String tag = null;
					for (int j = 1; j < tagXMLNodes.getLength(); j++) {
						NamedNodeMap tagAttributes = tagXMLNodes.item(j).getAttributes();
						if (tagAttributes != null) {
							if (tagAttributes.getNamedItem("k").getNodeValue().equals("name"))
								name = tagAttributes.getNamedItem("v").getNodeValue();
							if (tagAttributes.getNamedItem("k").getNodeValue().equals("amenity"))
								tag = tagAttributes.getNamedItem("v").getNodeValue();
						}
					}
					if (name != null && tag != null) {
						insert(indexFile.getRootOffset(), -1L,
								new RLeafEntry(
										new Box(Double.parseDouble(attributes.getNamedItem("lat").getNodeValue()),
												Double.parseDouble(attributes.getNamedItem("lon").getNodeValue())),
										name, tag));
					}
				}
			}
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}

	}

	public ArrayList<RLeafEntry> find(String tag, double range, double x, double y) {
		findResults = new ArrayList<>();
		double latInDeg = range / 2 * 8.983111749910168882500898311175e-6;
		double longInDeg = range / 2 * 1.2682790721270308318642434081195e-5;
		Box searchBox = new Box(new Point(x - longInDeg, y - latInDeg), new Point(x + longInDeg, y + latInDeg));
		find(tag, searchBox, indexFile.getRootOffset());
		return findResults;
	}

	public void add(String name, String tag, double x, double y) {
		insert(indexFile.getRootOffset(), -1, new RLeafEntry(new Box(x, y), name, tag));
	}

	private void find(String tag, Box searchBox, long nodeOffset) {
		RNodePersistent node = indexFile.getNode(nodeOffset);
		if (node.isLeaf()) {
			for (REntry rie : node.getEntries()) {
				if (((RLeafEntry) rie).getTag().trim().equals(tag) && searchBox.contains(rie.getBoundingBox()))
					findResults.add((RLeafEntry) rie);
			}
		} else {
			for (REntry rie : node.getEntries()) {
				if (searchBox.intersects(rie.getBoundingBox())) {
					find(tag, searchBox, ((RInternalEntry) rie).getChild());
				}
			}
		}
	}

	public Set<String> getAllTags() {
		return tagSet;
	}

	private void insert(long nodeOffset, long rootOffset, RLeafEntry x) {
		RNodePersistent node = indexFile.getNode(nodeOffset);
		if (node.isLeaf()) {
			node.addEntry(x);
			tagSet.add(x.getTag().trim());
			if (node.isFull()) {
				split(node, nodeOffset, rootOffset);
			} else {
				indexFile.insertNodeAtOffset(node, nodeOffset);
			}
		} else {
			if (node.isFull()) {
				rootOffset = split(node, nodeOffset, rootOffset);
				insert(rootOffset, -1L, x);
			} else {
				Box minBox = null;
				int minIndex = 0;
				double minEnlargement = Double.MAX_VALUE;
				for (int i = 0; i < node.getEntries().size(); i++) {
					if (node.getEntries().get(i).contains(x.getBoundingBox())) {
						insert(((RInternalEntry) node.getEntries().get(i)).getChild(), nodeOffset, x);
						return;
					} else {
						Box tmpBox = node.getEntries().get(i).getBoundingBox().minimumEnlargement(x.getBoundingBox());
						double difference = tmpBox.getArea() - node.getEntries().get(i).getBoundingBox().getArea();
						if (difference < minEnlargement) {
							minBox = tmpBox;
							minEnlargement = difference;
							minIndex = i;
						}
					}
				}
				node.getEntries().get(minIndex).setBoundingBox(minBox);
				indexFile.insertNodeAtOffset(node, nodeOffset);
				insert(((RInternalEntry) node.getEntries().get(minIndex)).getChild(), nodeOffset, x);
			}
		}
	}

	private long split(RNodePersistent node, long nodeOffset, long rootOffset) {
		Box e1 = null, e2 = null;
		double mind = 0;
		for (REntry tmp1 : node.getEntries()) {
			for (REntry tmp2 : node.getEntries()) {
				if (tmp1 != tmp2) {
					Box j = tmp1.getBoundingBox().minimumEnlargement(tmp2.getBoundingBox());
					double d = j.getArea() - tmp1.getBoundingBox().getArea() - tmp2.getBoundingBox().getArea();
					if (d > mind) {
						mind = d;
						e1 = tmp1.getBoundingBox();
						e2 = tmp2.getBoundingBox();
					}
				}
			}
		}
		RNodePersistent n1 = new RNodePersistent(new ArrayList<REntry>(), node.isLeaf());
		RNodePersistent n2 = new RNodePersistent(new ArrayList<REntry>(), node.isLeaf());
		// int c1 = 0, c2 = 0;
		// int limit = node.isLeaf() ? RNodePersistent.getLeafbranchingfactor()
		// : RNodePersistent.getNodebranchingfactor();
		for (REntry rie : node.getEntries()) {
			// if (c1 < limit) {
			// if (c2 < limit) {
			if (e1.contains(rie.getBoundingBox())) {
				n1.addEntry(rie);
				// c1++;
			} else if (e2.contains(rie.getBoundingBox())) {
				n2.addEntry(rie);
				// c2++;
			} else if (e1.minimumEnlargement(rie.getBoundingBox()).getArea()
					- e1.getArea() < e2.minimumEnlargement(rie.getBoundingBox()).getArea() - e2.getArea()) {
				n1.addEntry(rie);
				e1 = e1.minimumEnlargement(rie.getBoundingBox());
				// c1++;
			} else {
				n2.addEntry(rie);
				e2 = (e2.minimumEnlargement(rie.getBoundingBox()));
				// c2++;
			}
			// } else {
			// n1.addEntry(rie);
			// e1.setBoundingBox(e1.minimumEnlargement(rie.getBoundingBox()));
			// }
			//// } else {
			//// n2.addEntry(rie);
			//// e2.setBoundingBox(e2.minimumEnlargement(rie.getBoundingBox()));
			//// }
		}

		indexFile.insertNodeAtOffset(n1, nodeOffset);
		RInternalEntry e1e = new RInternalEntry(e1, nodeOffset);
		RInternalEntry e2e = new RInternalEntry(e2, indexFile.insertNode(n2));
		if (rootOffset != -1) {
			RNodePersistent root = indexFile.getNode(rootOffset);
			root.remove(nodeOffset);
			root.addEntry(e1e);
			root.addEntry(e2e);
			indexFile.insertNodeAtOffset(root, rootOffset);
			return rootOffset;
		} else {
			RNodePersistent root = new RNodePersistent(new ArrayList<REntry>(), false);
			root.addEntry(e1e);
			root.addEntry(e2e);
			indexFile.setRootOffset(indexFile.insertNode(root));
			return indexFile.getRootOffset();
		}

	}

	public void cleanUp() {
		indexFile.close();
	}

}

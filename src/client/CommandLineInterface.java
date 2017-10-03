package client;

import java.util.ArrayList;
import java.util.Scanner;
import datastructures.RLeafEntry;
import persistent.RTreePersistent;

public class CommandLineInterface {
	private static RTreePersistent rTree;
	private static Scanner scan;
	private static final String HELPTEXT = "READ <geospatial-data-file> -- reads the file located at the geospatial-data-file path -- e.g., read /home/user/locations.txt."
			+ "Each line in the file will have the following format: \r\n<name>, <tag>, <lat>, <lng>\r\n"
			+ "FIND <tag> WITHIN <range> OF <lat>, <lng> -- returns all the locations having the tag tag that are in a range of range from the point given by the lat, lng coordinates -- e.g., FIND accomodation WITHIN 500m OF 45.747826, 21.226216\r\n"
			+ "INSERT <name> TAGGED WITH <tag> LOCATED AT <lat>, <lng> -- inserts the record identified by name, tag, lat and lng -- e.g., INSERT \"Muzeul Banatului\" TAGGED WITH museum LOCATED AT 45.757143, 21.232651\r\n"
			+ "SHOW TAGS -- lists all the tags have have at least one POI associated to them"
			+ "exit -- exits the program";

	public static void processCommand(String[] words) {
		try {
			if (words[0].equalsIgnoreCase("help")) {
				System.out.println(HELPTEXT);
				return;
			}
			if (words[0].equalsIgnoreCase("exit")) {
				System.out.println("The program will now exit..");
				scan.close();
				rTree.cleanUp();
				System.exit(0);
				return;
			}
			if (words[0].equalsIgnoreCase("read")) {
				rTree = new RTreePersistent();
				boolean status = false;
				if (words[1].substring(words[1].length() - 4).equalsIgnoreCase(".osm"))
					status = rTree.buildTreeFromOSM(words[1]);
				else
					status = rTree.buildTree(words[1]);
				if (status)
					System.out.println("Successfully build the R-Tree!");
				else
					System.out
							.println("Error! Please check the spelling of the file and the corectness of the format!");
				return;
			}
			if (words[0].equalsIgnoreCase("find")) {
				ArrayList<RLeafEntry> results = rTree.find(words[1], Double.parseDouble(words[3]),
						Double.parseDouble(words[5].substring(0, words[5].length() - 1)), Double.parseDouble(words[6]));
				System.out.println("Found " + results.size() + " " + words[1] + "(s) in the specified range");
				for (RLeafEntry rle : results) {
					System.out.println(rle.toString());
				}
				return;
			}
			if (words[0].equalsIgnoreCase("insert")) {
				StringBuffer sb = new StringBuffer(words[1]);
				int i = 2;
				while (!words[i].equalsIgnoreCase("tagged")) {
					sb.append(" " + words[i]);
					++i;
				}
				rTree.add(sb.toString(), words[i + 2],
						Double.parseDouble(words[i + 5].substring(0, words[i + 5].length() - 1)),
						Double.parseDouble(words[i + 6]));
				System.out.println("Entry inserted successfully!");
				return;
			}
			if (words[0].equalsIgnoreCase("show") && words[1].equalsIgnoreCase("tags")) {
				System.out.println("Listing all tags with at least one POI associated:");
				for (String s : rTree.getAllTags())
					System.out.println(s);
				return;
			}
			System.out.println("Invalid command: Type help for a list of availible commands!");
		} catch (Exception e) {
			System.out.println("Invalid command: Type help for a list of availible commands!");
		}
	}

	public static void run() {
		scan = new Scanner(System.in);
		while (true) {
			System.out.print(">>>");
			processCommand(scan.nextLine().split("\\s"));
		}
	}

	public static void main(String[] args) {
		run();
	}

}

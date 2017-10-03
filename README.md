# POILocator
## Project Description

This program accomplishes the task of resolving queries on Geo-Spatial data. The way it does so is by indexing the dataset using a variation of the B-Trees, namely *R-Trees* and specific algorithms for insertion and search.

The program accepts large datasets as inputs due to the *on-disk* implementation of the persistent R-Trees, and the queries	are optimized to run on these types of entries due to the index on the coordinates attribute.

### Example Commands
```
>>> 	read monaco-latest.osm or read locations.txt
>>>	find restaurant within 300 of 43.73118738604639, 7.4241193844412505
>>>	insert Middle of the Black Sea tagged with sea located at 43.045447, 34.130628
>>>	show tags
>>>	exit
```

### Design Considerations

The tree is designed to be built up of leaf nodes that contain the names, tags(UTF-8 encoded, fixed size) and the coordinates of the Points of interest and internal nodes that describe the *bounding boxes* and *pointers* (offsets in the index-file) to the child nodes. I opted for the quadratic-cost method of splitting the nodes and assigning the new *"seed"* boxes for the new nodes.

As for the search, the algorithm first builds the box that should contain all the entries around the given coordinates + range, then it	recursively iterates through all the boxes (and their child nodes) that intersect the respective *"search box"* until a leaf node is reached.	while inside the leaf node, I extract only the elements that are contained in the search box.
The command line interface accepts both regular text files and also the Open Street Map xml files from [here](http://download.geofabrik.de/europe.html).

I used the [Document Object Model](https://en.wikipedia.org/wiki/Document_Object_Model) library for parsing the xml, while only building the RTree with the with nodes	that have tags with both the keys *"name"* and [*"amenity"*](http://wiki.openstreetmap.org/wiki/Key:amenity).

### Index File Format

```
Node size = 4kB

first 8 bytes in file: rootOffset
-------------------------------------------------------
LEAF NODE
first byte:  is leaf or not
second byte: numberOfEntries in the node
23 Entries * 178 bytes:	- 128 bytes  Name
		   	-  33 bytes  Tag
		   	-   8 bytes  x coord
		   	-   8 bytes  y coord
-------------------------------------------------------
INTERNAL NODE
first byte:  is leaf or not
second byte: numberOfEntries in the node
102 Entries * 40 bytes:	-   8 bytes lower x
			-   8 bytes lower y
			-   8 bytes upper x
			-   8 bytes upper y
			-   8 bytes child offset
-------------------------------------------------------
```


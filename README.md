# guaranteed_rate

Solution to programming problem given as part of Eric Hogberg's interview for the Clojure software engineer position with Guaranteed Rate.

## Installation

You'll need Leiningen on your development box before doing any of the following.

Installation:

	git clone git@github.com:ehogberg/guaranteed_rate.git
	cd $PROJECTDIR
	lein deps

Optionally you can build a stand-alone JAR file:

	lein uberjar
	
This will generate `$PROJECTDIR/target/uberjar/guaranteed_rate-0.1.0-SNAPSHOT-standalone.jar`


The test suite can be run using:

	lein test
	

## Usage

	lein run -- [args]
	
Note that the `--` before the arguments are required.


or (much faster, but requires a JAR build.):

    $ java -jar /path/to/uberjar/guaranteed_rate-0.1.0-standalone.jar [args]

A demo which loads a number of sample data files then starts the web service can be run using:

	lein demo
	
	
## CLI Options

Both Lein and Java invocations support the following options:

**-p [file1 file2 file3...]** :  Load the content of each of the specified files and process it as records.  File content should be in the format specified by the homework instructions.  Most field content is accepted "as-is"; birth date must be in one of the following formats to be accepted: *MM/DD/YYYY*, *MM-DD-YYYY*, *YYYYMMDD* or *YYYY-MM-DD*.

Any number of files may be specified, although wildcards are *not* supported.   After processing is finished, print out all loaded records, using each of the sort orders specified in the homework instructions, followed by a dump of exceptions encountered during loading (if any.)

**-w** : Start the API webservice.  To terminate the service, press CTRL-C.

**-h** : Print a brief summary of the above options.

-p and -w can be used together at once.  In this case, file processing will be performed first, then the web service started.


## Discussion

##### Code Organization

Solution code is distributed in 5 namespaces:

- **api** : The public call-level interface for loading lines and retrieving loaded records/exceptions using a variety of sortings.  Defines an in-memory transactionally safe reference used to store loaded records.

- **core** : Functions for driving program execution, including CLI parsing/validation, processing data files and starting the web service.

- **reporting** : Functions used to neatly output record information, load job status and exception dumps.

- **resultset** : Functions which parse a line into a record, including validations and transformations.

- **web** : The web service implementation: endpoint definition, request routing and fulfillment.

##### Maps vs. Records

Clojure offers a **Record** construct which provides schema durability and protocol/interface support for structured data.  These features, while useful in complex data models, were felt to be overkill for the exercise requirements, which ended up using an unadorned map for record storage.

##### Concurrency

Clojure offers several abstractions which simplify the process of implementing side-by-side concurrent processing.  Although concurrency is not a requirement specified in the problem definition, two features utilize it:

- Data files are processed concurrently by wrapping file loading/line iteration in a `(future)`.  Each file to be processed has its own future created, which are then `(resolve)`ed to a processing summary.

- Loaded records and exceptions are stored using separate `ref`ed vectors.  Transactional nature of ref updates plays nicely with the web API functionality, in which multiple record posts could theoretically collide but have contention managed by the `dosync` wrapping the vector append.

##### Sorted Output

The sorting implementation takes advantage of the Clojure `(sort)` function's ability to accept pluggable keyfuncs (specifying which part of an record's content should be used to sort on) and comparators (functions which evaluate keyfunc data and determine it's ordering relative to other records.)   Rather than write separate functions for each of the 3 required sort types, a general-purpose sort call is made, with keyfunc and comparator definitions particular to the sort type being selected from a lookup map and plugged into the call.  This implementation lets one set of very simple code implement 3 different sorts, and with the benefit of other sort types being easily added in as needed.

##### Web API

The API service is implemented using Ring + Compojure for endpoint handling, plus a simple middleware wrapper to automatically generate JSON response payloads.  A more practical implementation in a production environment would use a library such as **compojure-api** which provides additional value-added functionality on top of these componenents (better security, web-based call interface, multiple output formats and similar.)  The supplied implementation was chosen for its simplicity and transparency to reviewers, allowing all the routing and procesing to take place with a minimal code footprint.


### Caveats

- Testing the **core** namespace is a challenge, as most of the namespace's functions are only meaningfully useful in tandem with other job control/processing functions.  The `process-files` test case exercises almost all of this job control functionality via a full multi-file load run and examination of the resulting output for markers demonstrating loading, proper exception handling and inclusion of the various sorted reports.

- Likewise, there is no direct testing of the **report** namespace as it does no state or algorithmic work, but manipulates and outputs strings.  Several of the reporting functions' outputs are indirectly tested through the process-files integration case mentioned above.  

- The web service implementation provided is effective at delivering the specified API services but is woefully incomplete with regard to providing security and scalability features expected in most production environments.

- Web service endpoints are provided by an embedded Jetty server, which uses a log4j-style logging service whose format and appearance is dramatically different from the println-approach used by the load functions.  The appearance of these two formats, intermingled, can be a bit jarring.



## Example Output

	lein run -- -p resources/test/test_data_errors.txt
	Processing file: resources/test/test_data_errors.txt
	File resources/test/test_data_errors.txt processing complete (14 records added, 6 exceptions)

	** Records sorted by last name (descending) **

	+----------------+----------------+---------+------------+------------+
	| Last Name      | First Name     | Gender  | Color      | Birthday   |
	+----------------+----------------+---------+------------+------------+
	| Zealander      | Brita          | Female  | Fuscia     | 03/04/1987 |
	| Yuryatin       | Hiram          | Male    | Aquamarine | 05/28/2012 |
	| Waylen         | Greg           | Male    | Blue       | 03/25/2012 |
	| Tuson          | Ollie          | Male    | Red        | 01/29/1995 |
	| Schimank       | Traver         | Male    | Khaki      | 02/03/2009 |
	| Marini         | Reidar         | Male    | Khaki      | 11/29/1997 |
	| Hrishanok      | Sloan          | Male    | Yellow     | 12/07/1988 |
	| Eastcourt      | Farris         | Male    | Yellow     | 01/21/1998 |
	| Dear           | Stephana       | Female  | Green      | 10/02/1997 |
	| Cohani         | Joelie         | Female  | Purple     | 05/19/1989 |
	| Carlucci       | Jervis         | Male    | Yellow     | 12/23/2007 |
	| Apark          | Kessiah        | Female  | Yellow     | 07/26/2010 |
	| Antusch        | Morissa        | Female  | Purple     | 01/23/1972 |
	| Abeles         | Gertrude       | Female  | Turquoise  | 03/11/2000 |
	+----------------+----------------+---------+------------+------------+


	** Records sorted by gender/last name **

	+----------------+----------------+---------+------------+------------+
	| Last Name      | First Name     | Gender  | Color      | Birthday   |
	+----------------+----------------+---------+------------+------------+
	| Abeles         | Gertrude       | Female  | Turquoise  | 03/11/2000 |
	| Antusch        | Morissa        | Female  | Purple     | 01/23/1972 |
	| Apark          | Kessiah        | Female  | Yellow     | 07/26/2010 |
	| Cohani         | Joelie         | Female  | Purple     | 05/19/1989 |
	| Dear           | Stephana       | Female  | Green      | 10/02/1997 |
	| Zealander      | Brita          | Female  | Fuscia     | 03/04/1987 |
	| Carlucci       | Jervis         | Male    | Yellow     | 12/23/2007 |
	| Eastcourt      | Farris         | Male    | Yellow     | 01/21/1998 |
	| Hrishanok      | Sloan          | Male    | Yellow     | 12/07/1988 |
	| Marini         | Reidar         | Male    | Khaki      | 11/29/1997 |
	| Schimank       | Traver         | Male    | Khaki      | 02/03/2009 |
	| Tuson          | Ollie          | Male    | Red        | 01/29/1995 |
	| Waylen         | Greg           | Male    | Blue       | 03/25/2012 |
	| Yuryatin       | Hiram          | Male    | Aquamarine | 05/28/2012 |
	+----------------+----------------+---------+------------+------------+


	** Records sorted by birthdate **

	+----------------+----------------+---------+------------+------------+
	| Last Name      | First Name     | Gender  | Color      | Birthday   |
	+----------------+----------------+---------+------------+------------+
	| Antusch        | Morissa        | Female  | Purple     | 01/23/1972 |
	| Zealander      | Brita          | Female  | Fuscia     | 03/04/1987 |
	| Hrishanok      | Sloan          | Male    | Yellow     | 12/07/1988 |
	| Cohani         | Joelie         | Female  | Purple     | 05/19/1989 |
	| Tuson          | Ollie          | Male    | Red        | 01/29/1995 |
	| Dear           | Stephana       | Female  | Green      | 10/02/1997 |
	| Marini         | Reidar         | Male    | Khaki      | 11/29/1997 |
	| Eastcourt      | Farris         | Male    | Yellow     | 01/21/1998 |
	| Abeles         | Gertrude       | Female  | Turquoise  | 03/11/2000 |
	| Carlucci       | Jervis         | Male    | Yellow     | 12/23/2007 |
	| Schimank       | Traver         | Male    | Khaki      | 02/03/2009 |
	| Apark          | Kessiah        | Female  | Yellow     | 07/26/2010 |
	| Waylen         | Greg           | Male    | Blue       | 03/25/2012 |
	| Yuryatin       | Hiram          | Male    | Aquamarine | 05/28/2012 |
	+----------------+----------------+---------+------------+------------+


	** Exceptions **
	File: resources/test/test_data_errors.txt
	Line: Bedward Tiebold Male
	Exception: Incomplete record

	File: resources/test/test_data_errors.txt
	Line: Plumtree Rycca Female Aquamarine
	Exception: Incomplete record

	File: resources/test/test_data_errors.txt
	Line: Brady Richart Male Yellow 1981-MONTH-25
	Exception: Converting birthdate 1981-MONTH-25 to datetype failed

	File: resources/test/test_data_errors.txt
	Line: Dogg , Johanna ,  , Green , 1995-12-31
	Exception: At least one required field missing

	File: resources/test/test_data_errors.txt
	Line: Elks
	Exception: Incomplete record

	File: resources/test/test_data_errors.txt
	Line: Blondin Blair Female Khaki a008-06-05
	Exception: Converting birthdate a008-06-05 to datetype failed

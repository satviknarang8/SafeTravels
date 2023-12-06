# CSV and API Server

## Server Class
The Server class serves as the entry point for the application. It initializes a web server using the Spark framework and configures various HTTP routes to handle data loading, viewing, and searching. The key methods and attributes in this class include:
getDefensiveRows(): Returns a defensive copy of the loaded CSV data.
setParser(): Sets the CSV parser used for data loading.
getParser(): Retrieves the CSV parser.
setFileLoaded(): Sets the flag to indicate whether a CSV file is loaded.
getFileLoaded(): Retrieves the file loaded flag.
setRows(): Sets the loaded CSV data rows.
main(): The main method that starts the web server and initializes routes.

## ACSAPIBroadbandSource Class
The ACSAPIBroadbandSource class implements the ACSAPISource interface and is responsible for fetching broadband usage data from the Census API. Key methods and attributes include:
fetchStateIDMap(): Fetches a map of state names to state IDs from the Census API.
getBroadbandUsage(): Retrieves broadband usage data for a specified state and county.
broadbandStatistics(): Fetches broadband statistics data from the Census API.
connect(): A private helper method for establishing an HTTP connection to the API.
deserializeCensusData(): A private helper method for deserializing JSON data from the API.
ACSAPISource Interface
The ACSAPISource interface defines a contract for data sources that provide broadband usage information from the Census API. It includes the getBroadbandUsage() method.

## CachedACSAPIBroadbandSource Class
The CachedACSAPIBroadbandSource class wraps an ACSAPISource implementation and adds caching functionality using Google Guava's LoadingCache. Key methods and attributes include:
getBroadbandUsage(): Retrieves broadband usage data from the cache or the wrapped source.
Caching parameters such as maximum size and expiration time.
CSVResponse Class
The CSVResponse class is responsible for serializing a response object to JSON format. It includes the serialize() method for converting a response map to JSON.

## LoadCSVHandler Class
The LoadCSVHandler class handles HTTP requests for loading and parsing CSV files. It includes the handle() method for processing file loading requests.

## SearchCSVHandler Class
The SearchCSVHandler class handles HTTP requests for searching within loaded CSV data. It includes the handle() method for processing search requests.

## ViewCSVHandler Class
The ViewCSVHandler class handles HTTP requests for viewing loaded CSV data. It includes the handle() method for processing view requests.

# Usage
To run the CSV Data Server, follow these steps:
Build and compile the Java application.
Start the server, which will listen on a specified port.
Use HTTP requests to interact with the server, including loading, viewing, and searching CSV data.

# Dependencies
Spark: Used for creating the web server and handling HTTP requests.
Google Guava: Used for caching data in CachedACSAPIBroadbandSource.
Moshi: Used for JSON serialization and deserialization.
Other standard Java libraries.

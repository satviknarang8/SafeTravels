maps-gjacobs5-fpabari

## Project Details

- **Project Name:** Overlay Map Application
- **Description:** This project involves the development of a React application that utilizes Mapbox to display geographical overlays, including redlining data and location searches. The application provides interactive features for users to explore and analyze geographical data.

- **Total Estimated Time:** We spent around 14 hours working on this project 
- **Repository Link:** https://github.com/cs0320-f23/maps-gjacobs5-fpabari.git.
### Frontend-
# CSV and API Server-
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
The ACSAPISource interface defines a contract for data sources that provide broadband usage information from the Census API. It includes the getBroadbandUsage() method..
## CachedACSAPIBroadbandSource Class
The CachedACSAPIBroadbandSource class wraps an ACSAPISource implementation and adds caching functionality using Google Guava's LoadingCache. Key methods and attributes include:
getBroadbandUsage(): Retrieves broadband usage data from the cache or the wrapped source.
Caching parameters such as maximum size and expiration time.
CSVResponse Class
The CSVResponse class is responsible for serializing a response object to JSON format. It includes the serialize() method for converting a response map to JSON..
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
Other standard Java libraries.-
**App Component**.
The `App` component serves as the entry point for the application. It imports the main `MapBox` component and renders it within the overall application structure.
**fetchDataFromBackend Function**.
The `fetchDataFromBackend` function handles requests to the backend server. It appends the server URL to the provided endpoint and returns the JSON response. Error handling is included for non-OK responses.
**MapWidget Component**.
The `MapWidget` component is a functional React component that renders various UI elements related to map interaction and data display. Props include functionalities such as searching, toggling, and handling manual geocoding. It displays income data, broadband access information, and redlining data.
**GeoLayers (geoLayer and locLayer)**.
`geoLayer` and `locLayer` objects define styling configurations for rendering geographical data layers on the map. `geoLayer` is designed for redlining data, and `locLayer` is used for location search results. They utilize the Mapbox `FillLayer` type for rendering.
**fetchRedliningData Function**.
The `fetchRedliningData` function fetches redlining data from the backend based on specified geographical coordinates. It constructs the API URL, makes the request, and returns a `FeatureCollection` if successful. The `isFeatureCollection` function is a utility function to check if the returned data is a valid GeoJSON `FeatureCollection`.
**ACCESS_TOKEN**.
The `ACCESS_TOKEN` constant holds the Mapbox access token required for authentication in API requests.
#### Backend.
**MapBoxHandler**.
The `MapBoxHandler` class is a Spark Route responsible for handling requests to the Mapbox API for geocoding. It expects two parameters (`place` and `accessToken`) in the request. The class makes a request to the Mapbox API and returns the response.
**RedliningHandler**.
The `RedliningHandler` class is a Spark Route responsible for handling requests for redlining data. It reads GeoJSON data from a file, filters features based on bounding box parameters, and returns the filtered GeoJSON response. The class utilizes the Moshi library for parsing GeoJSON.
#### Server Class.
- The `Server` class represents the server application for handling CSV data.
- It utilizes the Spark framework for defining routes and handling HTTP requests.
- The server is configured to allow cross-origin resource sharing (CORS) by setting appropriate headers.
- It includes methods to manage CSV data, load CSV files, and handle different routes related to broadband data, redlining data, and Mapbox API requests..
#### BroadbandRouteUtility Package.
- The `BroadbandRouteUtility` package contains classes related to handling broadband data routes.
- `ACSAPIBroadbandSource` implements the `ACSAPISource` interface for fetching broadband usage data from the Census API.
- The `BroadbandHandler` class is a Spark `Route` responsible for handling requests related to broadband data.
- It uses an instance of `ACSAPIBroadbandSource` for data retrieval, allowing for caching to improve performance.
#### CSVRouteUtility Package.
- The `CSVRouteUtility` package contains classes for handling CSV data routes.
- `LoadCSVHandler` handles requests to load CSV data into the server.
- `ViewCSVHandler` handles requests to view loaded CSV data.
- `SearchCSVHandler` handles requests to search and filter CSV data-
#### Maps Package.
- The `Maps` package contains classes related to handling Mapbox API requests.
- `MapBoxHandler` is a Spark `Route` responsible for handling requests to the Mapbox API for geocoding.
- `RedliningHandler` is a Spark `Route` responsible for handling requests for redlining data-
#### Exception Handling.
- The `DatasourceException` and `InvalidArgsException` classes handle exceptions related to data source issues and invalid arguments, respectively-
## Tests
#### TestBroadbandSource
This set of JUnit tests is designed to validate the functionality of the BroadbandSource on the backend-
testEndpoint: Ensures that the /broadband endpoint responds with a status code of 200 for valid parameters.
testBroadbandUsage: Tests the broadband usage API with valid state and county parameters and checks the correctness of the response.
testCachedBroadbandUsage: Tests if the cached broadband usage data remains consistent after a short delay.
testInvalidState: Validates the error response when an invalid state parameter is provided.
testInvalidCounty: Validates the error response when an invalid county parameter is provided.
testInvalidInputs: Checks the error response when incomplete parameters are provided.
#### TestBroadbandUnit
The TestBroadbandUnit class contains JUnit tests for the ACSAPIBroadbandSource class in the BroadbandRouteUtility package-
fetchStateIDMap: Verifies the correct mapping of state names to state IDs by comparing the actual state ID map with the expected map.
getBroadbandUsage: Tests the getBroadbandUsage method by checking if the response contains specific data for the given state and county.
getBroadbandStatistics: Validates the output of the broadbandStatistics method by comparing it with the expected result for a given state and county.
connect: Tests the connection to the Census API by checking if the response code is 200.
deserializeCensusData: Validates the correct deserialization of Census data by comparing the actual result with the expected result.
#### TestRedlining
The TestRedlining class contains tests for the RedliningHandler class in the Maps package-
testEndpoint: Verifies that the /redlining endpoint returns a status code of 200 for valid parameters.
testRedliningFiltering: Tests the redlining data filtering by generating random bounding box parameters and checking if the response contains valid data within the specified range.
#### TestServerBroadband
The TestServerBroadband class includes tests for the broadband-related routes and handlers in the server-
testEndpoint: Verifies that the /broadband endpoint responds with a status code of 200 for valid parameters.
testBroadbandUsage: Tests the broadband usage route by checking if the response contains specific data for the given state and county.
testInvalidInput: Ensures that the server returns an error message for invalid input parameters.
testDatasourceError: Tests the server's response when there is an error in the data source.
#### TestServerCSV
The TestServerCSV class contains tests for the CSV-related routes and handlers in the server-
testEndpoints: Verifies that the /loadcsv, /viewcsv, and /searchcsv endpoints return a status code of 200.
testViewCSVNoLoad: Tests the server's response when attempting to view a CSV without loading it first.
testSearchCSVNoLoad: Tests the server's response when attempting to search a CSV without loading it first.
testLoadCSVValid: Tests the server's response when loading a valid CSV file.
testLoadCSVInvalid: Tests the server's response when attempting to load an invalid CSV file.
testLoadCSVEmpty: Tests the server's response when loading an empty CSV file.
testLoadAndViewCSV: Tests the server's response when loading and viewing a CSV file.
testLoadViewAndSearchCSV: Tests the server's response when loading, viewing, and searching a CSV file.
testLoadViewAndSearchCSVNoValidQuery: Tests the server's response when loading, viewing, and searching a CSV file with no valid query.
#### FuzzTest
The FuzzTest class performs a fuzz test on the serialization process of random data-
fuzzTestSerialize: Generates random data, serializes it, and checks if the serialized data is not equal to the previous serialized data, ensuring that the serialization process is not producing the same result consistently.I apologize for the confusion. Let's focus on describing the specific data structures used and their high-level usage in the project:

## Specific Data Structures and High-Level Architecture.
### Frontend.
#### Data Structures.
1. **GeoJSON:**
   - **Usage:** Represents geographical features, such as redlining data and location search results.
   - **Implementation:** Utilized to structure and convey geographical data in a standardized format.
2. **FeatureCollection:**
   - **Usage:** Groups GeoJSON features together.
   - **Implementation:** Facilitates the organization and presentation of multiple geographical features.
3. **Map Layers Configuration (geoLayer and locLayer):**
   - **Usage:** Define styling configurations for rendering geographical data layers on the map.
   - **Implementation:** Configures the appearance of map layers, such as redlining data and location search results, using the Mapbox `FillLayer` type.
#### High-Level Architecture.
1. **App Component:**
   - **Usage:** Entry point for the frontend application.
   - **Implementation:** Imports and renders the main `MapBox` component within the overall application structure.
2. **fetchDataFromBackend Function:**
   - **Usage:** Handles requests to the backend server, appends the server URL to the provided endpoint, and returns the JSON response.
   - **Implementation:** Ensures seamless communication between the frontend and backend, with error handling for non-OK responses.
3. **MapWidget Component:**
   - **Usage:** A functional React component that renders UI elements for map interaction and data display.
   - **Implementation:** Includes functionalities for searching, toggling, and manual geocoding. Displays income data, broadband access information, and redlining data.
4. **Map Layers (geoLayer and locLayer):**
   - **Usage:** Define styling configurations for rendering geographical data layers on the map.
   - **Implementation:** Configures the appearance of map layers, utilizing the Mapbox `FillLayer` type for rendering.
5. **fetchRedliningData Function:**
   - **Usage:** Fetches redlining data from the backend based on specified geographical coordinates.
   - **Implementation:** Constructs the API URL, makes the request, and returns a `FeatureCollection` if successful.
### Backend.
#### Data Structures.
1. **ACSAPISource Interface:**
   - **Usage:** Defines a contract for data sources providing broadband usage information from the Census API.
   - **Implementation:** Includes the `getBroadbandUsage()` method, establishing a standard for classes handling broadband data retrieval.
2. **CachedACSAPIBroadbandSource Class:**
   - **Usage:** Wraps an `ACSAPISource` implementation and adds caching functionality using Google Guava's `LoadingCache`.
   - **Implementation:** Enhances performance by caching broadband usage data, avoiding redundant API calls.
#### High-Level Architecture.
1. **Server Class:**
   - **Usage:** Represents the server application for handling CSV data, with defined routes for broadband data, redlining data, and Mapbox API requests.
   - **Implementation:** Utilizes the Spark framework for handling HTTP requests and defines methods for managing CSV data and loading CSV files.
2. **BroadbandRouteUtility Package:**
   - **Usage:** Contains classes related to handling broadband data routes.
   - **Implementation:** Includes `ACSAPIBroadbandSource` implementing the `ACSAPISource` interface and `BroadbandHandler` as a Spark `Route` for handling requests related to broadband data.
3. **CSVRouteUtility Package:**
   - **Usage:** Contains classes for handling CSV data routes.
   - **Implementation:** Includes `LoadCSVHandler` for loading CSV data, `ViewCSVHandler` for viewing loaded CSV data, and `SearchCSVHandler` for searching and filtering CSV data.
4. **Maps Package:**
   - **Usage:** Contains classes for handling Mapbox API requests.
   - **Implementation:** Includes `MapBoxHandler` and `RedliningHandler` as Spark `Routes` for handling geocoding requests and redlining data requests, respectively.
5. **Exception Handling:**
   - **Usage:** Classes such as `DatasourceException` and `InvalidArgsException` handle exceptions related to data source issues and invalid arguments.
   - **Implementation:** Ensures proper handling of exceptional cases in the application.
This section outlines the specific data structures employed in the project and provides insights into their roles and implementations within the high-level architecture of both the frontend and backend components..
#### How to Run Tests
. To run the tests, make sure that you are CD into maps-gjacobs5-fpabari. You should have playwright installed
    but if you don't, in your terminal type: 
                        npx playwright install
    once that is done, go back to your terminal and run the following commands to run the tests:
                        npx playwright test
                            - runs the tests
                        npx playwright test --ui
                            - opens a UI that allows for you to watch and run your tests as if in a live browser
#### How to Run Program
. To run the program, make sure that you are CD into maps-gjacobs5-fpabaris. You should have npm installed
    but if you don't, in your terminal type:
                        npm install
    once that is done, go back to your terminal and run the following command to start the server:
                        npm start
    open the local server that was started in your browser and then you can use it!

    To switch between modes type "mode" into the REPL box.
        There are two modes - brief and verbose. Brief only shows the output while verbose
        shows a more extensive output, including the command that was inputted into the REPL.
        The frontend automatically begins in brief mode.
    To load a file, type the command "file_load [fileName]" into the REPL box.
    To view a file, type "view" into the REPL box.
        In order to view a file, a file must be loaded first.
    To search for something within a file, type "search [col] [value]" into the REPL box.
        Where column can be either the name of the header of the column or the index of the column.
        To do a hollistic search of the file, the user would end up inputting -1 into the column box,
        but that is more of a thing to take care of once we connect our backend to frontend.
        A file must be loaded in inorder to be able to search..
# SafeTravels, A Neighborhood Safety App

## Project Overview

Welcome to SafeTravels! This project allows users to view neighborhood safety data in over 65,000 cities around the world, also allowing for real-time reports of crime and other incidents to be publicly available. This is done through the use of three separate API's that communicate -- Google Maps, MapBox, and Amadeus' Safe Place; the links to the keys for these can all be found below.

## CSCI0320 Term Project, SafeTravels

This full-stack project is composed of a backend (Java) server combined with a frontend (TypeScript/React) client, which in coalition allow for full functionality of the SafeTravels application.

Repository Link: https://github.com/cs0320-f23/term-project-ssquali1-snarang4-fpabari-ykothari

## Team Members, Contributions

This project had four contributors: Sofyan Squali-Houssaini(ssquali1), Yash Kothari (ykothari7), Falak Pabari (fpabari) and Satvik Narang (snarang4)

##### Contribution Details

- Sofyan: complete functionality of reporting system, complete functionality of login interface/logic, display and interaction with neighborhood safety data, integration tests, front-end documentation.
- Yash: Worked extensively on back-end handler and api connection, as well as partially on login handler and adding error handling there, wrote unit tests for backend functionality, wrote integration test for frontend functionality
- Satvik: Worked on setting up endpoints and handlers for our API as well as the API client. Helped connect backend to frontend by fetching safety data. Worked on algorithm for calculating and passing in safety data radius for Amadeus API call. Worked on backend documentation.
- Falak: complete mapbox handlesearch functionality & MapBoxHandler integration in backend, fetched safety data from backend to frontend, integration frontend tests, mock data, ldocumentation and commenting.

# Project Details: Structure, Design, and Implementation

Within this section will be the summary, explanations, and justifications for the design and implementation of the React and Java files within this project.

### Backend (Server)

In the backend, the Server class contains the main() method, which starts Spark and runs the handlers: MapBoxHandler, LoginManager, and SafetyHandler.

The Safety takes in the start and end points for the route and call on an instance of the API client to make a call to first convert the addresses to geocodes using the Google Geocoding API and then getting the relevant safety data using the Amadeus Safe Place API.

The LoginManager handles both the login and register endpoints. It takes in a username and password and stores it or checks against a map that holds this data.

The MapBoxHandler takes in a place and a token to allow for the map in the frontend to be set up correctly. It is also responsible for geocoding.

### Frontend (Client)

Here, our main components are **Login.tsx** and **MapBox.tsx**.

##### Login.tsx

The Login.tsx file handles user authentication, providing a user interface for logging in and registering. It defines a functional component named Login, which uses React's state management (useState) to handle input fields for login and registration. The component includes form elements for entering a username and password for both login and registration, along with buttons for triggering corresponding actions.

Styling is applied through inline CSS, creating a visually appealing and responsive design. The component communicates with the parent components through onLogin and onRegister callback functions, allowing the parent components to manage user authentication.

Upon login failure, an error message is displayed to the user, enhancing the user experience. The component makes use of asynchronous functions (handleLogin and handleRegister) to perform login and registration actions.

##### MapBox.tsx

The MapBox.tsx file implements the main map functionality of the application. It utilizes the react-map-gl library to render an interactive map, allowing users to explore locations. The map displays safety-related information, including hazard markers and safety pins. Users can input starting and destination locations, and the application fetches safety data for the specified route.

The component includes functionality for handling map interactions such as clicks and movements. It also supports the reporting of hazards and displays hazard markers on the map. The design includes additional features such as a crosshair, route generation button, and dropdowns for accessing previously viewed routes and reporting hazards.

The component uses various state variables to manage map state, hazard markers, safety pins, and user inputs. Asynchronous functions handle the fetching of safety data based on user inputs. Additionally, the component provides information popups when hazards or safety pins are clicked on the map.

### Accessibility Limitations & Features

Here, we've implemented accessibility features, emphasizing the dropdowns and their keyboard interactions. By utilizing semantic HTML elements such as buttons and input fields, we've enhanced the overall document structure for improved screen reader compatibility. Specifically, the dropdowns for hazard reporting and accessing previously viewed routes are designed to facilitate efficient navigation and interaction for keyboard users. Ongoing testing with assistive technologies remains integral to our approach, ensuring a user-friendly experience and promptly addressing any potential issues that may arise during keyboard-based interactions.

## Errors & Bugs

In the current version, there are no known errors or bugs present in SafeTravels.

# Test Suite Summary

## Backend Tests

## TestLoginManager

This test suite focuses on the functionalities of the LoginManager class in the server. It employs the Mockito framework to simulate requests and verify the responses of the login and registration processes. Key test cases include:

Successful login with valid credentials.
Handling of incorrect login credentials.
Handling of registration processes, including the detection of existing usernames.
By executing these tests, developers can ensure the robustness and correctness of the authentication mechanisms provided by the LoginManager.

## APIClientTest

The APIClientTest suite evaluates the functionalities of the APIClient class responsible for fetching geocode coordinates and safety ratings. This suite uses mocked HTTP connections to simulate various scenarios, such as:

Retrieving valid coordinates for known addresses.
Handling exceptions for incorrect addresses.
Verifying the structure and content of the safety ratings data returned.
This suite ensures that the API client behaves as expected, correctly fetching and processing data from external sources, and handling potential errors gracefully.

## Client (Frontend) Tests

In our testing suite, we have implemented a set of Playwright tests to ensure the functionality and user interface of the web application. The initial tests focus on the presence and visibility of essential elements upon page load. We confirm the existence of a "Login" button and validate the rendering of both the login and register forms along with their respective input fields and buttons.

To simulate user interactions, we've created tests that enter valid login and registration credentials, clicking the corresponding buttons. We verify successful redirection after a login attempt and the appearance of a success message upon successful registration. Additionally, we cover scenarios where entering duplicate registration credentials results in an error message, ensuring proper feedback to users.

Furthermore, we test the visibility of the hazard dropdown upon clicking the "Report A Hazard" button after logging in. We also confirm the visibility of the "Report A Hazard" and "Access Previously Viewed Routes" buttons upon successful login, providing comprehensive coverage for key user interactions.

# How To Get Started

## User Guide

To utilize the project, first run the server in the backend. This can be done by running the Server class in the Server directory.
Then, navigate to the frontend directory, and run npm run dev. Then, navigate to localhost:5173. This should bring up the login screen of SafeTravels -- from there, make an account, and sign in!

### API Links

Amadeus Safe Place: https://developers.amadeus.com/self-service/category/covid-19-and-travel-safety/api-doc/safe-place
Google Maps, Geocoder API: https://developers.google.com/maps/documentation/geocoding/overview
MapBox API: https://www.mapbox.com/

## How To Run Tests

FRONT-END: Navigate to the front-end directory and run "npx playwright test".
BACK-END: Press run on the inidvidual test files

import React, { useEffect, useState, useRef } from "react";
import Map, {
  Layer,
  MapLayerMouseEvent,
  Source,
  ViewStateChangeEvent,
} from "react-map-gl";
import { geoLayer, locLayer } from "./overlays";
import { FeatureCollection } from "geojson";
import { ACCESS_TOKEN } from "../private/api";
import MapWidget from "./MapWidget";
import { fetchDataFromBackend } from "./fetch";
import { Feature, Point } from "geojson";

interface LatLong {
  lat: number;
  long: number;
}

function MapBox() {
  const ProvidenceLatLong: LatLong = { lat: 41.8268, long: -71.4025 };
  const initialZoom = 10;
  const mapRef = useRef<any>();
  let showSearch = false;

  const [popup, setPopup] = useState<React.ReactNode | null>(null);
  const [currentZoom, setCurrentZoom] = useState(initialZoom);

  const [showDropdown, setShowDropdown] = useState(false);
  const [showHazardDropdown, setShowHazardDropdown] = useState(false);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const toggleHazardDropdown = () => {
    setShowHazardDropdown(!showHazardDropdown);
  };

  const [startLocation, setStartLocation] = useState<string>("");
  const [finalDestination, setFinalDestination] = useState<string>("");
  const [hazard, setHazard] = useState<string>("");

  // New function to handle user input for starting location and final destination
  const handleInput = (inputType: "start" | "destination", value: string) => {
    if (inputType === "start") {
      setStartLocation(value);
    } else {
      setFinalDestination(value);
    }
  };

  function onMapClick(e: MapLayerMouseEvent) {
    curLocation(e.lngLat.lat, e.lngLat.lng, true);
  }

  function curLocation(
    latitude: number,
    longitude: number,
    moveToLocation: boolean
  ) {
    const geocodingUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${longitude},${latitude}.json?access_token=${ACCESS_TOKEN}`;

    fetch(geocodingUrl)
      .then((response) => response.json())
      .then((data) => {
        if (data.features && data.features.length > 0) {
          const place = data.features[3];
          const county = place.text;
          const state = place.context.find(
            (context: { id: string | string[] }) =>
              context.id.includes("region")
          ).text;

          const popupContent = (
            <div>
              <h3>
                {latitude}, {longitude}
              </h3>
              <h3>
                {county}, {state}
              </h3>
            </div>
          );

          setPopup(popupContent);
          if (moveToLocation) {
            // handleSearch(county + "," + state);
          }
        }
      })
      .catch((error) => {
        console.error("Error with reverse geocoding:", error);
      });
  }

  const [viewState, setViewState] = useState({
    longitude: ProvidenceLatLong.long,
    latitude: ProvidenceLatLong.lat,
    zoom: initialZoom,
  });

  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );
  const [hazardMarkers, setHazardMarkers] = useState<FeatureCollection>({
    type: "FeatureCollection",
    features: [],
  });

  const submitHazard = () => {
    if (hazard.trim() !== "") {
      const mapCenter = mapRef.current.getMap().getCenter();

      const newHazardMarker: Feature<Point, { title: string }> = {
        type: "Feature",
        geometry: {
          type: "Point",
          coordinates: [mapCenter.lng, mapCenter.lat],
        },
        properties: {
          title: hazard,
        },
      };

      setHazardMarkers((prevMarkers) => ({
        type: "FeatureCollection",
        features: [...prevMarkers.features, newHazardMarker],
      }));

      // TODO: Send the hazard data to your backend for storage or further processing

      // Clear the hazard input field
      setHazard("");

      // Hide the hazard dropdown
      setShowHazardDropdown(false);
      console.log(hazardMarkers);
    }
  };

  const [searchOverlay, setSearchOverlay] = useState<
    GeoJSON.FeatureCollection | undefined
  >(undefined);

  const [manualGeocodeClicked, setManualGeocodeClicked] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedOptions, setSelectedOptions] = useState<string[]>([]);
  const [minLat, setMinLat] = useState(-90);
  const [maxLat, setMaxLat] = useState(90);
  const [minLon, setMinLon] = useState(-180);
  const [maxLon, setMaxLon] = useState(180);

  const [county, setCounty] = useState("");
  const [state, setState] = useState("");
  const [broadbandAccess, setBroadbandAccess] = useState(0);
  const [medianHousehold, setMedianHousehold] = useState(0);
  const [medianFamily, setMedianFamily] = useState(0);
  const [perCapita, setPerCapita] = useState(0);

  const [foundRecordsCount, setFoundRecordsCount] = useState(0);

  const updateFoundRecordsCount = (count: React.SetStateAction<number>) => {
    setFoundRecordsCount(count);
  };

  useEffect(() => {
    const fetchData = async () => {
      const apiUrl = `http://localhost:3232/redlining?minLat=${minLat}&maxLat=${maxLat}&minLon=${minLon}&maxLon=${maxLon}`;
      try {
        const response = await fetch(apiUrl);
        if (response.ok) {
          const data = await response.json();
          if (data.type === "FeatureCollection") {
            setOverlay(data);
            updateFoundRecordsCount(data.features.length);
          }
        }
      } catch (error) {
        console.error("Error fetching redlining data:", error);
      }
    };
    fetchData();
  }, [minLat, maxLat, minLon, maxLon]);

  // useEffect(() => {
  //   // Implement reverse geocoding logic here based on viewState.latitude and viewState.longitude
  //   // Set the county and state using setCounty and setState
  //   if (manualGeocodeClicked) {
  //     const geocodeCoordinates = async () => {
  //       try {
  //         const geocodingUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${viewState.longitude},${viewState.latitude}.json?access_token=${ACCESS_TOKEN}`;
  //         const response = await fetch(geocodingUrl);
  //         let data = await response.json();
  //         setMedianHousehold(0);
  //         setMedianFamily(0);
  //         setPerCapita(0);
  //         setBroadbandAccess(0);
  //         if (data.features && data.features.length > 0) {
  //           const place = data.features[3];
  //           const county = place.text;
  //           const state = place.context.find(
  //             (context: { id: string | string[] }) =>
  //               context.id.includes("region")
  //           ).text;
  //           setCounty(county);
  //           setState(state);
  //           curLocation(viewState.latitude, viewState.longitude, false);
  //           if (county.includes("County")) {
  //             await broadband([state, county.replace(" County", "")]);
  //           } else {
  //             setBroadbandAccess(0);
  //           }

  //           await load_file(["income/ri_income.csv", "true"]);
  //           const response = await search(["City/Town", county]);
  //           let result;
  //           try {
  //             result = JSON.stringify(response);
  //             data = JSON.parse(result);
  //           } catch (error) {
  //             data = null;
  //           }
  //           const medianHouseholdString = data.data[0][1];
  //           const medianHousehold = parseFloat(
  //             medianHouseholdString.replace(/"/g, "")
  //           );
  //           const medianFamilyString = data.data[0][2];
  //           const medianFamily = parseFloat(
  //             medianFamilyString.replace(/"/g, "")
  //           );
  //           const perCapitaString = data.data[0][3];
  //           const perCapita = parseFloat(perCapitaString.replace(/"/g, ""));
  //           setMedianHousehold(medianHousehold);
  //           setMedianFamily(medianFamily);
  //           setPerCapita(perCapita);
  //         }
  //       } catch (error) {
  //         console.error("Error with reverse geocoding:", error);
  //       }
  //     };
  //     geocodeCoordinates();
  //     setManualGeocodeClicked(false);
  //   }
  // }, [manualGeocodeClicked]);

  const handleSearch = async (searchTerm: string | number | boolean) => {
    showSearch = true;
    try {
      const apiUrl = `http://localhost:3232/mapbox?place=${encodeURIComponent(
        searchTerm
      )}&accessToken=${ACCESS_TOKEN}`;
      const response = await fetch(apiUrl);
      if (response.ok) {
        const data = await response.json();
        if (data.type === "FeatureCollection") {
          console.log(data);
          setSearchOverlay(data);
        }
        if (data.features.length > 0) {
          const firstFeature = data.features[0];
          const coordinates = firstFeature.center;

          curLocation(coordinates[1], coordinates[0], false);

          // Use mapRef.current.flyTo to access the map instance
          mapRef.current.flyTo({
            center: coordinates,
            zoom: 12,
          });
        } else {
          console.log("Location not found.");
        }
      }
    } catch (error) {
      console.error("Error:", error);
    }
  };

  // const handleToggle = (option: string) => {
  //   if (selectedOptions.includes(option)) {
  //     setSelectedOptions(selectedOptions.filter((item) => item !== option));
  //   } else {
  //     setSelectedOptions([...selectedOptions, option]);
  //   }
  // };

  // async function load_file(args: string[]): Promise<string> {
  //   return new Promise<string>(async (resolve, reject) => {
  //     // Case where an incorrect number of parameters is inputted while loading a CSV file
  //     if (args.length !== 2) {
  //       resolve("Invalid number of arguments.");
  //       return;
  //     }

  //     const filePath = args[0];
  //     const hasHeader = args[1];
  //     // Case where inputted file IS a CSV file
  //     if (filePath.endsWith(".csv")) {
  //       const url = "loadcsv?filePath=" + filePath + "&&hasHeader=" + hasHeader;
  //       try {
  //         const data = await fetchDataFromBackend(url);
  //         const dataJSON = JSON.stringify(data);
  //         const response = JSON.parse(dataJSON);
  //         if (response.error_message != undefined) {
  //           resolve(JSON.parse(dataJSON).error_message);
  //         }
  //         resolve("Successfully loaded file " + response.filepath);
  //       } catch (error) {
  //         console.error("Error parsing JSON: ", error);
  //         resolve("Error parsing JSON: " + error);
  //       }
  //     } else {
  //       // Case where inputted file is NOT a CSV file
  //       resolve("Invalid file type.");
  //     }
  //   });
  // }

  // async function search(args: string[]): Promise<string> {
  //   return new Promise<string>(async (resolve, reject) => {
  //     // case where incorrect number of parameters passed in
  //     if (args.length !== 2) {
  //       resolve("Invalid number of arguments.");
  //     }

  //     const value = args[1];
  //     const column = args[0];
  //     const url = "searchcsv?query=" + value + "&&columnToSearch=" + column;
  //     try {
  //       const data = await fetchDataFromBackend(url);
  //       const dataJSON = JSON.stringify(data);
  //       const response = JSON.parse(dataJSON);
  //       if (response.error_message != undefined) {
  //         resolve(JSON.parse(dataJSON).error_message);
  //       }
  //       resolve(response);
  //     } catch (error) {
  //       console.error("Error parsing JSON:", error);
  //       resolve("Error parsing JSON: " + error);
  //     }
  //   });
  // }

  // async function broadband(args: string[]): Promise<string> {
  //   return new Promise<string>(async (resolve, reject) => {
  //     // case where incorrect number of parameters passed in
  //     if (args.length !== 2) {
  //       resolve("Invalid number of arguments.");
  //     }

  //     const state = args[0];
  //     const county = args[1];
  //     const url = "broadband?state=" + state + "&&county=" + county;
  //     try {
  //       const data = await fetchDataFromBackend(url);
  //       const dataJSON = JSON.stringify(data);
  //       const response = JSON.parse(dataJSON);
  //       if (response.error_message != undefined) {
  //         resolve(JSON.parse(dataJSON).error_message);
  //       }
  //       const broadbandData = response.data;
  //       if (broadbandData.length >= 2) {
  //         const headerRow = broadbandData[0];
  //         const dataRow = broadbandData[1];
  //         const columnIndex = headerRow.findIndex(
  //           (column: string) => column === '"S2802_C03_022E"'
  //         );
  //         if (columnIndex !== -1) {
  //           const broadbandAccessValue = dataRow[columnIndex];

  //           // Remove the double quotes and parse the value
  //           const broadbandAccess = parseFloat(
  //             broadbandAccessValue.replace(/"/g, "")
  //           );

  //           if (!isNaN(broadbandAccess)) {
  //             // Set broadbandAccess in your state
  //             setBroadbandAccess(broadbandAccess);
  //           } else {
  //             // Handle the case when the value is not a valid number
  //             console.error(
  //               "Invalid broadband access value:",
  //               broadbandAccessValue
  //             );
  //           }
  //         }
  //       }
  //       resolve(response);
  //     } catch (error) {
  //       console.error("Error parsing JSON:", error);
  //       resolve("Error parsing JSON: " + error);
  //     }
  //   });
  // }

  function generateSafestRoute() {
    handleSearch(startLocation);
    // TODO: Implement logic to generate the safest route
    // You can use the startLocation and finalDestination states to get user input
    // Implement the route generation and update the map accordingly
    console.log("Generating Safest Route...");
  }

  function onMapMove(args: ViewStateChangeEvent) {
    setViewState(args.viewState);
    setCurrentZoom(args.viewState.zoom);
  }

  return (
    <div style={{ position: "relative" }}>
      <Map
        mapboxAccessToken={ACCESS_TOKEN}
        {...viewState}
        onMove={(ev) => onMapMove(ev)}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/streets-v12"}
        onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
        ref={mapRef}
      >
        {selectedOptions.includes("Redlining") && (
          <Source id="geo_data" type="geojson" data={overlay}>
            <Layer {...geoLayer} />
          </Source>
        )}
        {showSearch && (
          <Source id="loc_data" type="geojson" data={searchOverlay}>
            <Layer {...locLayer} />
          </Source>
        )}
        <Source id="hazard_markers" type="geojson" data={hazardMarkers}>
          <Layer
            type="symbol"
            id="hazard_markers_layer"
            layout={{
              "icon-image": "marker",
              "icon-size": currentZoom >= 12 ? 8 : 0.8,
            }}
          />
        </Source>

        {popup && (
          <div
            className="map-popup"
            style={{
              position: "absolute",
              top: "20px",
              right: "50px",
              zIndex: 1,
              pointerEvents: "none",
              color: "black",
            }}
          >
            {popup}
          </div>
        )}
        <div className="mapboxgl-marker" />
      </Map>
      <div
        className="crosshair"
        style={{
          color: "black",
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
        }}
      >
        <div className="vertical-line"></div>
        <div className="horizontal-line"></div>
        <div className="vertical-line"></div>
      </div>

      <div
        style={{
          position: "absolute",
          top: "150px",
          left: "1200px",
          zIndex: 1,
          color: "white",
          backgroundColor: "rgba(255, 255, 255, 0.9)",
          padding: "10px",
          borderRadius: "5px",
          boxShadow: "0 0 5px rgba(0, 0, 0, 0.3)",
        }}
      >
        <label
          style={{
            display: "block",
            marginBottom: "5px",
            color: "black",
            fontWeight: "bold",
          }}
        >
          Welcome to SafeTravels!
        </label>
        <label
          style={{ display: "block", marginBottom: "5px", color: "black" }}
        >
          Starting Location:
        </label>
        <input
          type="text"
          value={startLocation}
          onChange={(e) => handleInput("start", e.target.value)}
          style={{
            width: "100%",
            marginBottom: "10px",
            padding: "8px",
            boxSizing: "border-box",
            borderRadius: "3px",
            border: "1px solid #ccc",
          }}
          placeholder="Enter your starting address..."
        />

        <label
          style={{ display: "block", marginBottom: "5px", color: "black" }}
        >
          Final Destination:
        </label>
        <input
          type="text"
          value={finalDestination}
          onChange={(e) => handleInput("destination", e.target.value)}
          style={{
            width: "100%",
            marginBottom: "10px",
            padding: "8px",
            boxSizing: "border-box",
            borderRadius: "3px",
            border: "1px solid #ccc",
          }}
          placeholder="Enter the address of your destination..."
        />

        <button
          onClick={generateSafestRoute}
          id={"generateSafest"}
          style={{
            backgroundColor: "#007BFF",
            color: "black",
            padding: "8px 12px",
            borderRadius: "3px",
            border: "none",
            cursor: "pointer",
          }}
        >
          Generate Safest Route
        </button>
        <p></p>
        <button
          onClick={toggleHazardDropdown}
          id={"reportHazard"}
          style={{
            backgroundColor: "red",
            color: "black",
            padding: "8px 12px",
            borderRadius: "3px",
            border: "none",
            cursor: "pointer",
          }}
        >
          Report A Hazard
        </button>
        {showHazardDropdown && (
          <div
            id={"hazardDropdown"}
            style={{
              position: "absolute",
              top: "100%", // Adjust the top position to place it below the button
              left: 0,
              backgroundColor: "rgba(255, 255, 255, 0.9)",
              padding: "10px",
              borderRadius: "5px",
              boxShadow: "0 0 5px rgba(0, 0, 0, 0.3)",
              zIndex: 2,
            }}
          >
            <input
              type="text"
              value={hazard}
              onChange={(e) => setHazard(e.target.value)}
              style={{
                width: "100%",
                marginBottom: "10px",
                padding: "8px",
                boxSizing: "border-box",
                borderRadius: "3px",
                border: "1px solid #ccc",
              }}
              placeholder="What was the issue?"
            />
            <button
              onClick={submitHazard}
              style={{
                backgroundColor: "#007BFF",
                color: "black",
                padding: "8px 12px",
                borderRadius: "3px",
                border: "none",
                cursor: "pointer",
              }}
            >
              Submit
            </button>
          </div>
        )}
        <p></p>
        <button
          style={{
            backgroundColor: "#007BFF",
            color: "black",
            padding: "8px 12px",
            borderRadius: "3px",
            border: "none",
            cursor: "pointer",
          }}
          id={"accessPreviously"}
          onClick={toggleDropdown}
        >
          Access Previously Viewed Routes
        </button>
        {showDropdown && (
          <div
            id={"previouslyViewedRoutesDropdown"}
            style={{
              position: "absolute",
              top: "100%", // Adjust the top position to place it below the button
              left: 0,
              backgroundColor: "rgba(255, 255, 255, 0.9)",
              padding: "10px",
              borderRadius: "5px",

              boxShadow: "0 0 5px rgba(0, 0, 0, 0.3)",
              zIndex: 2,
            }}
          >
            {/* Dropdown content goes here */}
            <p>Route 1</p>
            <p>Route 2</p>
            <p>Route 3</p>
          </div>
        )}
      </div>

      <div
        style={{
          position: "absolute",
          top: "20px",
          left: "20px", // Adjust the left position to create space between the input fields and the MapWidget
          backgroundColor: "white",
          padding: "10px",
          boxShadow: "0 0 5px rgba(0, 0, 0, 0.3)",
          borderRadius: "5px",
        }}
      >
        {/* <MapWidget
          onSearch={handleSearch}
          onToggle={handleToggle}
          selectedOptions={selectedOptions}
          onFilterChange={(minLat, maxLat, minLon, maxLon) => {
            setMinLat(minLat);
            setMaxLat(maxLat);
            setMinLon(minLon);
            setMaxLon(maxLon);
          }}
          foundRecordsCount={foundRecordsCount}
          latitude={viewState.latitude}
          longitude={viewState.longitude}
          county={county}
          state={state}
          broadbandAccess={broadbandAccess}
          medianHousehold={medianHousehold}
          medianFamily={medianFamily}
          perCapita={perCapita}
          onManualGeocodeClick={() => setManualGeocodeClicked(true)}
        /> */}
      </div>
    </div>
  );
}

export default MapBox;

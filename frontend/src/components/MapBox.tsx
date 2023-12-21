import React, { useEffect, useState, useRef } from "react";
import { Marker, Popup } from "react-map-gl";
import Map, {
  Layer,
  MapLayerMouseEvent,
  Source,
  ViewStateChangeEvent,
} from "react-map-gl";
import { geoLayer, locLayer } from "./overlays";
import { FeatureCollection } from "geojson";
import { ACCESS_TOKEN } from "../private/api";
import { Feature, Point } from "geojson";

interface LatLong {
  lat: number;
  long: number;
}

interface SafetyData {
  id: string;
  name: string;
  geoCode: {
    latitude: number;
    longitude: number;
  };
  safetyScores: {
    lgbtq: number;
    medical: number;
    overall: number;
    physicalHarm: number;
    politicalFreedom: number;
    theft: number;
    women: number;
  };
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
  const [clickedPin, setClickedPin] = useState<SafetyData | null>(null);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
    if (showHazardDropdown) {
      setShowHazardDropdown(!showHazardDropdown);
    }
  };

  const toggleHazardDropdown = () => {
    if (showDropdown) {
      setShowDropdown(!showDropdown);
    }
    setShowHazardDropdown(!showHazardDropdown);
  };

  const [startLocation, setStartLocation] = useState<string>("");
  const [finalDestination, setFinalDestination] = useState<string>("");
  const [hazard, setHazard] = useState<string>("");
  const [clickedHazard, setClickedHazard] = useState<{
    coordinates: number[];
    title: string;
  } | null>(null);

  // New function to handle user input for starting location and final destination
  const handleInput = (inputType: "start" | "destination", value: string) => {
    if (inputType === "start") {
      setStartLocation(value);
    } else {
      setFinalDestination(value);
    }
  };

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
                {longitude}, {latitude}
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
      setHazard("");

      setShowHazardDropdown(false);
    }
  };

  const [safetyPins, setSafetyPins] = useState<SafetyData[]>([]);

  const handleSearch = async (
    startTerm: string | number | boolean,
    endTerm: string | number | boolean
  ) => {
    showSearch = true;
    try {
      let startCoordinates, endCoordinates;

      // Fetch start coordinates
      const startUrl = `http://localhost:3232/mapbox?place=${encodeURIComponent(
        startTerm
      )}&accessToken=${ACCESS_TOKEN}`;
      const startResponse = await fetch(startUrl);
      if (startResponse.ok) {
        const startData = await startResponse.json();
        if (startData.features.length > 0) {
          startCoordinates = startData.features[0].center;
          curLocation(startCoordinates[1], startCoordinates[0], true);
          mapRef.current.flyTo({
            center: startCoordinates,
            zoom: 12,
          });
        }
      }

      // Fetch end coordinates
      const endUrl = `http://localhost:3232/mapbox?place=${encodeURIComponent(
        endTerm
      )}&accessToken=${ACCESS_TOKEN}`;
      const endResponse = await fetch(endUrl);
      if (endResponse.ok) {
        const endData = await endResponse.json();
        if (endData.features.length > 0) {
          endCoordinates = endData.features[0].center;
          curLocation(endCoordinates[1], endCoordinates[0], true);
          mapRef.current.flyTo({
            center: endCoordinates,
            zoom: 12,
          });
        }
      }

      // If both start and end coordinates are available
      if (startCoordinates && endCoordinates) {
        const bounds = [
          [
            Math.min(startCoordinates[0], endCoordinates[0]),
            Math.min(startCoordinates[1], endCoordinates[1]),
          ],
          [
            Math.max(startCoordinates[0], endCoordinates[0]),
            Math.max(startCoordinates[1], endCoordinates[1]),
          ],
        ];

        mapRef.current.fitBounds(bounds, {
          padding: { top: 50, bottom: 50, left: 50, right: 50 },
        });
      }
    } catch (error) {
      console.error("Error:", error);
    }
  };

  function onMapClick(e: MapLayerMouseEvent) {
    const roundToNearest = 0.01; // Set the desired rounding precision
    const clickedCoordinates = [
      Math.round(e.lngLat.lng / roundToNearest) * roundToNearest,
      Math.round(e.lngLat.lat / roundToNearest) * roundToNearest,
    ];
    console.log(e);

    // Check if the clicked coordinates match any hazard marker
    for (let i = 0; i < hazardMarkers.features.length; i++) {
      const hazardMarker = hazardMarkers.features[i];

      // Use type assertion to specify that the geometry is a Point
      const hazardCoordinates = (hazardMarker.geometry as any).coordinates;

      // Round the hazard coordinates to the nearest 0.001
      const roundedHazardCoordinates = [
        Math.round(hazardCoordinates[0] / roundToNearest) * roundToNearest,
        Math.round(hazardCoordinates[1] / roundToNearest) * roundToNearest,
      ];

      // Compare rounded coordinates and print text if there's a match
      console.log(roundedHazardCoordinates[0]);
      console.log(roundedHazardCoordinates[1]);
      console.log(clickedCoordinates[0]);
      console.log(clickedCoordinates[1]);

      if (
        roundedHazardCoordinates[0] === clickedCoordinates[0] &&
        roundedHazardCoordinates[1] === clickedCoordinates[1]
      ) {
        const title = hazardMarker.properties?.title || "";
        const hazardInformation = {
          coordinates: roundedHazardCoordinates,
          title: title,
        };

        // Set the hazard information in the state
        setClickedHazard(hazardInformation);
        setPopup(null); // Clear any existing popup when a hazard is clicked
        return; // Exit the loop after finding a match
      }
    }
    for (let i = 0; i < safetyPins.length; i++) {
      const pin = safetyPins[i];
      const pinCoordinates = pin.geoCode;

      // Round the safety pin coordinates to the nearest 0.001
      const roundedPinCoordinates = [
        Math.round(pinCoordinates.longitude / roundToNearest) * roundToNearest,
        Math.round(pinCoordinates.latitude / roundToNearest) * roundToNearest,
      ];

      // Compare the rounded coordinates
      if (
        roundedPinCoordinates[0] === clickedCoordinates[0] &&
        roundedPinCoordinates[1] === clickedCoordinates[1]
      ) {
        console.log("Safety Pin Clicked:", pin);
        // Set the safety pin information in the state
        setClickedHazard(null); // Clear any existing hazard information
        setClickedPin(pin);
        return; // Exit the loop after finding a match
      }
    }

    // If no hazard marker or safety pin matches, proceed with other logic
    curLocation(e.lngLat.lat, e.lngLat.lng, true);
    setClickedHazard(null);
    setClickedPin(null);
  }

  function generateSafestRoute() {
    handleSearch(startLocation, finalDestination);
    setStartLocation("");
    setFinalDestination("");
    fetchSafetyData();
  }

  function onMapMove(args: ViewStateChangeEvent) {
    setViewState(args.viewState);
    setCurrentZoom(args.viewState.zoom);
  }

  const [safetyData, setSafetyData] = useState<SafetyData[]>([]);

  // Fetch safety data from the backend API
  const fetchSafetyData = async () => {
    const encodedStartLocation = encodeURIComponent(startLocation);
    const encodedFinalDestination = encodeURIComponent(finalDestination);

    const safetyUrl = `http://localhost:3232/safestroute?start=${encodedStartLocation}&end=${encodedFinalDestination}`;
    console.log("URL:" + safetyUrl);
    try {
      const response = await fetch(safetyUrl);
      console.log("haaaa");
      if (response.ok) {
        const data = await response.json();
        console.log("data shape 1:");
        console.log(data.data);
        console.log(data);
        if (data.data) {
          console.log("data shape 2:");
          console.log(data.data);
          setSafetyData(data.data.data);
          setSafetyPins(data.data.data);
          console.log("safetypins:" + safetyPins);
        }
      }
    } catch (error) {
      console.error("Error fetching safety data:", error);
    }
  };

  return (
    <div style={{ position: "relative" }}>
      <Map
        mapboxAccessToken={ACCESS_TOKEN}
        {...viewState}
        onMove={(ev) => onMapMove(ev)}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/bright-v9"}
        onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
        ref={mapRef}
      >
        <Source id="hazard_markers" type="geojson" data={hazardMarkers}>
          <Layer
            type="symbol"
            id="hazard_markers_layer"
            layout={{
              "icon-image": "marker-11",
              "icon-size": {
                stops: [
                  [12, 1],
                  [16, 8],
                ], // Adjust size based on zoom level
              },
            }}
          />
        </Source>
        <Source
          id="safety_pins"
          type="geojson"
          data={{
            type: "FeatureCollection",
            features: safetyData.map((neighborhood) => ({
              type: "Feature",
              geometry: {
                type: "Point",
                coordinates: [
                  neighborhood.geoCode.longitude,
                  neighborhood.geoCode.latitude,
                ],
              },
              properties: {
                title: neighborhood.name,
                description: neighborhood.name,
                subType: neighborhood.safetyScores.overall.toString(),
              },
            })),
          }}
        >
          <Layer
            type="symbol"
            id="safety_pins_layer"
            layout={{
              "icon-image": "information-11", // Add your safety pin icon image
              "icon-size": {
                stops: [
                  [12, 1],
                  [16, 12],
                ], // Adjust size based on zoom level
              },
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
        {clickedHazard && (
          <div
            className="map-popup"
            style={{
              position: "absolute",
              top: "250px",
              left: "750px",
              zIndex: 1,
              pointerEvents: "none",
              backgroundColor: "white",
              opacity: 0.6,
              color: "red",
            }}
          >
            <h3>{"Hazard Message:"}</h3>
            <h3>{clickedHazard.title}</h3>
          </div>
        )}
        {clickedPin && (
          <div
            className="map-popup"
            style={{
              position: "absolute",
              top: "250px",
              left: "750px",
              zIndex: 1,
              pointerEvents: "none",
              backgroundColor: "white",
              opacity: 0.6,
              color: "blue", // Change the color as needed
            }}
          >
            <h3>{"Safety Pin Information:"}</h3>
            <h3>{clickedPin.name}</h3>
            <p>LGBTQ Score: {clickedPin.safetyScores.lgbtq}</p>
            <p>Medical Score: {clickedPin.safetyScores.medical}</p>
            <p>Overall Score: {clickedPin.safetyScores.overall}</p>
            <p>Physical Harm Score: {clickedPin.safetyScores.physicalHarm}</p>
            <p>
              Political Freedom Score:{" "}
              {clickedPin.safetyScores.politicalFreedom}
            </p>
            <p>Theft Score: {clickedPin.safetyScores.theft}</p>
            <p>Women's Safety Score: {clickedPin.safetyScores.women}</p>
          </div>
        )}
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
          Generate Neighborhood Information
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
              placeholder="What was the crime? Theft, violence.."
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
    </div>
  );
}

export default MapBox;

import React, { useEffect, useState, useRef } from "react";
import { Popup } from "react-map-gl";
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

      // are we going to use backend here?
      // Clear the hazard input field
      setHazard("");

      // Hide the hazard dropdown
      setShowHazardDropdown(false);
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

  // const handleToggle = (option: string) => {
  //   if (selectedOptions.includes(option)) {
  //     setSelectedOptions(selectedOptions.filter((item) => item !== option));
  //   } else {
  //     setSelectedOptions([...selectedOptions, option]);
  //   }
  // };

  function onMapClick(e: MapLayerMouseEvent) {
    const roundToNearest = 0.001; // Set the desired rounding precision
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
        return; // Exit the loop after finding a match
      }
    }

    // If no hazard marker matches, proceed with other logic
    curLocation(e.lngLat.lat, e.lngLat.lng, true);
    setClickedHazard(null);
  }

  function generateSafestRoute() {
    handleSearch(startLocation, finalDestination);
    setStartLocation("");
    setFinalDestination("");
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

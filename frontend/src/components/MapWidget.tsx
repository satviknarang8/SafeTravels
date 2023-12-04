import React, { useState, useEffect } from 'react';

interface MapWidgetProps {
  onSearch: (searchTerm: string) => void;
  onToggle: (option: string) => void;
  selectedOptions: string[];
  onFilterChange: (minLat: number, maxLat: number, minLon: number, maxLon: number) => void;
  foundRecordsCount: number;
  latitude: number;
  longitude: number;
  county: string;
  state: string;
  broadbandAccess: number;
  medianHousehold: number;
  medianFamily: number;
  perCapita: number;
  onManualGeocodeClick: () => void;
}

const MapWidget: React.FC<MapWidgetProps> = ({
  onSearch,
  onToggle,
  selectedOptions,
  onFilterChange,
  foundRecordsCount,
  latitude,
  longitude,
  county,
  state,
  broadbandAccess,
  medianHousehold,
  medianFamily,
  perCapita,
  onManualGeocodeClick
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [minLat, setMinLat] = useState(-90);
  const [maxLat, setMaxLat] = useState(90);
  const [minLon, setMinLon] = useState(-180);
  const [maxLon, setMaxLon] = useState(180);

  const handleSearch = () => {
    onSearch(searchTerm);
    setSearchTerm('');
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  useEffect(() => {
    onFilterChange(minLat, maxLat, minLon, maxLon);
  }, [minLat, maxLat, minLon, maxLon]);

  return (
    <div className="map-widget">
      <pre>Enter a place to search for</pre>
      <p></p>
      <input
        type="text"
        placeholder="Search..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        onKeyDown={handleKeyPress}
      />
      <button onClick={handleSearch}>Search</button>
      <br />
      <br />
      <hr></hr>
      <p></p>
      <button onClick={onManualGeocodeClick}>Load geo data for current location</button>
      <p></p>
      <hr></hr>
      <div>
        <p></p>
        <b><pre>Income Data</pre></b>
        <pre>Median Household: <b>${medianHousehold},000</b></pre>
        <pre>Median Family: <b>${medianFamily},000</b></pre>
        <pre>Per Capita: <b>${perCapita},000</b></pre>
      </div>
      <p></p>
      <hr></hr>
      <div>
        <p></p>
        <b><pre>Broadband Access</pre></b>
        <pre><b>{broadbandAccess}%</b> of households</pre>
        <p></p>
      </div>
      <hr></hr>
      <div>
        <p></p>
        <b><pre>Redlining Data</pre></b>
      </div>
      <div className="toggle-buttons">
        <p></p>
        <button
          onClick={() => onToggle('Redlining')}
          className={selectedOptions.includes('Redlining') ? 'toggled' : ''}
        >
          Show Redlining Overlay
        </button>
      </div>
      <p></p>
      <div className="filter-sliders">
        <div className="slider-group">
            <label>Min lat:</label>
            <input
            type="range"
            min={-90}
            max={90}
            value={minLat}
            onChange={(e) => setMinLat(Number(e.target.value))}
            />
            <input
            type="number"
            min={-90}
            max={90}
            step="0.0001"
            value={minLat}
            onChange={(e) => setMinLat(Number(e.target.value))}
            />
        </div>
        <div className="slider-group">
            <label>Max lat:</label>
            <input
            type="range"
            min={-90}
            max={90}
            value={maxLat}
            onChange={(e) => setMaxLat(Number(e.target.value))}
            />
            <input
            type="number"
            min={-90}
            max={90}
            step="0.0001"
            value={maxLat}
            onChange={(e) => setMaxLat(Number(e.target.value))}
            />
        </div>
        <div className="slider-group">
            <label>Min lng:</label>
            <input
            type="range"
            min={-180}
            max={180}
            value={minLon}
            onChange={(e) => setMinLon(Number(e.target.value))}
            />
            <input
            type="number"
            min={-180}
            max={180}
            step="0.0001"
            value={minLon}
            onChange={(e) => setMinLon(Number(e.target.value))}
            />
        </div>
        <div className="slider-group">
            <label>Max lng:</label>
            <input
            type="range"
            min={-180}
            max={180}
            value={maxLon}
            onChange={(e) => setMaxLon(Number(e.target.value))}
            />
            <input
            type="number"
            min={-180}
            max={180}
            step="0.0001"
            value={maxLon}
            onChange={(e) => setMaxLon(Number(e.target.value))}
            />
        </div>
      </div>
      <div>
        <pre>Found <b>{foundRecordsCount}</b> records.</pre>
      </div>
    </div>
  );
};

export default MapWidget;

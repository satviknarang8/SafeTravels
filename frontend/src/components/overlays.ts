import { FeatureCollection } from 'geojson';
import { FillLayer } from 'react-map-gl';

const propertyName = 'holc_grade';

export const geoLayer: FillLayer = {
  id: 'geo_data',
  type: 'fill',
  paint: {
    'fill-color': [
      'match',
      ['get', propertyName],
      'A',
      '#5bcc04',
      'B',
      '#04b8cc',
      'C',
      '#e9ed0e',
      'D',
      '#d11d1d',
      '#ccc',
    ],
    'fill-opacity': 0.2,
  },
};

export const locLayer: FillLayer = {
  id: 'loc_data',
  type: 'fill',
  paint: {
    'fill-color': '#5bcc04',
    'fill-opacity': 0.2,
  }
}

export async function fetchRedliningData(minLat: number, maxLat: number, minLon: number, maxLon: number): Promise<FeatureCollection | undefined> {
  const apiUrl = `http://localhost:3232/redlining?minLat=${minLat}&maxLat=${maxLat}&minLon=${minLon}&maxLon=${maxLon}`;

  try {
    const response = await fetch(apiUrl);
    if (response.ok) {
      const data = await response.json();
      if (isFeatureCollection(data)) {
        return data;
      }
    }
  } catch (error) {
    console.error('Error fetching redlining data:', error);
  }

  return undefined;
}

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === 'FeatureCollection';
}

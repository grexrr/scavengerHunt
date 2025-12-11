import { useMemo } from 'react';
import { Polygon } from 'react-native-maps';
import { LandmarkDTO } from '../types';

type LandmarkProps = {
  landmark: LandmarkDTO;
  isSolved?: boolean;
};

export default function LandmarkPolygon({ landmark, isSolved = false }: LandmarkProps) {
  const coordinates = useMemo(() => {
    return landmark.coordinates
      .filter(coord => Array.isArray(coord) && coord.length >= 2)
      .map(coord => {
        const [lat, lng] = coord;
        return {
          latitude: lat,
          longitude: lng,
        };
      });
  }, [landmark.coordinates]);

  if (coordinates.length < 3) return null;

  const fillColor = isSolved
    ? 'rgba(0, 255, 0, 0.3)' // 绿色：已解决
    : 'rgba(255, 0, 0, 0.3)'; // 红色：未解决

  return <Polygon coordinates={coordinates} fillColor={fillColor} />;
}

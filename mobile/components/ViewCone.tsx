import { useMemo } from 'react';
import { Polygon } from 'react-native-maps';

type ViewConeProps = {
  center: { latitude: number; longitude: number };
  headingDeg: number;        
  spanDeg: number;          
  radiusMeters: number;      
  resolution?: number;       
  fillColor?: string;
};

const METERS_PER_DEG_LAT = 111_320;

export default function ViewCone({
  center,
  headingDeg,
  spanDeg,
  radiusMeters,
  resolution = 50,
  fillColor = 'rgba(0, 122, 255, 0.20)',
}: ViewConeProps) {
  const coordinates = useMemo(() => {
    if (!center) return [];

    const points = [];
    points.push({ latitude: center.latitude, longitude: center.longitude });

    const step = spanDeg / resolution;
    const start = headingDeg - spanDeg / 2;

    for (let i = 0; i <= resolution; i++) {
      const angle = start + i * step;
      const theta = (angle * Math.PI) / 180;

      const dLat = (radiusMeters * Math.cos(theta)) / METERS_PER_DEG_LAT; 
      const metersPerDegLng =
        METERS_PER_DEG_LAT * Math.cos((center.latitude * Math.PI) / 180);
      const dLng = (radiusMeters * Math.sin(theta)) / metersPerDegLng;

      points.push({
        latitude: center.latitude + dLat,
        longitude: center.longitude + dLng,
      });
    }

    points.push(points[0]); // close
    return points;
  }, [center, headingDeg, spanDeg, radiusMeters, resolution]);

  if (coordinates.length < 3) return null;

  return (
    <Polygon
      coordinates={coordinates}
      fillColor={fillColor}
      strokeColor="rgba(0,0,0,0)" 
      strokeWidth={0}
    />
  );
}
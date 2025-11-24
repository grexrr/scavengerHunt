import { useMemo } from 'react';
import { Polygon } from 'react-native-maps';

interface Position {
  latitude: number;
  longitude: number;
}

type ViewConeProps = {
  center: { latitude: number; longitude: number };
  headingDeg: number;        // 玩家朝向
  spanDeg: number;           // 扇形总角度，例如 60°
  radiusMeters: number;      // 半径，例如 50 米
  resolution?: number;       // 计算精度，默认 50
  fillColor?: string;
  strokeColor?: string;
};

const METERS_PER_DEG_LAT = 111_320;

export default function ViewCone({
  center,
  headingDeg,
  spanDeg,
  radiusMeters,
  resolution = 50,
  fillColor = 'rgba(0, 122, 255, 0.20)',
  strokeColor = 'rgba(0, 122, 255, 0.35)',
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

    points.push(points[0]); // 闭合
    return points;
  }, [center, headingDeg, spanDeg, radiusMeters, resolution]);

  if (coordinates.length < 3) return null;

  return (
    <Polygon
      coordinates={coordinates}
      fillColor={fillColor}
      // strokeColor={strokeColor}
      // strokeWidth={1.5}
    />
  );
}
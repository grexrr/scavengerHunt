import * as Location from 'expo-location';
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  getCurrentHeading,
  getCurrentPosition,
  Heading,
  Position,
  watchHeading,
  watchPosition,
} from '../services/location/locationService';

export interface UseLocationReturn {
  location: Position | null;
  heading: Heading | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  isTracking: boolean;
  startTracking: () => void;
  stopTracking: () => void;
}

export function useLocation(autoTrack: boolean = false): UseLocationReturn {
  const [location, setLocation] = useState<Position | null>(null);
  const [heading, setHeading] = useState<Heading | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isTracking, setIsTracking] = useState(false);

  const unsubscribePositionRef = useRef<(() => void) | null>(null);
  const unsubscribeHeadingRef = useRef<(() => void) | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const [position, direction] = await Promise.all([getCurrentPosition(), getCurrentHeading()]);

      if (position && direction) {
        setLocation(position);
        setHeading(direction);
        setError(null);
      } else if (!position) {
        setError('Unable to get position, please check permission settings');
        setLocation(null);
      } else {
        setError('Unable to get heading, please check permission settings');
        setHeading(null);
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'An error occurred while obtaining the location';
      setError(errorMessage);
      setLocation(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const startTracking = useCallback(() => {
    if (isTracking) return;

    setIsTracking(true);
    setError(null);

    // 开始实时跟踪位置
    unsubscribePositionRef.current = watchPosition(
      position => {
        setLocation(position);
      },
      {
        accuracy: Location.Accuracy.High,
        timeInterval: 1000,
        distanceInterval: 0,
      }
    );

    // 开始实时跟踪朝向（使用 Magnetometer）
    unsubscribeHeadingRef.current = watchHeading(headingData => {
      setHeading(headingData);
    });
  }, [isTracking]);

  const stopTracking = useCallback(() => {
    if (unsubscribePositionRef.current) {
      unsubscribePositionRef.current();
      unsubscribePositionRef.current = null;
    }
    if (unsubscribeHeadingRef.current) {
      unsubscribeHeadingRef.current();
      unsubscribeHeadingRef.current = null;
    }
    setIsTracking(false);
  }, []);

  // 如果 autoTrack 为 true，自动开始跟踪
  useEffect(() => {
    if (autoTrack) {
      startTracking();
    }
    return () => {
      stopTracking();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoTrack]);

  return {
    location,
    heading,
    loading,
    error,
    refresh,
    isTracking,
    startTracking,
    stopTracking,
  };
}

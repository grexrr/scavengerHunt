import { useCallback, useState } from 'react';
import { getCurrentHeading, getCurrentPosition, Heading, Position } from '../services/location/locationService';

export interface UseLocationReturn {
  location: Position | null;      
  heading: Heading | null;
  loading: boolean;                
  error: string | null;           
  refresh: () => Promise<void>;   
}

export function useLocation(): UseLocationReturn {

  const [location, setLocation] = useState<Position | null>(null);
  const [heading, setHeading] = useState<Heading | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);  
    
    try {
      const [position, direction] = await Promise.all([
        getCurrentPosition(),
        getCurrentHeading(),
      ]);
      
      if (position && direction) {
        setLocation(position);
        setHeading(direction);
        setError(null);  // 成功时清除错误
      } else if (!position){
        setError('Unable to get position, please check permission settings');
        setLocation(null);  // Clear position on failure
      } else {
        setError('Unable to get heading, please check permission settings');
        setHeading(null);  // 失败时清除位置
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'An error occurred while obtaining the location';
      setError(errorMessage);
      setLocation(null);
    } finally {
      setLoading(false);
    }
  }, []); 

  return {
    location,
    heading,
    loading,
    error,
    refresh,
  };
}
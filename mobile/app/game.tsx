import MapView, { UrlTile } from 'react-native-maps';
import ViewCone from '../components/ViewCone';
import { useLocation } from '../hooks/useLocation';
import { mapStyles } from '../styles/mapStyles';


const VIEW_CONE_SPAN = 60;   
const VIEW_CONE_RADIUS = 50; 

export default function GamePage() {
  const { location, heading, loading, error, isTracking, startTracking, stopTracking } = useLocation(true);
  
  // const getDirectionText = (angle: number): string => {
  //   if (angle === 0 || angle === 360) return '正北';
  //   if (angle < 90) return '东北';
  //   if (angle === 90) return '正东';
  //   if (angle < 180) return '东南';
  //   if (angle === 180) return '正南';
  //   if (angle < 270) return '西南';
  //   if (angle === 270) return '正西';
  //   return '西北';
  // };

  return (
    <MapView
      style={mapStyles.map}
      mapType="none"             
      initialRegion={{
        latitude: 52.145765,
        longitude: -8.641198,
        latitudeDelta: 0.01,
        longitudeDelta: 0.01,
      }}
      showsUserLocation={true}
      followsUserLocation={true}
    >
      <UrlTile
        urlTemplate="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maximumZ={19}
      />

      {location && heading && (
        <ViewCone
          center={location}
          headingDeg={heading.heading}
          spanDeg={VIEW_CONE_SPAN}
          radiusMeters={VIEW_CONE_RADIUS}
          fillColor="rgba(0, 122, 255, 0.25)" // 随意调整
        />
      )}
    </MapView>
  );
}

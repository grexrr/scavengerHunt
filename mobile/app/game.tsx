import MapView, { UrlTile } from 'react-native-maps';
import ViewCone from '../components/ViewCone';
import { useLocation } from '../hooks/useLocation';
import { mapStyles } from '../styles/mapStyles';


const VIEW_CONE_SPAN = 60;   
const VIEW_CONE_RADIUS = 50; 

export default function GamePage() {
  const { location, heading, loading, error, isTracking, startTracking, stopTracking } = useLocation(true);

  // 将角度转换为方向文字
  const getDirectionText = (angle: number): string => {
    if (angle === 0 || angle === 360) return '正北';
    if (angle < 90) return '东北';
    if (angle === 90) return '正东';
    if (angle < 180) return '东南';
    if (angle === 180) return '正南';
    if (angle < 270) return '西南';
    if (angle === 270) return '正西';
    return '西北';
  };

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
    // <SafeAreaView style={{ flex: 1 }} edges={['top']}>
    //   <ScrollView contentContainerStyle={styles.container}>
    //     <Text style={styles.title}>实时定位和角度</Text>
        
    //     {/* 跟踪状态 */}
    //     <View style={styles.statusContainer}>
    //       <Text style={styles.statusText}>
    //         跟踪状态: {isTracking ? '✅ 实时跟踪中' : '⏸️ 已停止'}
    //       </Text>
    //       <View style={styles.buttonRow}>
    //         <Button
    //           title="开始跟踪"
    //           onPress={startTracking}
    //           disabled={isTracking || loading}
    //         />
    //         <View style={styles.buttonSpacer} />
    //         <Button
    //           title="停止跟踪"
    //           onPress={stopTracking}
    //           disabled={!isTracking}
    //         />
    //       </View>
    //     </View>

    //     {/* 错误信息 */}
    //     {error && (
    //       <View style={styles.errorContainer}>
    //         <Text style={styles.errorText}>{error}</Text>
    //       </View>
    //     )}

    //     {/* 位置信息 */}
    //     {location && (
    //       <View style={styles.infoContainer}>
    //         <Text style={styles.sectionTitle}>位置信息</Text>
    //         <Text style={styles.label}>纬度 (Latitude):</Text>
    //         <Text style={styles.value}>{location.latitude.toFixed(6)}</Text>
            
    //         <Text style={styles.label}>经度 (Longitude):</Text>
    //         <Text style={styles.value}>{location.longitude.toFixed(6)}</Text>
            
    //         {location.accuracy && (
    //           <>
    //             <Text style={styles.label}>精度 (米):</Text>
    //             <Text style={styles.value}>{location.accuracy.toFixed(2)}</Text>
    //           </>
    //         )}
    //       </View>
    //     )}

    //     {/* 朝向信息 - 实时更新 */}
    //     {heading ? (
    //       <View style={styles.infoContainer}>
    //         <Text style={styles.sectionTitle}>朝向信息（实时）</Text>
    //         <Text style={styles.label}>设备朝向:</Text>
    //         <Text style={styles.headingValue}>{heading.heading.toFixed(2)}°</Text>
    //         <Text style={styles.directionText}>
    //           {getDirectionText(heading.heading)}
    //         </Text>
    //         {heading.accuracy !== null && (
    //           <>
    //             <Text style={styles.label}>精度:</Text>
    //             <Text style={styles.value}>{heading.accuracy}°</Text>
    //           </>
    //         )}
    //       </View>
    //     ) : (
    //       <View style={styles.infoContainer}>
    //         <Text style={styles.sectionTitle}>朝向信息</Text>
    //         <Text style={styles.label}>获取中...</Text>
    //       </View>
    //     )}
    //   </ScrollView>
    // </SafeAreaView>
  );
}

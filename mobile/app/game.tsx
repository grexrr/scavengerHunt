import { Button, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocation } from '../hooks/useLocation';


export default function GamePage() {
  const { location, heading, loading, error, refresh } = useLocation();

  const handleGetLocation = async () => {
    await refresh();
  };
  
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
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>定位和角度测试</Text>
        
        <Button
          title={loading ? '获取中...' : '获取位置和角度'}
          onPress={handleGetLocation}
          disabled={loading}
        />
        
        {location && (
          <View style={styles.infoContainer}>
            <Text style={styles.sectionTitle}>位置信息</Text>
            <Text style={styles.label}>纬度 (Latitude):</Text>
            <Text style={styles.value}>{location.latitude.toFixed(6)}</Text>
            
            <Text style={styles.label}>经度 (Longitude):</Text>
            <Text style={styles.value}>{location.longitude.toFixed(6)}</Text>
            
            {location.accuracy && (
              <>
                <Text style={styles.label}>精度 (米):</Text>
                <Text style={styles.value}>{location.accuracy.toFixed(2)}</Text>
              </>
            )}
          </View>
        )}

        {heading && (
          <View style={styles.infoContainer}>
            <Text style={styles.sectionTitle}>朝向信息</Text>
            <Text style={styles.label}>设备朝向:</Text>
            <Text style={styles.value}>{heading.heading}°</Text>
            <Text style={styles.directionText}>
              {getDirectionText(heading.heading)}
            </Text>
            {heading.accuracy !== null && (
              <>
                <Text style={styles.label}>精度:</Text>
                <Text style={styles.value}>{heading.accuracy}°</Text>
              </>
            )}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 30,
    marginTop: 20,
  },
  infoContainer: {
    marginTop: 30,
    padding: 20,
    backgroundColor: '#f0f0f0',
    borderRadius: 10,
    width: '100%',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 15,
    color: '#333',
  },
  label: {
    fontSize: 14,
    color: '#666',
    marginTop: 10,
  },
  value: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000',
    marginTop: 5,
  },
  directionText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#007AFF',
    marginTop: 10,
    textAlign: 'center',
  },
});

import BottomSheet from '@gorhom/bottom-sheet';
import React, { useCallback, useEffect, useRef } from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import MapView, { UrlTile } from 'react-native-maps';
import { SafeAreaView } from 'react-native-safe-area-context';
import ViewCone from '../components/ViewCone';
import { useGameSession } from '../hooks/useGameSession';
import { useLocation } from '../hooks/useLocation';
import { mapStyles } from '../styles/mapStyles';

const VIEW_CONE_SPAN = 60;   
const VIEW_CONE_RADIUS = 50; 
const UID_ADMIN = "408808b8-777c-469a-867d-dd5e7d5e38e2"

export default function GamePage() {
  const { 
    location, 
    heading, 
    loading, 
    error, 
    isTracking, 
    startTracking, 
    stopTracking } = useLocation(true);

  const gameSession = useGameSession();
  // const {
  //   status,
  //   maxRiddleDurationMinutes,
  //   roundLandmarks,
  //   currentTarget,
  //   timeSecondsLeft,
  //   isTimerActive,
  //   errorMessage} = gameSession;
  const bottomSheetRef = useRef<BottomSheet>(null);
  
  // =============== GAME INIT ===============
  useEffect(() => {
    if (location && heading) {
      gameSession.initGame({
        userId: UID_ADMIN,
        latitude: location.latitude,
        longitude: location.longitude,
        angle: heading.heading,
        spanDeg: VIEW_CONE_SPAN,
        coneRadiusMeters: VIEW_CONE_RADIUS,
      });
    }
  }, []);
  
  // 使用 useCallback 缓存处理函数
  const handleStartGame = useCallback(async (
    currentLocation: { latitude: number; longitude: number }, 
    currentHeading: { heading: number }) => {
      try {
        console.log('Starting Game', { 
          location: currentLocation, 
          heading: currentHeading,
          userId: gameSession.userId 
        });
        await gameSession.startRound({
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          angle: currentHeading.heading,
          radiusMeters: 500,
          language: 'english',
          style: 'medieval'
        });
        bottomSheetRef.current?.expand();
      } catch (error) {
      console.error('[Mobile][Game.tsx] Failed starting game:', error);
      alert(`[Mobile][Game.tsx]: ${error instanceof Error ? error.message : 'Unknown Error'}`);
    }
      
    }, [gameSession]);

  const handleFinishGame = useCallback(async () => {
    try {
      if (gameSession.userId) {
        await gameSession.finishRound({ userId: gameSession.userId });
      }
      bottomSheetRef.current?.close();
    } catch (error) {
      console.error('[Mobile][Game.tsx] Failed finishing game:', error);
      alert(`[Mobile][Game.tsx] Failed finishing game: ${error instanceof Error ? error.message : 'Unknown Error'}`);
    }
  }, [gameSession]);

  return (
    <SafeAreaView style={mapStyles.container} edges={['top']}>
      <MapView
        style={mapStyles.map}
        mapType="none"             
        initialRegion={{
          latitude: location?.latitude ?? 52.145765,
          longitude: location?.longitude ?? -8.641198,
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
      
      {/* Bottom Sheet- StartGame/EndRound Button*/}
      <View style={mapStyles.bottomBar}>
        <TouchableOpacity 
          style={mapStyles.startButton}
          onPress={() => {
            if (location && heading) {
              if (gameSession.status === 'inRound') {
                handleFinishGame();
              } else {
                handleStartGame(location, heading);
              }
            }
          }}
        >
          <Text style={mapStyles.startButtonText}>
            {gameSession.status === 'inRound' ? 'EndRound' : 'StartRound'}
          </Text>
        </TouchableOpacity>
      </View>
      
      {/* Bottom Sheet - 上拉菜单 */}
      <BottomSheet
        ref={bottomSheetRef}
        index={-1} // 初始状态：关闭
        snapPoints={['25%', '50%', '90%']} // 三个停靠点
        enablePanDownToClose={true}
      >
        <View style={mapStyles.bottomSheetContent}>
          <Text style={mapStyles.bottomSheetTitle}>Game Info</Text>
          {/* TODO: 这里添加谜语、答案输入等 */}
        </View>
      </BottomSheet>
    </SafeAreaView>
  );
}

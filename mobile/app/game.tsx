import BottomSheet from '@gorhom/bottom-sheet';
import React, { useRef } from 'react';
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
  if (location && heading) {
    gameSession.initGame({
      userId: UID_ADMIN, // UID ADMIN FOR TESTING PURPOSE
      latitude: location.latitude,
      longitude: location.longitude,
      angle: heading.heading,
      spanDeg: VIEW_CONE_SPAN,
      coneRadiusMeters: VIEW_CONE_RADIUS,
    });
  }
  
  const handleStartGame = () => {
    if (location && heading) {
      gameSession.startRound({
        latitude: location.latitude,
        longitude: location.longitude,
        angle: heading.heading,
        radiusMeters: 500, // FOR TESTING PURPOSE
        language: 'english', // FOR TESTING PURPOSE
        style: 'medieval' // FOR TESTING PURPOSE
      });
    }

    bottomSheetRef.current?.expand();
  }

  const handleFinishGame = () => {

  }

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
          onPress={gameSession.status === 'inRound' ? handleFinishGame : handleStartGame}
          disabled={gameSession.role === 'guest'}
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

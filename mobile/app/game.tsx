import React, { useCallback, useEffect, useMemo, useState } from 'react';
import MapView, { UrlTile } from 'react-native-maps';
import { SafeAreaView } from 'react-native-safe-area-context';
import AnswerResult from '../components/AnswerResult';
import FloatingActionButton from '../components/FloatingActionButton';
import GameHud from '../components/GameHUD';
import LandmarkPolygon from '../components/LandmarkPolygon';
import RiddleBubble from '../components/RiddleBubble';
import ViewCone from '../components/ViewCone';
import { useGameSession } from '../hooks/useGameSession';
import { useLocation } from '../hooks/useLocation';
import { mapStyles } from '../styles/mapStyles';
import { LandmarkDTO } from '../types';
import { calculateDistance } from '../utils/calculateDistance';
import { GAME_CONFIG } from '../utils/constants';

const VIEW_CONE_SPAN = GAME_CONFIG.VIEW_ANGLE;
const VIEW_CONE_RADIUS = GAME_CONFIG.VIEW_RADIUS;
const UID_ADMIN = '408808b8-777c-469a-867d-dd5e7d5e38e2';
const MAX_DISPLAY_LANDMARKS_COUNT = 10;
const MAX_DISPLAY_LANDMARKS_DISTANCE = 500;
const DEFAULT_LANGUATE = 'english';
const DEFAULT_STYLE = 'medieval';

export default function GamePage() {
  const { location, heading, loading, error, isTracking, startTracking, stopTracking } =
    useLocation(true);

  const gameSession = useGameSession();
  const [hasInitialized, setHasInitialized] = useState(false);
  const [showAnswerResult, setShowAnswerResult] = useState(false);

  // =============== GAME INIT ===============
  // 1. GameSession init
  useEffect(() => {
    if (location && heading && !hasInitialized) {
      setHasInitialized(true);
      gameSession.setRole('admin'); // FOR DEV
      gameSession.initGame({
        userId: UID_ADMIN, // FOR DEV
        latitude: location.latitude,
        longitude: location.longitude,
        angle: heading.heading,
        spanDeg: VIEW_CONE_SPAN,
        coneRadiusMeters: VIEW_CONE_RADIUS,
      });
    }
  }, [location, heading, hasInitialized]);

  // 2. Tracking Player Movement

  useEffect(() => {
    if (!location || !heading || !gameSession.userId) {
      return;
    }
    gameSession.updatePosition({
      latitude: location.latitude,
      longitude: location.longitude,
      angle: heading.heading,
      spanDeg: VIEW_CONE_SPAN,
      coneRadiusMeters: VIEW_CONE_RADIUS,
    });
  }, [location, heading, gameSession.userId, gameSession.role]);

  // 3. Landmark Display
  const displayLandmarks = useMemo(() => {
    if (!location || gameSession.role === 'guest' || gameSession.roundLandmarks.length === 0) {
      return [];
    }

    const landmarksWithDistance = gameSession.roundLandmarks
      .map(landmark => {
        if (!landmark.centroid?.latitude || !landmark.centroid?.longitude) {
          return null;
        }

        const distance = calculateDistance(
          { latitude: location.latitude, longitude: location.longitude },
          { latitude: landmark.centroid.latitude, longitude: landmark.centroid.longitude }
        );
        return { landmark, distance };
      })
      .filter(item => item !== null) as Array<{ landmark: LandmarkDTO; distance: number }>;

    return landmarksWithDistance
      .filter(item => item.distance <= MAX_DISPLAY_LANDMARKS_DISTANCE)
      .sort((a, b) => a.distance - b.distance)
      .slice(0, MAX_DISPLAY_LANDMARKS_COUNT)
      .map(item => item.landmark);
  }, [location, gameSession.roundLandmarks, gameSession.role]);

  // =============== GAME LOGIC ===============

  const handleStartGame = useCallback(async () => {
    if (!location || !heading || !gameSession.userId) {
      console.warn('[Game.tsx] Cannot start game: missing location, heading, or userId');
      return;
    }
    try {
      if (gameSession.status === 'finished') {
        console.log('[Mobile][Game.tsx] Game finished, re-initializing...');
        try {
          await gameSession.initGame({
            userId: gameSession.userId,
            latitude: location.latitude,
            longitude: location.longitude,
            angle: heading.heading,
            spanDeg: VIEW_CONE_SPAN,
            coneRadiusMeters: VIEW_CONE_RADIUS,
          });
        } catch (initError) {
          console.error('[Mobile][Game.tsx] initGame failed:', initError);
          alert(
            `Failed to initialize game: ${initError instanceof Error ? initError.message : 'Unknown Error'}`
          );
          return; // init failure
        }
      }

      console.log('[Mobile][Game.tsx] Before startRound, status:', gameSession.status);
      console.log('Starting Game', {
        location,
        heading,
        userId: gameSession.userId,
      });
      await gameSession.startRound({
        latitude: location.latitude,
        longitude: location.longitude,
        angle: heading.heading,
        radiusMeters: 500,
        language: DEFAULT_LANGUATE, // DEV
        style: DEFAULT_STYLE, // DEV
      });
      console.log('[Mobile][Game.tsx] After startRound, status:', gameSession.status);
    } catch (error) {
      console.error('[Mobile][Game.tsx] Failed starting game:', error);
      alert(`[Mobile][Game.tsx]: ${error instanceof Error ? error.message : 'Unknown Error'}`);
    }
  }, [gameSession, location, heading]);

  const handleSubmitAnswer = useCallback(async () => {
    if (!location || !heading || !gameSession.userId) {
      console.warn('[Game.tsx] Cannot submit answer: missing location, heading, or userId');
      return;
    }

    try {
      await gameSession.submitAnswer({
        secondsUsed: gameSession.maxRiddleDurationMinutes * 60 - (gameSession.timeSecondsLeft ?? 0),
        currentAngle: heading.heading,
        latitude: location.latitude,
        longitude: location.longitude,
      });

      // display result
      setShowAnswerResult(true);
    } catch (error) {
      console.error('[Mobile][Game.tsx] Failed submitting answer:', error);
      alert(`Failed to submit answer: ${error instanceof Error ? error.message : 'Unknown Error'}`);
    }
  }, [gameSession, location, heading]);

  const handleFinishGame = useCallback(async () => {
    try {
      if (gameSession.userId) {
        await gameSession.finishRound();
      }
    } catch (error) {
      console.error('[Mobile][Game.tsx] Failed finishing game:', error);
      alert(
        `[Mobile][Game.tsx] Failed finishing game: ${error instanceof Error ? error.message : 'Unknown Error'}`
      );
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
        <UrlTile urlTemplate="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" maximumZ={19} />

        {/* ViewCone */}
        {location && heading && (
          <ViewCone
            center={location}
            headingDeg={heading.heading}
            spanDeg={VIEW_CONE_SPAN}
            radiusMeters={VIEW_CONE_RADIUS}
            fillColor="rgba(0, 122, 255, 0.25)" // 随意调整
          />
        )}

        {/* Landmarks */}
        {displayLandmarks.map(landmark => (
          <LandmarkPolygon key={landmark.id} landmark={landmark} isSolved={false} />
        ))}
      </MapView>

      {/* GameHUD */}
      <GameHud
        status={gameSession.status}
        timeSecondsLeft={gameSession.timeSecondsLeft}
        attemptsLeft={gameSession.currentTarget?.attemptsLeft}
        onFinishRound={handleFinishGame}
      />

      {/* RiddleBubble */}
      {location && gameSession.status === 'inRound' && (
        <RiddleBubble
          riddle={gameSession.currentTarget?.riddle}
          location={location}
          autoCollapseDistance={10}
        />
      )}

      {/* AnswerResult Modal */}
      <AnswerResult
        message={gameSession.lastMessage}
        visible={showAnswerResult}
        onClose={() => setShowAnswerResult(false)}
      />

      {/* FloatingActionButton */}
      <FloatingActionButton
        status={gameSession.status}
        onPress={() => {
          if (location && heading) {
            if (gameSession.status === 'inRound') {
              handleSubmitAnswer();
            } else {
              // TODO: startRound
              handleStartGame();
            }
          }
        }}
      />
    </SafeAreaView>
  );
}

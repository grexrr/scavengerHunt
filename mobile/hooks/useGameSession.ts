import { useEffect, useRef, useState } from 'react';
import { apiClient } from '../services/client';
import { storageService } from '../services/storageService';

type Role = 'guest' | 'player' | 'admin';
type GameStatus = 'initializing' | 'initialized' | 'inRound' | 'finished' | 'error';

interface InitGameResponse {
  landmarks: LandmarkDTO[];
}

interface TargetDTO {
  id: string;
  name: string;
  riddle: string;
  attemptsLeft: number;
}

interface SubmitAnswerResponse {
  isCorrect: boolean;
  gameFinished: boolean;
  message: string;
  target?: TargetDTO;
}

interface LandmarkDTO {
  id: string;
  name: string;
  centroid: {
    latitude: number;
    longitude: number;
  };
  coordinates: number[][];
}

interface GameSessionState {
  userId?: string;
  role: Role;
  status: GameStatus;
  maxRiddleDurationMinutes: number;
  roundLandmarks: LandmarkDTO[];
  currentTarget?: TargetDTO;
  timeSecondsLeft: number | null;
  isTimerActive: boolean;
  lastMessage?: string;
  errorMessage?: string;
}

export function useGameSession() {
  const [state, setState] = useState<GameSessionState>({
    status: 'initializing',
    role: 'guest',
    maxRiddleDurationMinutes: 30,
    roundLandmarks: [],
    currentTarget: undefined,
    timeSecondsLeft: null,
    isTimerActive: false,
    errorMessage: undefined,
  });

  useEffect(() => {
    (async () => {
      const role = await storageService.getRole();
      if (role) {
        const normalizedRole = role.toLowerCase() as Role;
        updateState({ role: normalizedRole });
      }
    })();
  }, []);

  const timerRef = useRef<NodeJS.Timeout | null>(null);

  function updateState(patch: Partial<GameSessionState>) {
    setState(prev => ({ ...prev, ...patch }));
  }

  async function updatePosition(params: {
    latitude: number;
    longitude: number;
    angle: number;
    spanDeg: number;
    coneRadiusMeters: number;
  }) {
    if (!state.userId) {
      console.warn('[updatePosition] User ID not found');
      return;
    }

    try {
      await apiClient.post('/api/game/update-position', {
        userId: state.userId,
        ...params,
      });
    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
    }
  }

  async function initGame(params: {
    userId: string;
    latitude: number;
    longitude: number;
    angle: number;
    spanDeg: number;
    coneRadiusMeters: number;
  }) {
    updateState({
      userId: params.userId,
      status: 'initialized',
      errorMessage: undefined,
    });
    try {
      const data = await apiClient.post<InitGameResponse>('/api/game/init-game', params);
      // console.log('[initGame] Full API response:', JSON.stringify(data, null, 2));
      // console.log('[initGame] data.landmarks type:', typeof data.landmarks);
      // console.log('[initGame] data.landmarks is array?', Array.isArray(data.landmarks));
      // console.log('[initGame] landmarks count:', data.landmarks?.length ?? 0);
      if (data.landmarks && Array.isArray(data.landmarks)) {
        updateState({
          roundLandmarks: data.landmarks,
        });
        // console.log('[initGame] State updated successfully');
      } else {
        console.warn('[initGame] Invalid landmarks data:', data.landmarks);
      }
    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
    }
  }

  async function startRound(params: {
    latitude: number;
    longitude: number;
    angle: number;
    radiusMeters: number;
    language?: string;
    style?: string;
  }) {
    if (!state.userId) {
      throw new Error('User ID not found. Please call initGame first.');
    }

    try {
      const target = await apiClient.post<TargetDTO>('/api/game/start-round', {
        userId: state.userId,
        ...params,
      });

      updateState({
        status: 'inRound',
        currentTarget: {
          id: target.id,
          name: target.name,
          riddle: target.riddle,
          attemptsLeft: target.attemptsLeft,
        },
      });

      startTimer();
    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
      throw err;
    }
  }

  async function submitAnswer(params: {
    secondsUsed?: number;
    latitude?: number;
    longitude?: number;
    currentAngle?: number;
  }) {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    const secondsUsed =
      params.secondsUsed ?? state.maxRiddleDurationMinutes * 60 - (state.timeSecondsLeft ?? 0);
    updateState({ isTimerActive: false });

    try {
      const data = await apiClient.post<SubmitAnswerResponse>('/api/game/submit-answer', {
        userId: state.userId,
        secondsUsed: secondsUsed,
        latitude: params.latitude,
        longitude: params.longitude,
        currentAngle: params.currentAngle,
      });

      if (data.gameFinished) {
        updateState({
          status: 'finished',
          timeSecondsLeft: 0,
          lastMessage: data.message,
        });
      } else if (data.target) {
        updateState({
          currentTarget: {
            id: data.target.id,
            name: data.target.name,
            riddle: data.target.riddle,
            attemptsLeft: data.target.attemptsLeft,
          },
          lastMessage: data.message,
        });
        startTimer();
      }
    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
    }
  }

  async function finishRound(params?: {
    latitude: number;
    longitude: number;
    angle: number;
    spanDeg: number;
    coneRadiusMeters: number;
  }) {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    try {
      await apiClient.post('/api/game/finish-round', {
        userId: state.userId,
      });

      updateState({
        status: 'finished',
        currentTarget: undefined,
        timeSecondsLeft: null,
        isTimerActive: false,
      });

      // refetch roundLandmarks
      if (params && state.userId) {
        const data = await apiClient.post<InitGameResponse>('/api/game/init-game', {
          userId: state.userId,
          ...params,
        });
        if (data.landmarks && Array.isArray(data.landmarks)) {
          updateState({
            roundLandmarks: data.landmarks,
          });
        }
      }
    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
    }
  }

  // utils
  function setRole(newRole: Role) {
    updateState({ role: newRole });
    storageService.setRole(newRole);
  }

  function startTimer() {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    // start timer
    setState(prev => ({
      ...prev,
      timeSecondsLeft: prev.maxRiddleDurationMinutes * 60,
      isTimerActive: true,
    }));

    timerRef.current = setInterval(() => {
      setState(prev => {
        if (prev.timeSecondsLeft == null || prev.timeSecondsLeft <= 1) {
          // times up
          if (timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
          }

          // IMPORTANT: submitAnswer
          const secondsUsed = prev.maxRiddleDurationMinutes * 60 + 1;
          if (prev.userId) {
            submitAnswer({
              secondsUsed: secondsUsed,
            });
          }

          return {
            ...prev,
            timeSecondsLeft: -1,
            isTimerActive: false,
          };
        }
        return { ...prev, timeSecondsLeft: prev.timeSecondsLeft - 1 };
      });
    }, 1000);
  }

  return {
    ...state,
    updatePosition,
    submitAnswer,
    startRound,
    initGame,
    finishRound,
    setRole,
  };
}

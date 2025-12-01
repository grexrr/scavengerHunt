import { useEffect, useRef, useState } from 'react';
import { apiClient } from '../services/api/client';
import { storageService } from '../services/storage/storageService';

type Role = 'guest' | 'player' | 'admin'
type GameStatus = 'initializing' | 'inRound' | 'finished' | 'error';

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
    status: 'finished',
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
        updateState({ role: normalizedRole })
      } 
    })()
  }, [])

  const timerRef = useRef<NodeJS.Timeout | null>(null);

  function updateState(patch: Partial<GameSessionState>) {
    setState(prev => ({ ...prev, ...patch }));
  }

  async function updatePosition(params: {
    userId: string;
    latitude: number;
    longitude: number;
    angle: number;
    spanDeg: number;
    coneRadiusMeters: number;
  }) {
    try {
      await apiClient.post('/api/game/update-position', params);
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
      status: 'initializing', 
      errorMessage: undefined });
    try {
      const data = await apiClient.post<InitGameResponse>('/api/game/init-game', params);

      updateState({
        roundLandmarks: data.landmarks ?? [],
      });

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
      })

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
    userId: string;
    secondsUsed?: number;
    currentAngle?: number;
    latitude?: number;
    longitude?: number;
  }) {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    const secondsUsed = params.secondsUsed ?? (state.maxRiddleDurationMinutes * 60 - (state.timeSecondsLeft ?? 0));
    updateState({ isTimerActive: false });

    try {
      const data = await apiClient.post<SubmitAnswerResponse>('/api/game/submit-answer', {
        userId: params.userId,
        secondsUsed: secondsUsed,
        currentAngle: params.currentAngle,
        latitude: params.latitude,
        longitude: params.longitude,
      })

      if (data.gameFinished) {
        updateState({
          status: 'finished',
          timeSecondsLeft: 0,
          lastMessage: data.message
        });
      } else if (data.target) {
        updateState({
          currentTarget: {
            id: data.target.id,
            name: data.target.name,
            riddle: data.target.riddle,
            attemptsLeft: data.target.attemptsLeft,
          },
          lastMessage: data.message
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

  async function finishRound(params:{
    userId: string;
  }){
    if (timerRef.current){
      clearInterval(timerRef.current);
      timerRef.current = null;
    }

    try {
      await apiClient.post('/api/game/finish-round', {
        userId: params.userId,
      });

      updateState({
        status: 'finished',
        roundLandmarks: [],
        currentTarget: undefined,
        timeSecondsLeft: null,
        isTimerActive: false
      })

    } catch (err) {
      updateState({
        status: 'error',
      });
      console.error(err);
    }
  }

  // utils
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
                userId: prev.userId, 
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
    finishRound
  };
}

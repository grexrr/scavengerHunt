import { useEffect, useRef, useState } from 'react';
import { storageService } from '../services/storage/storageService';

type Role = 'guest' | 'player' | 'admin'
type GameStatus = 'initializing' | 'inRound' | 'finished' | 'error';

interface LandmarkDTO {
  id: string;
  name: string;
  coordinates: number[][];
}

interface currentTarget {
  id: string;
  name: string;
  riddle: string;
  attemptsLeft: number;
}

interface GameSessionState {
  userId?: string;  
  role: Role;
  status: GameStatus;
  maxRiddleDurationMinutes: number;
  roundLandmarks: LandmarkDTO[];
  currentTarget?: currentTarget;
  timeSecondsLeft: number | null;
  isTimerActive: boolean;
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
      const res = await fetch('/api/game/update-position', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params),
      });
      
      if (!res.ok) {
        throw new Error(`update-position failed with status ${res.status}`);
      }
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
      const res = await fetch('/api/game/init-game', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params),
      });

      if (!res.ok) {
        throw new Error(`init-game failed with status ${res.status}`);
      }

      const data = await res.json();

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
      const res = await fetch('/api/game/start-round', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: state.userId,
          ...params
        }),
      });
      if (!res.ok) {
        throw new Error(`start-round failed with status ${res.status}`);
      }

      const target = await res.json();
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
      const res = await fetch('/api/game/submit-answer', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: params.userId,
          secondsUsed: secondsUsed.toString(),
          currentAngle: params.currentAngle?.toString(),
          latitude: params.latitude?.toString(),
          longitude: params.longitude?.toString(),
        }),
      });

      if (!res.ok) {
        throw new Error(`submit-answer failed with status ${res.status}`);
      }

      const data = await res.json();

      if (data.gameFinished) {
        updateState({
          status: 'finished',
          timeSecondsLeft: 0,
        });
      } else if (data.target) {
        updateState({
          currentTarget: {
            id: data.target.id,
            name: data.target.name,
            riddle: data.target.riddle,
            attemptsLeft: data.target.attemptsLeft,
          },
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
      const res = await fetch('/api/game/finish-round', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: params.userId,
        })
      });

      if (!res.ok) {
        throw new Error(`finish-round failed with status ${res.status}`);
      }

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

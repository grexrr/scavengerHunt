import { useRef, useState } from 'react';

type GameStatus = 'idle' | 'initializing' | 'inRound' | 'finished' | 'error'

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

interface InitGameParams {
    userId: string;
    latitude: number;
    longitude: number;
    angle: number;
    spanDeg: number;
    coneRadiusMeters: number;
}

interface StartRoundParams {
    userId: string;
    latitude: number;
    longitude: number;
    angle: number;
    radiusMeters: number;
    language?: string;
    style?: string;
}

interface GameSessionState {
    status: GameStatus;
    roundLandmarks: LandmarkDTO[];
    currentTarget?: currentTarget;
    timeSecondsLeft: number | null;
    isTimerActive: boolean;
    errorMessage?: string;
}

export function useGameSession() {
    const [state, setState] = useState<GameSessionState>({
        status: 'idle',
        roundLandmarks: [],
        currentTarget: undefined,
        timeSecondsLeft: null,
        isTimerActive: false,
        errorMessage: undefined,
    });

    const timerRef = useRef<NodeJS.Timeout | null>(null);

    function updateState(patch: Partial<GameSessionState>) {
        setState(prev => ({ ...prev, ...patch }));
    }

    async function initGame(params: InitGameParams) {
        updateState({ status: 'initializing', errorMessage: undefined });
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
                status: 'idle',
                roundLandmarks: data.landmarks ?? [],
            });
        } catch (err) {
            updateState({
                status: 'error'
            });
            console.error(err);
        }
    }

    async function startRound(params: StartRoundParams) {
        try {
            const res = await fetch('/api/game/start-round', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(params),
            })
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
                    attemptsLeft: target.attemptsLeft
                }
            })
        } catch (err) {
            updateState({
                status: 'error'
            });
            console.error(err);
        }
    }

    async function submitAnswer() {
        
    }

    return {
        ...state,
        initGame,
        startRound,
    }
}
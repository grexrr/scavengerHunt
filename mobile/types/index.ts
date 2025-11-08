export interface LandmarkDTO {
  id: string;
  name: string;
  coordinates: number[][];
}

export interface TargetResponse {
  id: string;
  name: string;
  attemptsLeft: number;
  riddle: string;
}

export interface InitGameResponse {
  landmarks: LandmarkDTO[];
}

export interface SubmitAnswerResponse {
  isCorrect: boolean;
  gameFinished: boolean;
  message: string;
  target?: TargetResponse;
}

export interface User {
  userId: string;
  username: string;
  role?: string;
}

export interface GameSessionState {
  userId: string;
  status: 'not_started' | 'in_progress' | 'completed';
  landmarks?: LandmarkDTO[];
  currentTarget?: TargetResponse;
  timeRemaining?: number; // sec
}

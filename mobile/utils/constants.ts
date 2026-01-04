export const API_BASE_URL = __DEV__ ? 'http://192.168.1.9:8443' : 'https://3.81.254.35';

export const GAME_CONFIG = {
  TIME_LIMIT_SEC: 1800,
  MAX_WRONG_ANSWER: 3,
  VIEW_ANGLE: 45,
  VIEW_RADIUS: 80,
} as const;

import { Platform } from 'react-native';
import Constants from 'expo-constants';

const isDev = __DEV__;
console.log('[Constants] __DEV__ =', isDev);
console.log('[Constants] Platform =', Platform.OS);

const extra = Constants.expoConfig?.extra ?? {};
const apiBaseUrlDev = String(extra.apiBaseUrlDev || 'http://192.168.1.9:8443');
const apiBaseUrlProd = String(extra.apiBaseUrlProd || 'https://454bb8d88e34.ngrok-free.app');

export const API_BASE_URL = isDev ? apiBaseUrlDev : apiBaseUrlProd;

console.log('[Constants] API_BASE_URL =', API_BASE_URL);

export const GAME_CONFIG = {
  TIME_LIMIT_SEC: 1800,
  MAX_WRONG_ANSWER: 3,
  VIEW_ANGLE: 45,
  VIEW_RADIUS: 80,
} as const;

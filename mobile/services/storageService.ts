import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEYS = {
  USER_ID: '@scavengerhunt:userId',
  USERNAME: '@scavengerhunt:username',
  ROLE: '@scavengerhunt:role',
  MAP_ZOOM: '@scavengerhunt:mapZoom',
  FIRST_PERSON_ZOOM: '@scavengerhunt:firstPersonZoom',
  FIRST_PERSON_ENABLED: '@scavengerhunt:firstPersonEnabled'
} as const;

class StorageService {
  async setUserId(userId: string): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEYS.USER_ID, userId);
  }

  async getUserId(): Promise<string | null> {
    return await AsyncStorage.getItem(STORAGE_KEYS.USER_ID);
  }

  async setUsername(username: string): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEYS.USERNAME, username);
  }

  async getUsername(): Promise<string | null> {
    return await AsyncStorage.getItem(STORAGE_KEYS.USERNAME);
  }

  async setRole(role: string): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEYS.ROLE, role);
  }

  async getRole(): Promise<string | null> {
    return await AsyncStorage.getItem(STORAGE_KEYS.ROLE);
  }

  async setMapZoom(zoom: number): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEYS.MAP_ZOOM, zoom.toString());
  }

  async getMapZoom(): Promise<number> {
    const zoom = await AsyncStorage.getItem(STORAGE_KEYS.MAP_ZOOM);
    if (zoom) return parseFloat(zoom);
    const legacyZoom = await AsyncStorage.getItem(STORAGE_KEYS.FIRST_PERSON_ZOOM);
    return legacyZoom ? parseFloat(legacyZoom) : 18;
  }

  async setFirstPersonEnabled(enabled: boolean): Promise<void> {
    await AsyncStorage.setItem(STORAGE_KEYS.FIRST_PERSON_ENABLED, enabled.toString());
  }

  async getFirstPersonEnabled(): Promise<boolean> {
    const enabled = await AsyncStorage.getItem(STORAGE_KEYS.FIRST_PERSON_ENABLED);
    return enabled === 'true';
  }

  async clearUserData(): Promise<void> {
    await AsyncStorage.multiRemove([
      STORAGE_KEYS.USER_ID,
      STORAGE_KEYS.USERNAME,
      STORAGE_KEYS.ROLE,
    ]);
  }

  async clearAll(): Promise<void> {
    await AsyncStorage.clear();
  }
}

export const storageService = new StorageService();

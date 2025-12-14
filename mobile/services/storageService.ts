import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEYS = {
  USER_ID: '@scavengerhunt:userId',
  USERNAME: '@scavengerhunt:username',
  ROLE: '@scavengerhunt:role',
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

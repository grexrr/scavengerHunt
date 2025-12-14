import { useEffect, useState } from 'react';
import { storageService } from '../services/storageService';

interface User {
  userId: string | null;
  username: string | null;
  role: string | null;
}

export function useUser() {
  const [user, setUser] = useState<User>({
    userId: null,
    username: null,
    role: null,
  });
  const [isLoading, setIsLoading] = useState(true);

  const loadUser = async () => {
    try {
      const [userId, username, role] = await Promise.all([
        storageService.getUserId(),
        storageService.getUsername(),
        storageService.getRole(),
      ]);

      setUser({
        userId,
        username,
        role,
      });
    } catch (error) {
      console.error('[useUser] Failed to load user:', error);
    } finally {
      setIsLoading(false);
    }
  };
 
  useEffect(() => {
    loadUser();
  }, []);

  const isLoggedIn = user.userId !== null;

  const refreshUser = async () => {
    await loadUser();
  };

  return {
    user,
    isLoading,
    isLoggedIn,
    refreshUser,
  };
}

import { useEffect, useState } from 'react';
import { Alert, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { apiClient } from '../services/client';
import { storageService } from '../services/storageService';
import { commonStyles } from '../styles/commonStyles';

interface UserProfile {
  userId: string;
  username: string;
  email: string;
  role: string;
  preferredLanguage: string;
  preferredStyle: string;
}

export default function SettingsPage() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loginToken, setloginToken] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  const [isLoading, setIsLoading] = useState(false);

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [editableProfile, setEditableProfile] = useState<UserProfile | null>(null);
  const [isProfileUpdated, setIsProfileUpdated] = useState(false);

  // registration
  const [showRegister, setShowRegister] = useState(false);
  const [registerUsername, setRegisterUsername] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');
  const [registerEmail, setRegisterEmail] = useState('');
  const [registerLanguage, setRegisterLanguage] = useState('');
  const [registerStyle, setRegisterStyle] = useState('');

  const handleLogin = async () => {
    if (!loginToken.trim() || !loginPassword.trim()) {
      Alert.alert('Error', 'Require username/email and password');
      return;
    }

    setIsLoading(true);

    try {
      const isEmail = loginToken.includes('@');

      const requestBody = {
        password: loginPassword,
        ...(isEmail
          ? { email: loginToken, username: null }
          : { username: loginToken, email: null }),
      };

      const response = await apiClient.post<{
        userId: string;
        username: string;
        email: string;
        role: string;
      }>('/api/auth/login', requestBody);

      await storageService.setUserId(response.userId);
      await storageService.setUsername(response.username);
      await storageService.setRole(response.role);

      await loadProfile(response.userId);
      setIsLoggedIn(true);

      setloginToken('');
      setLoginPassword('');
      // Alert.alert('Success', 'Login successful!')
    } catch (error: any) {
      Alert.alert('Login Failed', error.message || 'Please check your credentials');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      const userId = await storageService.getUserId();
      if (userId) {
        try {
          await apiClient.post('/api/auth/logout', { userId });
        } catch (apiError) {
          console.warn('Backend logout failed:', apiError);
        }
      }

      await storageService.clearUserData();
      setIsLoggedIn(false);
      setProfile(null);
      setEditableProfile(null);
      setIsProfileUpdated(false);
      Alert.alert('Success', 'You have been logged out');
    } catch (error: any) {
      Alert.alert('Error', 'Failed to logout: ' + error.message);
    }
  };

  const handleUpdate = async () => {
    if (!editableProfile || !isProfileUpdated) return;

    setIsLoading(true);
    try {
      await apiClient.post('/api/auth/update-profile', {
        userId: editableProfile.userId,
        username: editableProfile.username,
        email: editableProfile.email || '',
        preferredLanguage: editableProfile.preferredLanguage,
        preferredStyle: editableProfile.preferredStyle,
      });

      await loadProfile(editableProfile.userId);
      setIsProfileUpdated(false);

      Alert.alert('Success', 'Profile updated successfully');
    } catch (error: any) {
      let errorMsg = 'Failed to update profile';

      if (error.message) {
        if (error.message.includes('Username already exists')) {
          errorMsg = 'Username already exists. Please choose another one.';
        } else if (error.message.includes('Email already exists')) {
          errorMsg = 'Email already exists. Please use another email.';
        } else {
          errorMsg = error.message;
        }
      }

      Alert.alert('Update Failed', errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!registerUsername.trim() || !registerPassword.trim()) {
      Alert.alert('Error', 'Username and password are required');
      return;
    }

    setIsLoading(true);

    try {
      // 1. 注册
      await apiClient.post('/api/auth/register', {
        username: registerUsername.trim(),
        password: registerPassword.trim(),
        email: registerEmail.trim() || null,
        preferredLanguage: registerLanguage.trim().toLowerCase() || 'english',
        preferredStyle: registerStyle.toLowerCase() || 'medieval',
        createdAt: new Date().toISOString(),
      });

      // 2. 注册成功后，自动登录
      try {
        const loginResponse = await apiClient.post<{
          userId: string;
          username: string;
          email: string;
          role: string;
        }>('/api/auth/login', {
          username: registerUsername.trim(),
          password: registerPassword.trim(),
        });

        // 保存登录信息
        await storageService.setUserId(loginResponse.userId);
        await storageService.setUsername(loginResponse.username);
        await storageService.setRole(loginResponse.role);

        // 加载用户信息
        await loadProfile(loginResponse.userId);

        // 设置登录状态
        setIsLoggedIn(true);

        // 清空注册表单
        setRegisterUsername('');
        setRegisterPassword('');
        setRegisterEmail('');
        setRegisterLanguage('');
        setRegisterStyle('');
        setShowRegister(false);

        Alert.alert('Success', 'Registration and login successful!');
      } catch (loginError: any) {
        // 如果自动登录失败，提示用户手动登录
        Alert.alert('Registration Successful', 'Please login manually.');
        setShowRegister(false);
        // 将用户名填入登录表单，方便用户
        setloginToken(registerUsername.trim());
        setRegisterUsername('');
        setRegisterPassword('');
        setRegisterEmail('');
        setRegisterLanguage('');
        setRegisterStyle('');
      }
    } catch (error: any) {
      let errorMsg = 'Registration failed';

      if (error.message) {
        if (error.message.includes('Username already exists')) {
          errorMsg = 'Username already exists. Please choose another one.';
        } else if (error.message.includes('Email already exists')) {
          errorMsg = 'Email already exists. Please use another email.';
        } else {
          errorMsg = error.message;
        }
      }

      Alert.alert('Registration Failed', errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const loadProfile = async (userId: string) => {
    try {
      const profile_response = await apiClient.get<UserProfile>(`/api/auth/profile/${userId}`);
      setProfile(profile_response);
      setEditableProfile({ ...profile_response });
      console.log('User profile loaded:', profile_response);
    } catch (error: any) {
      Alert.alert('Error', 'Failed to load user profile: ' + error.message);
    }
  };

  useEffect(() => {
    if (profile && editableProfile) {
      const hasChanged =
        profile.username !== editableProfile.username ||
        profile.email !== editableProfile.email ||
        profile.preferredLanguage !== editableProfile.preferredLanguage ||
        profile.preferredStyle !== editableProfile.preferredStyle;
      setIsProfileUpdated(hasChanged);
    } else {
      setIsProfileUpdated(false);
    }
  }, [editableProfile, profile]);

  // 页面加载时检查登录状态
  useEffect(() => {
    const checkLoginStatus = async () => {
      const storedUserId = await storageService.getUserId();
      if (storedUserId) {
        setIsLoggedIn(true);
        await loadProfile(storedUserId);
      }
    };
    checkLoginStatus();
  }, []); // 空数组表示只在组件首次加载时执行一次

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView style={commonStyles.container}>
        <Text style={commonStyles.title}>Settings</Text>

        {!isLoggedIn ? (
          <View style={commonStyles.section}>
            {!showRegister ? (
              // Login Page
              <>
                <Text style={commonStyles.subtitle}>Log in</Text>

                {/* Username / Email Box */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Username / Email"
                  value={loginToken}
                  onChangeText={setloginToken}
                  autoCapitalize="none"
                />

                {/* Password Box */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Password"
                  value={loginPassword}
                  onChangeText={setLoginPassword}
                  secureTextEntry
                />

                {/* Login Button */}
                <TouchableOpacity
                  style={commonStyles.button}
                  onPress={handleLogin}
                  disabled={isLoading}
                >
                  <Text style={commonStyles.buttonText}>
                    {isLoading ? 'Logging in...' : 'Log in'}
                  </Text>
                </TouchableOpacity>

                {/* Register Button */}
                <TouchableOpacity
                  style={commonStyles.registerButton}
                  onPress={() => setShowRegister(true)}
                  disabled={isLoading}
                >
                  <Text style={commonStyles.registerButtonText}>{'Register'}</Text>
                </TouchableOpacity>
              </>
            ) : (
              // Registration Page
              <>
                <Text style={commonStyles.subtitle}>Register</Text>
                {/* Username */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Username *"
                  value={registerUsername}
                  onChangeText={setRegisterUsername}
                  autoCapitalize="none"
                />

                {/* Password */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Password *"
                  value={registerPassword}
                  onChangeText={setRegisterPassword}
                  secureTextEntry
                />

                {/* Email */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Email (optional)"
                  value={registerEmail}
                  onChangeText={setRegisterEmail}
                  autoCapitalize="none"
                  keyboardType="email-address"
                />

                {/* Language */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Preferred language (default: English)"
                  value={
                    registerLanguage
                      ? registerLanguage.charAt(0).toUpperCase() + registerLanguage.slice(1)
                      : ''
                  }
                  onChangeText={text => setRegisterLanguage(text.toLowerCase())}
                />

                {/* Style */}
                <TextInput
                  style={commonStyles.input}
                  placeholder="Preferred riddle style (default: Medieval)"
                  value={
                    registerStyle
                      ? registerStyle.charAt(0).toUpperCase() + registerStyle.slice(1)
                      : ''
                  }
                  onChangeText={text => setRegisterStyle(text.toLowerCase())}
                />

                {/* Registration Button */}
                <TouchableOpacity
                  style={commonStyles.button}
                  onPress={handleRegister}
                  disabled={isLoading}
                >
                  <Text style={commonStyles.buttonText}>
                    {isLoading ? 'Registering...' : 'Register'}
                  </Text>
                </TouchableOpacity>

                {/* Return button */}
                <TouchableOpacity
                  style={commonStyles.registerButton}
                  onPress={() => setShowRegister(false)}
                  disabled={isLoading}
                >
                  <Text style={commonStyles.registerButtonText}>{'Back to Login'}</Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        ) : (
          <>
            <View style={commonStyles.section}>
              <Text style={commonStyles.sectionTitle}>User Info</Text>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.secondaryText}>User ID</Text>
                <Text style={commonStyles.bodyText}>{profile?.userId || 'Loading...'}</Text>
              </View>

              {/* admin display */}
              {profile?.role === 'ADMIN' ? (
                <View style={commonStyles.sectionItem}>
                  <Text style={commonStyles.secondaryText}>ROLE</Text>
                  <Text style={commonStyles.bodyText}>{profile.role}</Text>
                </View>
              ) : null}

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.secondaryText}>Username</Text>
                <TextInput
                  style={commonStyles.input}
                  value={editableProfile?.username || ''}
                  onChangeText={text =>
                    setEditableProfile(prev => (prev ? { ...prev, username: text } : null))
                  }
                />
              </View>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.secondaryText}>Email</Text>
                <TextInput
                  style={commonStyles.input}
                  placeholder="email@example.com"
                  value={editableProfile?.email || ''}
                  onChangeText={text =>
                    setEditableProfile(prev => (prev ? { ...prev, email: text } : null))
                  }
                  autoCapitalize="none"
                  keyboardType="email-address"
                />
              </View>
            </View>

            <View style={commonStyles.section}>
              <Text style={commonStyles.sectionTitle}>Preference</Text>

              {/* Preferred Language */}
              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.secondaryText}>LANGUAGE</Text>
                <TextInput
                  style={commonStyles.input}
                  value={
                    editableProfile?.preferredLanguage
                      ? editableProfile.preferredLanguage.charAt(0).toUpperCase() +
                        editableProfile.preferredLanguage.slice(1)
                      : 'English'
                  }
                  onChangeText={text =>
                    setEditableProfile(prev =>
                      prev ? { ...prev, preferredLanguage: text.toLowerCase() } : null
                    )
                  }
                />
              </View>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.secondaryText}>RIDDLE STYLE</Text>
                <TextInput
                  style={commonStyles.input}
                  value={
                    editableProfile?.preferredStyle
                      ? editableProfile.preferredStyle.charAt(0).toUpperCase() +
                        editableProfile.preferredStyle.slice(1)
                      : 'Loading...'
                  }
                  onChangeText={text =>
                    setEditableProfile(prev =>
                      prev ? { ...prev, preferredStyle: text.toLowerCase() } : null
                    )
                  }
                />
              </View>
            </View>

            {/* Update Button */}
            <TouchableOpacity
              style={[commonStyles.button, !isProfileUpdated && { opacity: 0.5 }]}
              onPress={handleUpdate}
              disabled={!isProfileUpdated || isLoading}
            >
              <Text style={commonStyles.buttonText}>{isLoading ? 'Updating...' : 'Update'}</Text>
            </TouchableOpacity>

            {/* Logout Button */}
            <TouchableOpacity
              style={commonStyles.registerButton}
              onPress={handleLogout}
              disabled={isLoading}
            >
              <Text style={commonStyles.registerButtonText}>{'Logout'}</Text>
            </TouchableOpacity>
          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

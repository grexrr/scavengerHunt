import { useState } from 'react';
import { Alert, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { apiClient } from '../services/api/client';
import { storageService } from '../services/storage/storageService';
import { commonStyles } from '../styles/commonStyles';

export default function SettingsPage() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const [loginToken, setloginToken] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const [isProfileUpdated, setIsProfileUpdated] = useState(false);

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
          : { username: loginToken, email: null }
        )
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

      setIsLoggedIn(true);

      setloginToken('');
      setLoginPassword('');
      // Alert.alert('Success', 'Login successful!')
    } catch (error: any) {
      Alert.alert('Login Failed', error.message || 'Please check your credentials');
    } finally {
      setIsLoading(false);
    }
  }

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
      Alert.alert('Success', 'You have been logged out');
    } catch (error: any) {
      Alert.alert('Error', 'Failed to logout: ' + error.message);
    }
  }
  
  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView style={commonStyles.container}>
        <Text style={commonStyles.title}>Settings</Text>

        {!isLoggedIn ? (
          <View style={commonStyles.section}>
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
              placeholder='Password'
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
              // onPress={handleLogin}
              disabled={isLoading}
            >
              <Text style={commonStyles.registerButtonText}>
                {'Register'}
              </Text>
            </TouchableOpacity>

          </View>
        ) : (
          <>
            < View style={commonStyles.section}>
              <Text style={commonStyles.sectionTitle}>User Info</Text>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.sectionItemText}>Login Status</Text>
              </View>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.sectionItemText}>User Profile</Text>
              </View>
            </View> 


            <View style={commonStyles.section}>
              <Text style={commonStyles.sectionTitle}>Preference</Text>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.sectionItemText}>Language</Text>
              </View>

              <View style={commonStyles.sectionItem}>
                <Text style={commonStyles.sectionItemText}>Riddle Style</Text>
              </View>
            </View>

            {/* Update Button */}
            <TouchableOpacity 
              style={commonStyles.button}
              // onPress={handleLogin}
              disabled={isProfileUpdated}
            >
              <Text style={commonStyles.buttonText}>
                {'Update'}
              </Text>
            </TouchableOpacity>

            {/* Logout Button */}
            <TouchableOpacity 
              style={commonStyles.registerButton}
              onPress={handleLogout}
              disabled={isLoading}
            >
              <Text style={commonStyles.registerButtonText}>
                {'Logout'}
              </Text>
            </TouchableOpacity>
          </>
        )}

      </ScrollView>
    </SafeAreaView >
  );
}

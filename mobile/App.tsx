import { Ionicons } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import GamePage from './app/game';
import SettingsPage from './app/settings';
import { colors } from './styles/theme';

const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Tab.Navigator
          screenOptions={{
            headerShown: false,
            tabBarStyle: {
              backgroundColor: colors.tabBarBackground,
              // borderTopColor: colors.backgroundSecondary,
              // borderTopWidth: 1,
            },
            tabBarActiveTintColor: colors.tabBarActive,
            tabBarInactiveTintColor: colors.tabBarInactive,
          }}
        >
          <Tab.Screen
            name="Game"
            component={GamePage}
            options={{
              tabBarIcon: ({ color, size }) => <Ionicons name="map" size={size} color={color} />,
            }}
          />
          <Tab.Screen
            name="Settings"
            component={SettingsPage}
            options={{
              tabBarIcon: ({ color, size }) => (
                <Ionicons name="settings" size={size} color={color} />
              ),
            }}
          />
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

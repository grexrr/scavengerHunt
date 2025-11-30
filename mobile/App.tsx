import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import GamePage from './app/game';

export default function App() {
  return (
    <SafeAreaProvider>
      <GamePage/>
    </SafeAreaProvider>
  );
}

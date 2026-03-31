import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import { mapStyles } from '../styles/mapStyles';

type GameStatus = 'initializing' | 'initialized' | 'inRound' | 'finished' | 'error';

interface FloatingActionButtonProps {
  status: GameStatus;
  isLoggedIn: boolean;
  onPress: () => void;
  bottomInset?: number;
}

export default function FloatingActionButton({
  status,
  isLoggedIn,
  onPress,
  bottomInset = 0,
}: FloatingActionButtonProps) {
  const bottomOffset = bottomInset > 0 ? bottomInset + 32 : 32;
  if (!isLoggedIn) {
    return (
      <TouchableOpacity style={[mapStyles.floatingActionButton, { bottom: bottomOffset }]} onPress={onPress}>
        <Text style={mapStyles.floatingActionButtonText}>Login</Text>
      </TouchableOpacity>
    );
  }
  console.log('[FloatingActionButton] Current status:', status);
  const buttonText = status === 'inRound' ? 'Submit Answer' : 'Start Round';

  return (
    <TouchableOpacity style={[mapStyles.floatingActionButton, { bottom: bottomOffset }]} onPress={onPress}>
      <Text style={mapStyles.floatingActionButtonText}>{buttonText}</Text>
    </TouchableOpacity>
  );
}

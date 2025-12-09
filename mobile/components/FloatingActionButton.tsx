import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import { mapStyles } from '../styles/mapStyles';

type GameStatus = 'initializing' | 'initialized' | 'inRound' | 'finished' | 'error';

interface FloatingActionButtonProps {
    status: GameStatus;
    onPress: () => void;
  }

export default function FloatingActionButton({ status, onPress }: FloatingActionButtonProps) {
    console.log('[FloatingActionButton] Current status:', status);
    const buttonText = status === 'inRound' ? 'Submit Answer' : 'Start Round';
  
    return (
      <TouchableOpacity 
        style={mapStyles.floatingActionButton}
        onPress={onPress}
      >
        <Text style={mapStyles.floatingActionButtonText}>
          {buttonText}
        </Text>
      </TouchableOpacity>
    );
  }
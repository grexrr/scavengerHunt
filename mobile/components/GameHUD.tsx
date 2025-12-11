import { Alert, Text, TouchableOpacity, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { mapStyles } from '../styles/mapStyles';

type GameStatus = 'initializing' | 'initialized' | 'inRound' | 'finished' | 'error';

interface GameHUDProps {
  status: GameStatus;
  timeSecondsLeft: number | null;
  attemptsLeft?: number;
  onFinishRound: () => void;
}

export default function GameHud({
  status,
  attemptsLeft,
  timeSecondsLeft,
  onFinishRound,
}: GameHUDProps) {
  const insets = useSafeAreaInsets();

  if (status !== 'inRound') {
    return null;
  }

  const formatTime = (seconds: number | null): string => {
    if (seconds === null || seconds < 0) {
      return '00:00';
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
  };

  const handleFinishRound = () => {
    Alert.alert('Finish Round', 'Are you sure you want to finish this round?', [
      {
        text: 'Cancel',
        style: 'cancel',
      },
      {
        text: 'Finish',
        style: 'destructive',
        onPress: onFinishRound,
      },
    ]);
  };

  return (
    <View style={[mapStyles.gameHUD, { top: insets.top }]}>
      {/* left */}
      <View style={mapStyles.gameHUDLeft}>
        <Text style={mapStyles.countdownText}>{formatTime(timeSecondsLeft)}</Text>
        {attemptsLeft !== undefined && (
          <Text style={mapStyles.attemptsText}>Remaining Attempts: {attemptsLeft}</Text>
        )}
      </View>

      {/* right: finishing round */}
      <TouchableOpacity style={mapStyles.finishRoundButton} onPress={handleFinishRound}>
        <Text style={mapStyles.finishRoundButtonText}>Finish Round</Text>
      </TouchableOpacity>
    </View>
  );
}

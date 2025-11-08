import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function GamePage() {
  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <View style={styles.container}>
        <Text>Game Page</Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

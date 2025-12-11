import { useEffect, useRef, useState } from 'react';
import { Animated, Text, TouchableOpacity, View } from 'react-native';
import { bubbleStyles } from '../styles/bubbleStyles';
import { calculateDistance } from '../utils/calculateDistance';

interface RiddleBubbleProps {
  riddle?: string;
  location: { latitude: number; longitude: number };
  autoCollapseDistance: number;
}

export default function RiddleBubble({
  riddle,
  location,
  autoCollapseDistance = 10,
}: RiddleBubbleProps) {
  const [expanded, setExpanded] = useState(false);
  const prevLocation = useRef<{ latitude: number; longitude: number } | null>(null);
  const scale = useRef(new Animated.Value(1)).current;
  const opacity = useRef(new Animated.Value(1)).current;

  // first time expansion with incoming riddle
  useEffect(() => {
    if (riddle) {
      setExpanded(true);
    }
  }, [riddle]);

  useEffect(() => {
    if (!location || !riddle) return;

    if (prevLocation.current) {
      const dist = calculateDistance(prevLocation.current, location);
      if (dist > autoCollapseDistance) {
        setExpanded(false);
      }
    }
  }, [location, autoCollapseDistance, riddle]);

  useEffect(() => {
    Animated.parallel([
      Animated.spring(scale, {
        toValue: expanded ? 1 : 0.6,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: expanded ? 1 : 0.7,
        duration: 150,
        useNativeDriver: true,
      }),
    ]).start();
  }, [expanded, scale, opacity]);

  if (!riddle) return null;

  return (
    <Animated.View
      style={[
        bubbleStyles.riddleBubble,
        expanded ? bubbleStyles.riddleBubbleExpanded : bubbleStyles.riddleBubbleCollapsed,
        { transform: [{ scale }], opacity },
      ]}
    >
      <TouchableOpacity onPress={() => setExpanded(prev => !prev)} activeOpacity={0.8}>
        <Text style={bubbleStyles.riddleBubbleText}>{expanded ? riddle : '...'}</Text>
        <View style={bubbleStyles.riddleBubbleArrow} />
      </TouchableOpacity>
    </Animated.View>
  );
}

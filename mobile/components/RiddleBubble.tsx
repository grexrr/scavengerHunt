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
  const lastToggleTime = useRef<number>(0);

  // ========== 简单：当 riddle 消失时，执行淡出动画 ==========
  useEffect(() => {
    if (!riddle) {
      // riddle 消失，执行淡出动画
      Animated.parallel([
        Animated.timing(scale, {
          toValue: 0.3,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start();
    } else {
      // 有新的 riddle，重置并展开
      scale.setValue(1);
      opacity.setValue(1);
      setExpanded(true);
    }
  }, [riddle]); // ========== 只依赖 riddle ==========

  useEffect(() => {
    if (!location || !riddle) return;

    const timeSinceToggle = Date.now() - lastToggleTime.current;
    if (timeSinceToggle < 500) {
      return;
    }

    if (prevLocation.current) {
      const dist = calculateDistance(prevLocation.current, location);
      if (dist > autoCollapseDistance) {
        setExpanded(false);
      }
    }
    prevLocation.current = location;
  }, [location, autoCollapseDistance, riddle]);

  useEffect(() => {
    if (!riddle) return; // 没有 riddle 时不执行展开/收起动画

    Animated.parallel([
      Animated.spring(scale, {
        toValue: expanded ? 1 : 0.6,
        useNativeDriver: true,
        tension: 50,
        friction: 7,
      }),
      Animated.timing(opacity, {
        toValue: expanded ? 1 : 0.7,
        duration: 150,
        useNativeDriver: true,
      }),
    ]).start();
  }, [expanded, riddle]); // ========== 只依赖 expanded 和 riddle ==========

  const handleToggle = () => {
    if (!riddle) return;
    
    console.log('[RiddleBubble] Toggle clicked, current expanded:', expanded);
    lastToggleTime.current = Date.now();
    setExpanded(prev => !prev);
  };

  // ========== 简单：没有 riddle 就不显示 ==========
  if (!riddle) return null;

  return (
    <Animated.View
      style={[
        bubbleStyles.riddleBubble,
        expanded ? bubbleStyles.riddleBubbleExpanded : bubbleStyles.riddleBubbleCollapsed,
        { transform: [{ scale }], opacity },
      ]}
      pointerEvents="box-none"
    >
      <TouchableOpacity
        onPress={handleToggle}
        activeOpacity={0.8}
        hitSlop={{ top: 20, bottom: 20, left: 20, right: 20 }}
        style={{
          minWidth: 60,
          minHeight: 40,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Text style={bubbleStyles.riddleBubbleText}>
          {expanded ? riddle : '...'}
        </Text>
        <View style={bubbleStyles.riddleBubbleArrow} />
      </TouchableOpacity>
    </Animated.View>
  );
}
import { StyleSheet } from 'react-native';
import { colors, fontSize, spacing } from './theme';

// Riddle Bubble
export const bubbleStyles = StyleSheet.create({
  riddleBubble: {
    position: 'absolute',
    top: 100, // 简化：HUD 下方。如需贴合 marker 后面再算坐标
    left: 20,
    right: 20,
    alignItems: 'center',
    zIndex: 900,
  },
  riddleBubbleExpanded: {
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderRadius: 12,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderWidth: 1,
    borderColor: colors.backgroundSecondary,
  },
  riddleBubbleCollapsed: {
    backgroundColor: 'rgba(255,255,255,0.8)',
    borderRadius: 12,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderWidth: 1,
    borderColor: colors.backgroundSecondary,
  },
  riddleBubbleText: {
    color: colors.text,
    fontSize: fontSize.md,
    fontWeight: '600',
    textAlign: 'center',
  },
  riddleBubbleArrow: {
    position: 'absolute',
    bottom: -10,
    left: '50%',
    marginLeft: -8,
    width: 0,
    height: 0,
    borderLeftWidth: 8,
    borderRightWidth: 8,
    borderTopWidth: 10,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderTopColor: colors.backgroundSecondary,
  },
});

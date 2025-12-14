import { StyleSheet } from 'react-native';
import { colors, fontSize, spacing } from '../styles/theme';

export const mapStyles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
  // bottomBar: {
  //   position: 'absolute',
  //   bottom: 0,
  //   left: 0,
  //   right: 0,
  //   backgroundColor: colors.tabBarBackground,
  //   paddingHorizontal: spacing.md,
  //   paddingVertical: spacing.sm,
  //   borderTopWidth: 1,
  //   borderTopColor: colors.backgroundSecondary,
  // },

  // Auth Management
  settingsButton: {
    position: 'absolute',
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    zIndex: 1001,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  settingsButtonText: {
    color: colors.text,
    fontSize: fontSize.sm,
    fontWeight: '600',
  },
  closeSettingsButton: {
    position: 'absolute',
    bottom: 20,
    left: 20,
    right: 20,
    backgroundColor: colors.primary,
    padding: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
    zIndex: 1002,
  },
  closeSettingsButtonText: {
    color: colors.background,
    fontSize: fontSize.md,
    fontWeight: '600',
  },

  // startbUT
  // startButton: {
  //   backgroundColor: colors.primary,
  //   padding: spacing.md,
  //   borderRadius: 8,
  //   alignItems: 'center',
  // },
  // startButtonText: {
  //   color: colors.background,
  //   fontSize: 16,
  //   fontWeight: '600',
  // },
  // bottomSheetBackground: {
  //   backgroundColor: colors.background,
  // },
  // bottomSheetContent: {
  //   flex: 1,
  //   padding: spacing.md,
  // },
  // bottomSheetTitle: {
  //   fontSize: 20,
  //   fontWeight: 'bold',
  //   color: colors.text,
  //   marginBottom: spacing.md,
  // },
  // riddleText: {
  //   fontSize: 16,
  //   color: colors.text,
  //   lineHeight: 24,
  // },

  // Start Butt
  floatingActionButton: {
    position: 'absolute',
    bottom: 32, // 距离底部 32px
    alignSelf: 'center', // 水平居中
    width: 100, // 宽度
    height: 100, // 高度（圆形）
    borderRadius: 60, // 圆角半径 = 高度/2，形成圆形
    backgroundColor: colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 4, // Android 阴影
    shadowColor: '#000', // iOS 阴影
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
  },
  floatingActionButtonText: {
    color: colors.background,
    fontSize: fontSize.md,
    fontWeight: '600',
    textAlign: 'center',
  },

  // HUD
  gameHUD: {
    position: 'absolute',
    left: 0,
    right: 0,
    backgroundColor: 'rgba(252, 250, 250, 0.95)', // 半透明背景
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: colors.backgroundSecondary,
    zIndex: 1000, // 确保在最上层
  },
  gameHUDLeft: {
    flex: 1,
    flexDirection: 'column',
  },
  countdownText: {
    fontSize: fontSize.lg,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  attemptsText: {
    fontSize: fontSize.sm,
    color: colors.textSecondary,
  },
  finishRoundButton: {
    backgroundColor: colors.error,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 8,
  },
  finishRoundButtonText: {
    color: colors.background,
    fontSize: fontSize.sm,
    fontWeight: '600',
  },
});

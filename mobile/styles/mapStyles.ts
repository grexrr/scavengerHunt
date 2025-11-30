import { StyleSheet } from 'react-native';
import { colors, spacing } from '../styles/theme';

export const mapStyles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
  bottomBar: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.tabBarBackground,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderTopWidth: 1,
    borderTopColor: colors.backgroundSecondary,
  },
  startButton: {
    backgroundColor: colors.primary,
    padding: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
  },
  startButtonText: {
    color: colors.background,
    fontSize: 16,
    fontWeight: '600',
  },
  bottomSheetBackground: {
    backgroundColor: colors.background,
  },
  bottomSheetContent: {
    flex: 1,
    padding: spacing.md,
  },
  bottomSheetTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.md,
  },
  riddleText: {
    fontSize: 16,
    color: colors.text,
    lineHeight: 24,
  },
  });
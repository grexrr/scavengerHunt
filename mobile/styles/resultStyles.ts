import { StyleSheet } from 'react-native';
import { colors, fontSize, spacing } from './theme';

// Riddle Bubble
export const resultStyles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: colors.background,
    borderRadius: 16,
    padding: spacing.lg,
    width: '80%',
    maxWidth: 400,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 5,
  },
  modalTitle: {
    fontSize: fontSize.lg,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.md,
  },
  modalMessage: {
    fontSize: fontSize.md,
    color: colors.text,
    textAlign: 'center',
    marginBottom: spacing.lg,
    lineHeight: 24,
  },
  modalButton: {
    backgroundColor: colors.primary,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderRadius: 8,
    minWidth: 100,
  },
  modalButtonText: {
    color: colors.background,
    fontSize: fontSize.md,
    fontWeight: '600',
    textAlign: 'center',
  },
});

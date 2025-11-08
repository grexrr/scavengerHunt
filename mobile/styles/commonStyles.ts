import { StyleSheet } from 'react-native';
import { colors, fontSize, spacing } from './theme';

export const commonStyles = StyleSheet.create({
  // 容器样式
  container: {
    flex: 1,
    backgroundColor: colors.background,
    padding: spacing.md,
  },

  // 文本样式
  title: {
    fontSize: fontSize.xl,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.md,
  },

  subtitle: {
    fontSize: fontSize.lg,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.sm,
  },

  bodyText: {
    fontSize: fontSize.md,
    color: colors.text,
    marginBottom: spacing.sm,
  },

  secondaryText: {
    fontSize: fontSize.sm,
    color: colors.textSecondary,
  },

  // 分组样式
  section: {
    marginBottom: spacing.xl,
  },

  sectionTitle: {
    fontSize: fontSize.md,
    fontWeight: '600',
    color: colors.text,
    backgroundColor: colors.backgroundSecondary,
    padding: spacing.sm,
    marginBottom: spacing.md,
    marginTop: spacing.sm,
    marginLeft: 0,
    marginRight: 0,
  },

  sectionItem: {
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.sm,
    backgroundColor: colors.background,
    borderRadius: 8,
    marginBottom: spacing.sm,
  },

  sectionItemText: {
    fontSize: fontSize.md,
    color: colors.text,
  },

  input: {
    borderWidth: 1,
    borderColor: colors.backgroundSecondary,
    borderRadius: 8,
    padding: spacing.md,
    fontSize: fontSize.md,
    backgroundColor: colors.background,
    marginBottom: spacing.md,
  },
  
  button: {
    backgroundColor: colors.primary,
    padding: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: spacing.sm,
  },

  buttonText: {
    color: colors.background,
    fontSize: fontSize.md,
    fontWeight: '600',
  },

  registerButton: {
    backgroundColor: '#F0F0F0',
    color: '#000000',
    padding: spacing.md,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: spacing.sm,
  },

  registerButtonText: {
    color: colors.text,  
    fontSize: fontSize.md,
    fontWeight: '600',
  },
  

});

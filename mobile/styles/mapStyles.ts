import { StyleSheet } from 'react-native';

export const mapStyles = StyleSheet.create({
    container: {
      flex: 1,
      padding: 20,
      alignItems: 'center',
    },
    map: {
      flex: 1,
    },
    title: {
      fontSize: 24,
      fontWeight: 'bold',
      marginBottom: 30,
      marginTop: 20,
    },
    statusContainer: {
      marginBottom: 20,
      padding: 15,
      backgroundColor: '#e3f2fd',
      borderRadius: 10,
      width: '100%',
      alignItems: 'center',
    },
    statusText: {
      fontSize: 16,
      fontWeight: 'bold',
      marginBottom: 10,
      color: '#1976d2',
    },
    buttonRow: {
      flexDirection: 'row',
      width: '100%',
      justifyContent: 'center',
    },
    buttonSpacer: {
      width: 10,
    },
    errorContainer: {
      marginBottom: 20,
      padding: 15,
      backgroundColor: '#ffebee',
      borderRadius: 10,
      width: '100%',
    },
    errorText: {
      fontSize: 14,
      color: '#c62828',
      textAlign: 'center',
    },
    infoContainer: {
      marginTop: 20,
      padding: 20,
      backgroundColor: '#f0f0f0',
      borderRadius: 10,
      width: '100%',
    },
    sectionTitle: {
      fontSize: 18,
      fontWeight: 'bold',
      marginBottom: 15,
      color: '#333',
    },
    label: {
      fontSize: 14,
      color: '#666',
      marginTop: 10,
    },
    value: {
      fontSize: 18,
      fontWeight: 'bold',
      color: '#000',
      marginTop: 5,
    },
    headingValue: {
      fontSize: 32,
      fontWeight: 'bold',
      color: '#007AFF',
      marginTop: 5,
      textAlign: 'center',
    },
    directionText: {
      fontSize: 20,
      fontWeight: 'bold',
      color: '#007AFF',
      marginTop: 10,
      textAlign: 'center',
    },
  });
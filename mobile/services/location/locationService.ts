import * as Location from 'expo-location';

export interface Position {
    latitude: number;
    longitude: number;
    accuracy: number | null
}

export async function requestPermissions(): Promise<boolean> {
    try {
        const { status } = await Location.requestForegroundPermissionsAsync();
        return status === 'granted';
    } catch (error) {
        console.error('Error requesting location permissions:', error);
        return false;
    } 
}

export async function getCurrentPosition(): Promise<Position | null> {
    try{
        const hasPermission = await requestPermissions();
        if (!hasPermission) {
            console.warn('User refuse location permission!')
            return null;
        }

        const location = await Location.getCurrentPositionAsync({
            accuracy: Location.Accuracy.High
        });

        return {
            latitude: location.coords.latitude,
            longitude: location.coords.longitude,
            accuracy: location.coords.accuracy,
        };
    } catch (error) {
        console.error('Error acquiring position:', error);
        return null;
    }
}
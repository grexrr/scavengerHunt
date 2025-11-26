import * as Location from 'expo-location';
import { Magnetometer } from 'expo-sensors';

export interface Position {
    latitude: number;
    longitude: number;
    accuracy: number | null;
}

export interface Heading {
    heading: number;  // 0-360 度（0 = 正北）
    accuracy: number | null;
}

// ==================== Permission ====================
    
export async function requestPermissions(): Promise<boolean> {
    try {
        const { status } = await Location.requestForegroundPermissionsAsync();
        return status === 'granted';
    } catch (error) {
        console.error('Error requesting location permissions:', error);
        return false;
    }
}

// ==================== Location ====================

export async function getCurrentPosition(): Promise<Position | null> {
    try {
        const hasPermission = await requestPermissions();
        if (!hasPermission) {
            console.warn('User refuse location permission!');
            return null;
        }

        const location = await Location.getCurrentPositionAsync({
            accuracy: Location.Accuracy.High,
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

export function watchPosition(
    callback: (position: Position) => void,
    options?: {
        accuracy?: Location.Accuracy;
        timeInterval?: number;
        distanceInterval?: number;
    }
): () => void {
    let subscription: Location.LocationSubscription | null = null;

    requestPermissions().then(async (hasPermission) => {
        if (hasPermission) {
            subscription = await Location.watchPositionAsync(
                {
                    accuracy: options?.accuracy || Location.Accuracy.High,
                    timeInterval: options?.timeInterval || 1000,
                    distanceInterval: options?.distanceInterval || 0,
                },
                (location) => {
                    callback({
                        latitude: location.coords.latitude,
                        longitude: location.coords.longitude,
                        accuracy: location.coords.accuracy,
                    });
                },
                (error) => {
                    console.error('Failed to watch position:', error);
                }
            );
        }
    });

    return () => {
        if (subscription) {
            subscription.remove();
        }
    };
}

// ==================== Bearing ====================
export async function getCurrentHeading(): Promise<Heading | null> {
    try {

        const hasPermission = await requestPermissions();
        if (!hasPermission) {
            console.warn('Location permissions not granted');
            return null;
        }

        const headingData = await Location.getHeadingAsync();
        if (headingData && headingData.magHeading !== null && headingData.magHeading >= 0) {
            return {
                heading: headingData.magHeading, 
                accuracy: headingData.accuracy,
            };
        }

        const available = await Magnetometer.isAvailableAsync();
        if (available) {
            return new Promise((resolve) => {
                Magnetometer.setUpdateInterval(100);
                const subscription = Magnetometer.addListener((data) => {
                    subscription.remove();
                    const { x, y } = data;
                    let angle = Math.atan2(y, x) * (180 / Math.PI);
                    if (angle < 0) {
                        angle += 360;
                    }
                    const heading = (90 - angle + 360) % 360;
                    resolve({
                        heading: heading,
                        accuracy: null,
                    });
                });
            });
        }

        return null;
    } catch (error) {
        console.error('Failed to get heading:', error);
        return null;
    }
}


export function watchHeading(
    callback: (heading: Heading) => void
): () => void {
    let subscription: Location.LocationSubscription | null = null;
    let isUnsubscribed = false;

    (async () => {
        try {
            // ✅ 优先使用 expo-location（更准确，返回设备朝向）
            const hasPermission = await requestPermissions();
            if (!hasPermission || isUnsubscribed) return;

            subscription = await Location.watchHeadingAsync(
                (headingData) => {
                    if (isUnsubscribed) return;
                    if (headingData && headingData.magHeading !== null && headingData.magHeading >= 0) {
                        callback({
                            heading: headingData.magHeading,  // ✅ 直接使用，已经是地理方向（0度=正北）
                            accuracy: headingData.accuracy,
                        });
                    }
                },
                (error) => {
                    console.error('Failed to watch heading:', error);
                }
            );
        } catch (error) {
            console.error('Error setting up heading watch:', error);
            
            // 降级到 Magnetometer（如果 expo-location 失败）
            try {
                const available = await Magnetometer.isAvailableAsync();
                if (available && !isUnsubscribed) {
                    Magnetometer.setUpdateInterval(100);
                    subscription = Magnetometer.addListener((data) => {
                        if (isUnsubscribed) return;
                        const { x, y } = data;
                        let angle = Math.atan2(y, x) * (180 / Math.PI);
                        if (angle < 0) angle += 360;
                        const heading = (90 - angle + 360) % 360;
                        callback({
                            heading: heading,
                            accuracy: null,
                        });
                    });
                }
            } catch (magnetError) {
                console.error('Magnetometer also failed:', magnetError);
            }
        }
    })();

    return () => {
        isUnsubscribed = true;
        if (subscription) {
            if (subscription.remove) {
                subscription.remove();
            }
            subscription = null;
        }
    };
}
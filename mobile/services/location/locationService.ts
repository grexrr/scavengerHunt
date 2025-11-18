import * as Location from 'expo-location';

export interface Position {
    latitude: number;
    longitude: number;
    accuracy: number | null;
}

export interface Heading {
    heading: number;  // 0-360 度（0 = 正北）
    accuracy: number | null;
}

// ==================== 权限管理 ====================

export async function requestPermissions(): Promise<boolean> {
    try {
        const { status } = await Location.requestForegroundPermissionsAsync();
        return status === 'granted';
    } catch (error) {
        console.error('Error requesting location permissions:', error);
        return false;
    }
}

// ==================== 位置相关 ====================

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
                    timeInterval: options?.timeInterval || 1000,  // 默认每1秒更新
                    distanceInterval: options?.distanceInterval || 0,  // 默认不限制距离
                },
                // callback - 位置更新时调用
                (location) => {
                    callback({
                        latitude: location.coords.latitude,
                        longitude: location.coords.longitude,
                        accuracy: location.coords.accuracy,
                    });
                },
                // errorHandler - 错误时调用
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

// ==================== 朝向相关 ====================

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
                heading: Math.round(headingData.magHeading),
                accuracy: headingData.accuracy,
            };
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

    requestPermissions().then(async (hasPermission) => {
        if (hasPermission) {
            subscription = await Location.watchHeadingAsync(
                // callback - 朝向更新时调用
                (headingData) => {
                    if (headingData && headingData.magHeading !== null && headingData.magHeading >= 0) {
                        callback({
                            heading: Math.round(headingData.magHeading),
                            accuracy: headingData.accuracy,
                        });
                    }
                },
                (error) => {
                    console.error('Failed to watch heading:', error);
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

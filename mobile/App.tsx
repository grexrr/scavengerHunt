import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import GamePage from './app/game';
import SettingsPage from './app/settings';

const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Tab.Navigator>
        <Tab.Screen 
          name="Game" 
          component={GamePage} 
        />
        <Tab.Screen 
          name="Settings" 
          component={SettingsPage} 
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
}
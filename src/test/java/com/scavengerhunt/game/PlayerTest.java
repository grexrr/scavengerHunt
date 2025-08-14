package com.scavengerhunt.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.scavengerhunt.model.Player;

public class PlayerTest {

    @Test
    void testPlayerInitialization() {
        Player player = new Player(51.0, -8.5, 90.0);

        assertEquals(51.0, player.getLatitude());
        assertEquals(-8.5, player.getLongitude());
        assertEquals(90.0, player.getAngle());
    }

    @Test
    void testSettersAndGetters() {
        Player player = new Player(0, 0, 0);
        player.setLatitude(52.0);
        player.setLongitude(-9.0);
        player.setAngle(45.0);
        player.setPlayerId("123");
        player.setPlayerNickname("TestPlayer");

        assertEquals(52.0, player.getLatitude());
        assertEquals(-9.0, player.getLongitude());
        assertEquals(45.0, player.getAngle());
        assertEquals(123, player.getPlayerId());
        assertEquals("TestPlayer", player.getPlayerNickname());
    }
    
}

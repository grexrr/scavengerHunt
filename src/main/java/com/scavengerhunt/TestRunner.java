// game.initGame(51.8954396, -8.4890143, 0); //init player, 

package com.scavengerhunt;

import java.util.Scanner;

import com.scavengerhunt.ui.UIController;

public class TestRunner {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        UIController ui = new UIController();
        ui.initGame(51.8954396, -8.4890143, 0); // UCC Entrance

        System.out.println("Input Search Radius: ");
        double radius = scanner.nextDouble();
        ui.startNewRound(radius);

        while (true) {
            System.out.println("Submit Answer?(Y/N):");
            String cmd = scanner.next();
            if (cmd.equalsIgnoreCase("Y")) {
                ui.submitAnswer();
            } else {
                break;
            }
        }
        scanner.close();
    }
}
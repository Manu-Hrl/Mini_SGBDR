package src;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        DBManager.getInstance().init();

        DBParams.DBPath = args[0];
        DBParams.pageSize = 4096;
        DBParams.frameCount = 2;
        
        Scanner sc = new Scanner(System.in);
        String command = "";

        do {
            System.out.println("Entrez votre commande : ");
            command = sc.nextLine();

            switch (command) {
                case "EXIT":
                    sc.close(); //Ferme le flux d'entrer clavier
                    DBManager.getInstance().finish(); //Fin de l'exécution du DBManager
                    System.out.println("Arrêt du programme ! Au revoir :)");
                    break;
            
                default:
                    DBManager.getInstance().processCommand(command);
                    break;
            }
        }while(!command.equals("EXIT"));
    }
}

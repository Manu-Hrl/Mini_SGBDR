package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public final class DBManager {

    private static DBManager INSTANCE; // Instance unique du DBManager
    ArrayList<Enregistrement> Enregistrements;

    private DBManager() {
        Enregistrements = new ArrayList<Enregistrement>();
    }

    /**
     * Permet d'obtenir l'unique instance de DBManager
     * 
     * @return retourne l'instance du DBManager
     */
    public static DBManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DBManager();
        }

        return INSTANCE;
    }

    /**
     * Initialise le DBManager
     */
    public void init() {
        DBInfo.getInstance().init();
        FileManager.getInstance().init(); // Apres le DBInfo car de potentiels relations sont en mémoire HDD et doivent
                                          // se charger dans le DBInfo
    }

    /**
     * Désactive le DBManager
     */
    public void finish() {
        DBInfo.getInstance().finish();
        BufferManager.getInstance().flushAll();
    }

    /**
     * Crée une relation et l'insert dans la DBInfo
     * 
     * @param nomRel   Nom de la relation
     * @param nbCol    Nombre de colonnes de la relation
     * @param nomCols  Nom des colonnes de la reation
     * @param typeCols Type des colonnes de la relation
     */
    private void createRelation(String nomRel, int nbCol, ArrayList<String> nomCols, ArrayList<String> typeCols) {
        int fileIdx = DBInfo.getInstance().getNbRel();
        int recordSize = calculrecordSize(typeCols);
        int slotCount = calculSlotCount(recordSize);


        RelationInfo relation = new RelationInfo(nomRel, nbCol, nomCols, typeCols, fileIdx, recordSize, slotCount);
        DBInfo.getInstance().addRelation(relation);
        FileManager.getInstance().createRelationFile(relation);
    }

    /**
     * Calcul le recordSize (taille en octets d’un record)
     * 
     * @param typeCols type des colonnes de la relation
     * @return retourne la taille du recordSize
     */
    private int calculrecordSize(ArrayList<String> typeCols) {
        int tailleRecord = 0;

        for (String string : typeCols) {
            if (string.equals("int") || string.equals("float")) {
                tailleRecord += Integer.BYTES;
            } else if (string.contains("string")) {
                try {
                    int longueurString = Integer.parseInt(string.substring(6));
                    tailleRecord += longueurString*Character.BYTES;
                } catch (NumberFormatException e) {
                    System.err.println(
                            "Ce qui est écrit à la suite de " + string + " n'est pas un nombre ! Fin du programme");
                    System.exit(0);
                }
            } else {
                System.err.println(string + " n'est pas un type valide ! Fin du programme");
                System.exit(0);
            }
        }
        
        tailleRecord *= 2;
        return tailleRecord;
    }

    /**
     * Calcul le nombre de slots sur une page. On divise la taille de la page par la
     * taille d'un record et on ajoute 1 pour sauvegarder le bytemap
     * 
     * @param recordSize taille en octets d’un record
     * @return retourne nb slots sur une page
     */
    private int calculSlotCount(int recordSize) {
        return (DBParams.pageSize / recordSize) + 1;
    }

    /**
     * Insère le record dans la relation voulu
     * 
     * @param nomRel  nom de la relation
     * @param valeurs valeurs à insérer
     */
    private void insertRecord(String nomRel, String valeurs) {
        ArrayList<String> listValeurs = new ArrayList<String>(Arrays.asList(valeurs.split(",")));

        ArrayList<RelationInfo> listRelation = DBInfo.getInstance().getListRels();

        for (RelationInfo relationInfo : listRelation) {
            if (relationInfo.getNomRel().equals(nomRel)) {
                Record record = new Record(relationInfo, listValeurs);
                Rid rid = FileManager.getInstance().insertRecordInRelation(record, nomRel);
                Enregistrement e = new Enregistrement(record, rid);
                Enregistrements.add(e); 
                return;
            }
        }
        
        System.out.println("Le record n'a pas été inséré car la relation n'existe pas");
    }

    /**
     * Demande l’insertion de plusieurs records dans une relation à partir d'un fichier .csv
     * 
     * @param nomRel    nom de la relation où l'on veut insérer les records
     * @param nomficher nom du fichier csv
     */
    private void batchInsert(String nomRel, String nomficher) {
        try (FileReader csvReader = new FileReader("./" + nomficher); BufferedReader csvBR = new BufferedReader(csvReader)) {
            String record;
            while ((record = csvBR.readLine()) != null) {
                insertRecord(nomRel, record);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Le ficher "+nomficher+" n'existe pas !");
        } catch (IOException e) {
            System.err.println("Problème d'E/S sur le fichier "+nomficher);
        }
    }

    /**
     * Affiche tous les records d’une relation (1 record par ligne), ainsi que le total de record(s)
     * 
     * @param nomRel nom de la relation
     */
    private void selectAll(String nomRel) {
        ArrayList<Record> records = FileManager.getInstance().selectAllFromRelation(nomRel);

        if (records != null) {
            int compteur = 0;

            for (Record record : records) {
                System.out.println(record.toString());
                compteur++;
            }
            
            System.out.println("Total records="+compteur);
        } else {
            System.out.println("La relation "+nomRel+" N'existe pas");
        }
    }

    /**
     * Affiche tous les records d’une relation qui ont une valeur donnée sur une colonne donnée
     * 
     * @param nomRel nom de la relation
     * @param nomColonne nom de la colonne
     * @param valeur valeur que l'on veut pour la colonne
     */
    private void selectS(String nomRel, String nomColonne, String valeur) {
        ArrayList<Record> records = FileManager.getInstance().selectAllFromRelation(nomRel);

        /**
         * On implémente un compte à partir du 1er record (car tous les record ont le même schéma de relation)
         * donc on peut faire records.get(0)
         */
        int indiceCol = 0;
        for (String nomCol : records.get(0).getRelInfo().getNomCols()) {
            indiceCol++;
            if (nomCol.equals(nomColonne)) {
                break;
            }
        }

        int compteur = 0;
        for (Record record : records) {
            if (record.getValues().get(indiceCol-1).equals(valeur)) {
                compteur++;
                System.out.println(record.toString());
            }
        }

        System.out.println("Total records="+compteur);
    }

    /**
     * Met à jour des tuples selon une certaine condition
     * 
     * @param nomRel nom de la relation
     * @param set nom colonne que l'on veut modifier et sa nouvelle valeur 
     * @param where condition pour que l'on modifie un tuple
     */
    public void update(String nomRel, String set, String where) {
        String [] tabSet = set.split("=");
        String [] tabWhere = where.split("=");

        String nomColSet = tabSet[0];
        String valSet = tabSet[1];

        String nomColWhere = tabWhere[0];
        String valWhere = tabWhere[1];

        ArrayList<Record> records = FileManager.getInstance().selectAllFromRelation(nomRel);

        int indiceColWhere = 0;
        for (String nomColonne: records.get(0).getRelInfo().getNomCols()) {
            indiceColWhere++;
            if (nomColonne.equals(nomColWhere)) {
                break;
            }
        }

        int compteur = 0;
        
        for (Record record : records) {
            if (record.getValues().get(indiceColWhere-1).equals(valWhere)) {
                int indiceColSet = 0;
                for (String nomColonne: record.getRelInfo().getNomCols()) {
                    if (nomColonne.equals(nomColSet)) {
                        compteur++;
                        
                        for (Enregistrement e : Enregistrements) {
                            if (e.getRecord().equals(record)) {
                                record.getValues().set(indiceColSet, valSet);
                                e.setRecord(record);
                                ByteBuffer bufferPage = BufferManager.getInstance().getPage(e.getRid().getPageId());
                                int position = e.getRid().getSlotIdx() * e.getRecord().getRelInfo().getRecordSize() + e.getRecord().getRelInfo().getSlotCount();
                                e.getRecord().writeToBuffer(bufferPage, position);
                                BufferManager.getInstance().freePage(e.getRid().getPageId(), true);
                            }
                        }
                        break;
                    }
                    indiceColSet++;
                }
            }
        }
        
        System.out.println("Total records update = "+compteur);
    }

    /**
     * Gère les différentes commandes du LDD (langage de définition de données) et LMD (langage de manipulation de données)
     * 
     * @param command Commande de l'utilisateur
     */
    public void processCommand(String command) {
        String [] splitCommand = command.split(" ");

        switch (splitCommand[0]) {

            case "CREATEREL":
                String nomRel = splitCommand[1];
                int nbCol = 0;
                ArrayList <String> nomCols = new ArrayList<String>();
                ArrayList <String> typeCols = new ArrayList<String>();

                for (String string : splitCommand) { //Ajoute le nom et le type des colonnes + calcul le nb de colonnes
                    if ((!string.equals("CREATEREL")) && (!string.equals(nomRel))) {
                        String [] splitNomTypeCols = string.split(":");
                        nomCols.add(splitNomTypeCols[0]);
                        typeCols.add(splitNomTypeCols[1]);
                        nbCol++;
                    }
                }

                createRelation(nomRel, nbCol, nomCols, typeCols);
                break;
            case "INSERT":
                String nomRelInsert = splitCommand[2]; //Position 2 car à l'indice 1 on a "INTO"

                for (String string : splitCommand) {
                    if (string.startsWith("(") && string.endsWith(")")) {
                        String valeurs = string.substring(1, string.length() - 1);

                        insertRecord(nomRelInsert, valeurs);
                    }
                }
                break;
            case "SELECTALL":
                String nomRelSelectAll = splitCommand[2]; //Position 2 car à l'indice 1 on a "FROM"
                selectAll(nomRelSelectAll);
                break;
            case "SELECTS":
                String nomRelSelectS = splitCommand[2]; //Position 2 car à l'indice 1 on a "FROM"

                for (String string : splitCommand) {
                    if (string.contains("=")) {
                        String [] where = string.split("=");

                        selectS(nomRelSelectS, where[0], where[1]);
                    }
                }
                break;
            case "BATCHINSERT":
                String nomRelBatch = splitCommand[2]; //Position 2 car à l'indice 1 on a "INTO"

                for (String string : splitCommand) {
                    if (string.contains(".csv")) {
                        batchInsert(nomRelBatch, string);
                    }
                }
                break;
            case "SELECTC":
                break;
            case "UPDATE":
                String nomRelUpdate = splitCommand[1];
                String set = splitCommand[3];
                String where = splitCommand[5];

                update(nomRelUpdate, set, where);
                break;
            case "RESET":
                BufferManager.getInstance().reset();
                DBInfo.getInstance().reset();
                FileManager.getInstance().reset();

                break;
            default:
                System.out.println("Mauvaise commande ! Recommencez s'il vous plait");
                break;
        }
    }
}
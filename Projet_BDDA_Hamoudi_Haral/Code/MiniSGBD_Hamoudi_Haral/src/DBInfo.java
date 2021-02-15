package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public final class DBInfo implements Serializable {

    /**
     * clé de hachage SHA qui identifie de manière unique la classe DBInfo
     */
    private static final long serialVersionUID = 1L;

    private static DBInfo INSTANCE;
    private int nbRel;
    private ArrayList <RelationInfo> listRels;

    private DBInfo() {
        this.nbRel = 0;
        this.listRels = new ArrayList <RelationInfo> ();
    }

    /**
     * Permet d'obtenir l'unique instance de DBInfo
     * 
     * @return retourne l'instance du DBInfo
     */
    public static DBInfo getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DBInfo();
        }

        return INSTANCE;
    }
    
    /**
     * Initialise le DBInfo en récupérant les données du Catalog.def
     */
    public void init() {
        String pathnameString = "./DB/Catalog.def";
        File f = new File(pathnameString);

        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                INSTANCE = (DBInfo) ois.readObject();
            } catch (ClassNotFoundException e) {
                System.err.println("L'objet DBInfo n'a pas été retrouvée à l'initialisation");
            } catch (NullPointerException e) {
                System.err.println("Le fichier n'a pas été trouvé l'initialisation de DBInfo");
            } catch (InvalidClassException e) {
                System.err.println("Problème de sérialisation à l'initialisation de DBInfo");
            } catch (IOException e) {
                System.err.println("Erreur d'E/S lors de l'initialisation de DBInfo");
            }
        }
    }

    /**
     * Désactive le DBInfo et enregistre la DBInfo dans Catalog.def
     */
    public void finish() {
        String pathnameString = "./DB/Catalog.def";
        File f = new File(pathnameString);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(INSTANCE);
        } catch (NullPointerException e) {
			System.err.println("Le fichier n'a pas été trouvé à l'arrêt de DBInfo");
        } catch (InvalidClassException e) {
            System.err.println("Problème de sérialisation à l'arrêt de DBInfo");
        } catch (IOException e) {
			System.err.println("Erreur d'E/S lors de l'arrêt de DBInfo");
		}
    }

    /**
     * Ajoute une relation dans la liste de relation et incrémente le nombre de relation
     * 
     * @param relation Relation (de type modèle relationnel)
     */
    public void addRelation(RelationInfo relation) {
        listRels.add(relation);
        nbRel++;
    }

    /**
     * Getter de la liste de relations
     * 
     * @return retourne la liste de relations
     */
    public ArrayList<RelationInfo> getListRels() {
        return listRels;
    }

    /**
     * Getter du nombre de relation
     * 
     * @return retourne le nb de relation
     */
    public int getNbRel() {
        return nbRel;
    }

    /**
     * Réinitialise la DBInfo :
     * - Supprime le fichier Catalog.def et tous les fichiers .rf
     * - Supprime toutes les relations
     */
    public void reset() {
        String pathnameString = "./DB/";
        File file = new File(pathnameString);

        for (File f : file.listFiles()) {
            f.delete();
        }

        nbRel = 0;
        listRels = new ArrayList <RelationInfo> ();
    }
}

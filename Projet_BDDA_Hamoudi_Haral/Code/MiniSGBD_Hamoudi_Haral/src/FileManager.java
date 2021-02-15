package src;

import java.util.ArrayList;

public class FileManager {
    private ArrayList<HeapFile> heapFiles;
    private static FileManager INSTANCE;

    /**
     * Construit le File Manager et initialise la liste de Heap File
     */
    private FileManager() {
        heapFiles = new ArrayList<HeapFile>();
    }

    /**
     * Permet d'obtenir l'unique instance du File Manager
     * 
     * @return retourne l'instance du File Manager
     */
    public static FileManager getInstance() {
        if (INSTANCE == null){
            INSTANCE = new FileManager();
        }

        return INSTANCE;
    }


    /**
     * Initialise le File Manager (« point d’entrée » de la couche Accès/Fichiers)
     */
    public void init() {
        ArrayList<RelationInfo> listeRelations = DBInfo.getInstance().getListRels();

        /**
         * Parcourir la liste des RelationInfo du DBInfo
         */
        for (RelationInfo relationInfo : listeRelations) {
            heapFiles.add(new HeapFile(relationInfo)); //On crée un Heap File associé à une relation et on l'ajoute à la liste de Heap File
        }
    }

    /**
     * Permet la création d'une relation avec son Heap File
     * 
     * @param relInfo une relation
     */
    public void createRelationFile(RelationInfo relInfo) {
        HeapFile heapFile = new HeapFile(relInfo);
        heapFile.createNewOnDisk();
        heapFiles.add(heapFile);
    }

    /**
     * Insertion d’un Record dans une relation
     * 
     * @param record record à insérer
     * @param relName nom de la relation dans laquelle on veut insérer le record
     * @return retourne le RID du record, null si la relation ne fait pas parti de la liste des Heap File
     */
    public Rid insertRecordInRelation(Record record, String relName) {
        for (HeapFile heapFile : heapFiles) {
            if (heapFile.getRelInfo().getNomRel().equals(relName)) {
                return heapFile.insertRecord(record);
            }
        }

        return null;
    }

    /**
     * Liste tous les records d’une relation (« sélection sans conditions »)
     * 
     * @param relName nom de la relation pour laquelle on veut connaître ses records (tuples)
     * @return retourne une liste de records, null si la relation n'existe pas
     */
    public ArrayList<Record> selectAllFromRelation(String relName) {
        for (HeapFile heapFile : heapFiles) {
            if (heapFile.getRelInfo().getNomRel().equals(relName)) {
                return heapFile.getAllRecords();
            }
        }

        return null;
    }

    /**
     * Accesseur de la liste de Heap File
     * 
     * @return retourne la liste de Heap File
     */
    public ArrayList<HeapFile> getHeapFiles() {
        return heapFiles;
    }

    /**
     * Réinitialise le File Manager :
     * - Supprime les Heap File ainsi que les pages associées
     */
    public void reset() {
        heapFiles = new ArrayList<HeapFile>();
    }
}

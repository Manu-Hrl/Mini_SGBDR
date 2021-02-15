package src;

import java.io.Serializable;
import java.util.ArrayList;

public class RelationInfo implements Serializable {
    
    /**
     * clé de hachage SHA qui identifie de manière unique la classe RelationInfo
     */
    private static final long serialVersionUID = 1L;

    private String nomRel;
    private int nbCols;
    private ArrayList <String> nomCols;
    private ArrayList <String> typeCols; //Type restreint à "int" "float" "stringT", où T=longeur du string

    private int fileIdx;
    private int recordSize;
    private int slotCount;

    /**
     * Crée une nouvelle relation 
     * 
     * @param nomRel nom de la relation
     * @param nbCols nombre de colonne(s)
     * @param nomCols nom de colonne(s)
     * @param typeCols liste du type de(s) colonne(s)
     * @param fileIdx indice du fichier disque qui stocke la relation
     * @param recordSize  représente la taille d’un record
     * @param slotCount représente le nombre de cases (slots) sur une page
     */
    public RelationInfo(String nomRel, int nbCols,ArrayList <String> nomCols, ArrayList <String> typeCols, int fileIdx, int recordSize, int slotCount) {
        this.nomRel=nomRel;
        this.nbCols=nbCols;
        this.nomCols=nomCols;
        this.typeCols=typeCols; //Mieux d'utiliser un ArrayList que tableau car on peut modifier le nb de colonne par la suite
        this.fileIdx = fileIdx;
        this.recordSize = recordSize;
        this.slotCount = slotCount;
    }

    /**
     * Crée une relation et initialise automatiquement recordSize à 500 et slotCount à 9.
     * Le fileIdx est géré automatiquement par le nb de relation dans DBInfo
     * 
     * @param nomRel nom de la relation
     * @param nbCols nombre de colonne(s)
     * @param nomCols nom de colonne(s)
     * @param typeCols iste du type de(s) colonne(s)
     */
    public RelationInfo(String nomRel, int nbCols,ArrayList <String> nomCols, ArrayList <String> typeCols) {
        this(nomRel, nbCols, nomCols, typeCols, DBInfo.getInstance().getNbRel(), 500, 9);
    }

    /**
     * Accesseur du nom de la relation
     * 
     * @return retourne le nom de la relation
     */
    public String getNomRel() {
        return nomRel;
    }

    /**
     * Accesseur du nombre de colonnes
     * 
     * @return retourne le nombre de colonnes
     */
    public int getNbCols() {
        return nbCols;
    }

    /**
     * Accesseur de la liste de type des colonnes
     * 
     * @return retourne la liste de type des colonnes
     */
    public ArrayList<String> getTypeCols() {
        return typeCols;
    }

    /**
     * Accesseur de l'identifiant fichier
     * 
     * @return retourne le fileIdx
     */
    public int getFileIdx() {
        return fileIdx;
    }

    /**
     * Accesseur du nombre de case(s) (slot(s))
     * 
     * @return retourne le nb de case(s)
     */
    public int getSlotCount() {
        return slotCount;
    }

    /**
     * Accesseur de la taille d'un record
     * 
     * @return retourne la taille d'un record
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * Accesseur du nom des colonnes
     * 
     * @return returne la liste du nom des colonnes
     */
    public ArrayList<String> getNomCols() {
        return nomCols;
    }

    @Override
    public boolean equals(Object obj) {
        RelationInfo relInfo = (RelationInfo) obj;

        if (this == relInfo) {
            return true;
        }

        for (int i = 0; i < nbCols; i++) {
            if ( (!nomCols.get(i).equals(relInfo.getNomCols().get(i))) || (!typeCols.get(i).equals(relInfo.getTypeCols().get(i)))) {
                return false;
            }
        }

        return (nomRel.equals(relInfo.getNomRel()) && (nbCols==relInfo.getNbCols()) && (fileIdx==relInfo.getFileIdx()) && (recordSize==relInfo.getRecordSize()) && (slotCount==relInfo.getSlotCount()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(nomRel+"\n");
        sb.append(nbCols+"\n");
        
        for (int i = 0; i < nomCols.size(); i++) {
            sb.append(nomCols.get(i)+":"+typeCols.get(i)+" ");
        }

        return sb.toString();
    }
}

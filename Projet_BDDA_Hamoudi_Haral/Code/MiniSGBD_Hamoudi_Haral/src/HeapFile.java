package src;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HeapFile {
    private RelationInfo relInfo;

    /**
     * Construit un Heap File pour une relation donnée
     * 
     * @param relInfo relation à lié avec un Heap File
     */
    public HeapFile(RelationInfo relInfo) {
        this.relInfo = relInfo;
    }

    /**
     * Gère la création de fichier disque correspondant au Heap File
     */
    public void createNewOnDisk() {
        int fileIdx = relInfo.getFileIdx();
		DiskManager.createFile(fileIdx);
        PageId newPageId = DiskManager.addPage(fileIdx);

        ByteBuffer bufferHeaderPage = BufferManager.getInstance().getPage(newPageId);
        
        if (bufferHeaderPage != null) {
            for (int i = 0; i < bufferHeaderPage.capacity(); i += Integer.BYTES) {
                bufferHeaderPage.putInt(i, 0);
            }
        }

        BufferManager.getInstance().freePage(newPageId, true);
    }

    /**
     * Rajoute une page de données et renvoie la page ajoutée
     * 
     * @return retourne la page ajoutée au fichier
     */
    private PageId addDataPage() {
        int fileIdx = relInfo.getFileIdx();
        PageId newPageId = DiskManager.addPage(fileIdx);

        PageId headerPage = new PageId(fileIdx, 0);
        ByteBuffer bufferHeaderPage = BufferManager.getInstance().getPage(headerPage);
        bufferHeaderPage.putInt(0,bufferHeaderPage.getInt(0)+1); //entier N, correspondant au nombre de pages de données dans le fichier, mis à l'indice 0 de la Header Page

        bufferHeaderPage.putInt(bufferHeaderPage.getInt(0) * Integer.BYTES, relInfo.getSlotCount()); // Ajout du nombre de slot dispo au dernière indice de la Header Page
        BufferManager.getInstance().freePage(headerPage, true);
        
        /**
         * Insertion des 0 (correspondant à la bytemap)
         * 0 étant slot libre
         * 1 étant slot occupé
         */
        ByteBuffer bufferPage = BufferManager.getInstance().getPage(newPageId);
        byte slotLibre = 0;
        for (int i = 0; i < relInfo.getSlotCount(); i += Byte.BYTES) {
            bufferPage.put(i, slotLibre);
        }
        BufferManager.getInstance().freePage(newPageId, true);

        return newPageId;
    }

    /**
     * Trouve le  PageId d'une page de données sur laquelle il reste des cases libres
     * 
     * @return retourne le pageId de la page de données libre, null si aucune page n'est libre
     */
    private PageId getFreeDataPageId() {
        int fileIdx = relInfo.getFileIdx();
        PageId headerPage = new PageId(fileIdx, 0);
        ByteBuffer bufferHeaderPage = BufferManager.getInstance().getPage(headerPage);

        int sautParIntByte = Integer.BYTES;

        for (int i = 1; i <= bufferHeaderPage.getInt(0); i++) {

            if (bufferHeaderPage.getInt(sautParIntByte) > 0) {
                BufferManager.getInstance().freePage(headerPage, false);
                return new PageId(fileIdx, i);
            }

            sautParIntByte += Integer.BYTES;
        }

        BufferManager.getInstance().freePage(headerPage, false);

        return null;
    }

    /**
     * Cette méthode doit écrire l’enregistrement record dans la page de données identifiée par pageId, 
     * et renvoyer son Rid
     * 
     * @param record tuple que l'on souhaite enregistrer
     * @param pageId pageId à laquelle on souhaite enregistrer le record
     * @return retourne l'identifiant du record enregistré
     */
    private Rid writeRecordToDataPage(Record record, PageId pageId) {
        int bytemapPosition = 0;
        ByteBuffer bufferPage = BufferManager.getInstance().getPage(pageId);

        for (bytemapPosition = 0 ; bytemapPosition < record.getRelInfo().getSlotCount() ; bytemapPosition += Byte.BYTES) {
            if (bufferPage.get(bytemapPosition) == 0) {
                int position = record.getRelInfo().getRecordSize() * (bytemapPosition + 1) + relInfo.getSlotCount(); //On doit ajouter le nombre de slots pour ne pas écrire dans le bytemap

                record.writeToBuffer(bufferPage, position);
                byte slotOccupe = 1;
                bufferPage.put(bytemapPosition, slotOccupe);
                
                break;
            }
        }

        BufferManager.getInstance().freePage(pageId, true);

        PageId headerPage = new PageId(pageId.getFileIdx(), 0);
        ByteBuffer headerPageBuffer = BufferManager.getInstance().getPage(headerPage);
		int positionHeaderPage = pageId.getPageIdx()*Integer.BYTES;
        headerPageBuffer.putInt(positionHeaderPage, headerPageBuffer.getInt(positionHeaderPage)-1); //On décrémente le nombre de slots dispo selon la pageId
        BufferManager.getInstance().freePage(headerPage, true);
        
        return new Rid(pageId, bytemapPosition+1);
    }

    /**
     * retourne la liste des records stockés dans la page identifiée par pageId.
     * 
     * @param pageId identifiant de page
     * @return retourne la liste de record
     */
    private ArrayList<Record> getRecordsInDataPage(PageId pageId) {
        ByteBuffer bufferPage = BufferManager.getInstance().getPage(pageId);
        ArrayList<Record> listRecords = new ArrayList<Record>();

        for (int bytemapPosition = 0; bytemapPosition < relInfo.getSlotCount(); bytemapPosition += Byte.BYTES) { 
            if (bufferPage.get(bytemapPosition) == 1) {
                int position = relInfo.getRecordSize() * (bytemapPosition + 1) + relInfo.getSlotCount(); //On doit ajouter le nombre de slots pour ne pas être à la position du bytemap
                Record newRecord = new Record(relInfo);
                newRecord.readFromBuffer(bufferPage, position);
                listRecords.add(newRecord);
            }
        }

        BufferManager.getInstance().freePage(pageId, false);
        return listRecords;
    }

    /**
     * Permet l'insertion d'un record dans une page de données non pleine
     * 
     * @param record record que l'on veut insérer
     * @return retourne le RID du record
     */
    public Rid insertRecord(Record record) {
        PageId FreePage = getFreeDataPageId();

        if(FreePage == null) { //Si aucune page n'est libre on en rajoute une
			FreePage = addDataPage();
        }

		return writeRecordToDataPage(record, FreePage);
    }

    /**
     * Permet de lister tous les records du Heap File 
     * 
     * @return retourne la liste de records
     */
    public ArrayList<Record> getAllRecords() {
        ArrayList<Record> listRecords = new ArrayList<Record>();
        ByteBuffer bufferHeaderPage = BufferManager.getInstance().getPage(new PageId(relInfo.getFileIdx(), 0)); //Correspond au buffer de la header page
        int nbPages = bufferHeaderPage.getInt(0);

        int fileIdx = relInfo.getFileIdx();

        for (int i = 1; i <= nbPages; i++) {
            PageId pageId = new PageId(fileIdx, i);
            listRecords.addAll(getRecordsInDataPage(pageId));
        }

        BufferManager.getInstance().freePage(new PageId(relInfo.getFileIdx(), 0), false);
        return listRecords;
    }

    /**
     * Accesseur de la relation lié au Heap File
     * 
     * @return retourne la relation
     */
    public RelationInfo getRelInfo() {
        return relInfo;
    }
}

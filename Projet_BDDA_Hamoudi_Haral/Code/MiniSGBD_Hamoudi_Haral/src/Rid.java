package src;

public class Rid {
    private PageId pageId;
	private int slotIdx;
    
    /**
     *  Construit un Rid (Record Id, donc identifiant) d’un enregistrement
     * 
     * @param pageId indique la page à laquelle appartient le Record
     * @param slotIdx indice de la case où le Record est stocké
     */
	public Rid(PageId pageId, int slotIdx) {
		this.pageId = pageId;
		this.slotIdx = slotIdx;
	}

    /**
     * Getter de la PageId
     * 
     * @return retourne la PageId du record (enregisterment)
     */
	public PageId getPageId() {
		return pageId;
	}

    /**
     * Getter du SlotIdx
     * 
     * @return retourne le SlotIdx du record (enregisterment)
     */
	public int getSlotIdx() {
		return slotIdx;
    }
    
    @Override
    public boolean equals(Object obj) {
        Rid rid = (Rid) obj;

        if (this == rid) {
            return true;
        }

        return (pageId.equals(rid.getPageId()) && (slotIdx == rid.getSlotIdx()));
    }
}

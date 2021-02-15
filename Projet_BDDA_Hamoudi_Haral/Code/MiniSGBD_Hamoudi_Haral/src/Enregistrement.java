package src;

public class Enregistrement {
    private Record record;
    private Rid rid;

    public Enregistrement(Record record, Rid rid) {
        this.record = record;
        this.rid = rid;
    }

    /**
     * Accesseur du Rid d'un enregistrement
     * 
     * @return retourne le Rid
     */
    public Rid getRid() {
        return rid;
    }

    /**
     * Accesseur du record enregistré
     * 
     * @return retourne un record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Modificateur du record
     * 
     * @param record le nouveau record que l'on a UPDATE
     */
    public void setRecord(Record record) {
        this.record = record;
    }
}

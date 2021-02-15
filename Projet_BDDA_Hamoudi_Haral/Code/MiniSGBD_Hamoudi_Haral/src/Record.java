package src;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
    private RelationInfo relInfo; //correspond à la relation à laquelle « appartient » le record
    
    private ArrayList<String> values;

    /**
     *  Construit un tuple (record) pour une relation donnée avec ses valeurs
     * 
     * @param relInfo relation à laquelle le tuple est rattaché
     * @param values valeurs du record
     */
    public Record(RelationInfo relInfo, ArrayList<String> values) {
        this.relInfo = relInfo;
        this.values = values;
        
    }

    /**
     * Construit un tuple (record) pour une relation donnée sans valeurs
     * 
     * @param relInfo relation à laquelle le tuple est rattaché
     */
    public Record(RelationInfo relInfo) {
        this.relInfo = relInfo;
        values = new ArrayList<String>();
        
    }

    /**
     * Écrit les valeurs du Record dans le buffer, l’une après l’autre, à partir de position
     * 
     * @param buff le buffer dans lequel on veut écrire
     * @param position la position où l'on écrit dans le buffer
     */
    public void writeToBuffer(ByteBuffer buff, int position) {
        if (position > 4096) {
            return;
        }
        buff.position(position);
        for (int i = 0; i < relInfo.getNbCols(); i++) {

            switch (relInfo.getTypeCols().get(i)) {
                case "int":
                    try {
                        int valueInt = Integer.parseInt(values.get(i));
                        buff.putInt(valueInt);

                        if ((buff.position() + Integer.BYTES) > 4096) {
                            return;
                        }

                        buff.position(buff.position() + Integer.BYTES);
                    } catch (NumberFormatException e) {
                        System.out.println("La valeur "+values.get(i)+" n'est pas un nombre entier valide !");
                    }
                    

                    break;

                case "float" :
                    try {
                        float valueFloat = Float.parseFloat(values.get(i));
                        buff.putFloat(valueFloat);

                        if ((buff.position() + Float.BYTES) > 4096) {
                            return;
                        }
                        
                        buff.position(buff.position() + Float.BYTES);
                    } catch (NumberFormatException e) {
                        System.out.println("La valeur "+values.get(i)+" n'est pas un nombre réel valide !");
                    }
                    
                    break;

                default:
                    int sizeString = Integer.parseInt(relInfo.getTypeCols().get(i).substring(6)); //Retourne la taille du string

                    /**
                     * On ajoute des espaces si la taille réelle du string est inférieur à la taille max
                     * Si l'on dépasse la taille max du string, alors on ne prend pas en compte le reste du string
                     */
                    for (int j = 0; j < sizeString; j++) {
                        if (j < values.get(i).length()) {
                            buff.putChar(values.get(i).charAt(j));
                        } else {
                            buff.putChar(' ');
                        }

                        if ((buff.position() + Character.BYTES) > 4096) {
                            return;
                        }

                        buff.position(buff.position() + Character.BYTES);
                    }
                    
                    break;
            }
            
        }
    }

    /**
     * Lit les valeurs du Record depuis le buffer, l’une après l’autre, à partir de position
     * 
     * @param buff le buffer que l'on veut lire
     * @param position la position où l'on écrit dans le buffer
     */
    public void readFromBuffer(ByteBuffer buff, int position) {
        if (position > 4096) {
            return;
        }
        buff.position(position);

        for (int i = 0; i < relInfo.getNbCols(); i++) {

            switch (relInfo.getTypeCols().get(i)) {
                case "int":
                    String intToString = Integer.toString(buff.getInt());
                    values.add(i, intToString);

                    if ((buff.position() + Integer.BYTES) > 4096) {
                        return;
                    }

                    buff.position(buff.position() + Integer.BYTES); //On change la position selon le nbr de byte d'un int

                    break;
            
                case "float":
                    String floatToString = Float.toString(buff.getFloat());
                    values.add(i, floatToString);

                    if ((buff.position() + Float.BYTES) > 4096) {
                        return;
                    }

                    buff.position(buff.position() + Float.BYTES); //On change la position selon le nbr de byte d'un float

                    break;
                default:
                    int sizeString = Integer.parseInt(relInfo.getTypeCols().get(i).substring(6)); //Retourne la taille du string

                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < sizeString; j++) {
                        sb.append(buff.getChar());

                        if ((buff.position() + Character.BYTES) > 4096) {
                            return;
                        }

                        buff.position(buff.position() + Character.BYTES); //On change la position selon le nbr de byte d'un char
                    }

                    values.add(i, sb.toString());

                    break;
            }
        }
    }

    /**
     * Accesseur de la relation à laquelle est rattaché le record
     * 
     * @return retourne la relation
     */
    public RelationInfo getRelInfo() {
        return relInfo;
    }

    /**
     * Accesseur des valeurs du record
     * 
     * @return retourne la liste de valeurs du record
     */
    public ArrayList<String> getValues() {
        return values;
    }
    
    @Override
    public boolean equals(Object obj) {
        Record record = (Record) obj;

        if (this == record) {
            return true;
        }

        for (int i = 0; i < values.size(); i++) {
            if (!values.get(i).equals(record.getValues().get(i))) {
                return false;
            }
        }

        return relInfo.equals(record.getRelInfo());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');

        for (String valeur : values) {
            sb.append(valeur+",");
        }
        sb.deleteCharAt(sb.length()-1); //permet de retirer la dernière virgule
        sb.append(')');
        return sb.toString();
    }
}

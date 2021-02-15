package src;

import java.util.ArrayList;

public class DBInfoTests {
    public static void main(String[] args) {
        DBParams.DBPath = args[0];
        DBParams.pageSize = 4096;
        DBParams.frameCount = 2;

        DBInfo.getInstance().init();

        if (!DBInfo.getInstance().getListRels().isEmpty()) {
            for (RelationInfo relation : DBInfo.getInstance().getListRels()) {
                System.out.println(relation.toString());
            }
        } else {
            System.out.println("La liste de relation est vide");
        }
        DBInfo.getInstance().addRelation(new RelationInfo("Test", 2, new ArrayList<String>(), new ArrayList<String>()));
        
        DBInfo.getInstance().finish();

        
    }
}

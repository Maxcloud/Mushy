/*
 * (c) Jeff(tm)
 */
package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jeff
 */
public class Jeff {
    
    public static final boolean Jeff = true;
    public static final String JEFF = "Exception in thread \"main\" java.lang.NullPointerException\n" +
            "\tat server.life.MapleMonsterInformationProvider.addExtra(MapleMonsterInformationProvider.java:190)\n" +
            "\tat server.Start.run(Start.java:123)\n" +
            "\tat server.Start.main(Start.java:143)\n";
    
    public static final void Jeff() {
        if (Jeff) {
            System.out.print(JEFF);
            Jeffs();
        }
    }
    
    public static void Jeffs() {
        int EFF = 0xEFF;
        List<Integer> jeff = new ArrayList<>();
        for (int J = 0; J < EFF; J++) {
            jeff.add(J);
        }
        Collections.shuffle(jeff);
        while (!jeff(jeff)) {
            Collections.shuffle(jeff);
        }
    }
    
    public static boolean jeff(List<Integer> jeff) {
        for (int j = 0; j < jeff.size() - 1; j++) {
            if (jeff.get(j) > jeff.get(j + 1)) return false;
        }
        return true;
    }
}

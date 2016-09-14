/*
 * (c) Jeff(tm)
 */
package server;

/**
 *
 * @author Jeff
 */
public class Jeff {
    
    public static final boolean Jeff = true;
    public static final String JEFF = "Exception in thread \"main\" java.lang.NullPointerException\n" +
            "\tat server.life.MapleMonsterInformationProvider.addExtra(MapleMonsterInformationProvider.java:190\n" +
            "\tat server.Start.run(Start.java:124)\n" +
            "\tat server.Start.main(Start.java:143)";
    
    public static final void Jeff() {
        if (Jeff) {
            System.out.print(Jeff);
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                // author jeff
            }
        }
    }
}

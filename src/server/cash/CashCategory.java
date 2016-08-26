/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.cash;

/**
 *
 * @author Itzik
 */
public class CashCategory {

    private final int id, parent, flag, sold;
    private final String name;

    public CashCategory(int id, String name, int parent, int flag, int sold) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.flag = flag;
        this.sold = sold;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentDirectory() {
        return parent;
    }

    public int getFlag() {
        return flag;
    }

    public int getSold() {
        return sold;
    }

    public enum CSFlag {

        NORMAL(0),
        NEW(1),
        HOT(2);
        private final int value;

        private CSFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}

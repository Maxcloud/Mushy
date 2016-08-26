package client.inventory;

import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.DatabaseConnection;
import server.MapleItemInformationProvider;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.StaticLifeMovement;
import tools.Randomizer;
import tools.Triple;

public class MapleAndroid
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private int stance = 0;
    private final int uniqueid;
    private final int itemid;
    private int hair;
    private int face;
    private String name;
    private Point pos = new Point(0, 0);
    private boolean changed = false;

    private MapleAndroid(int itemid, int uniqueid) {
        this.itemid = itemid;
        this.uniqueid = uniqueid;
    }

    public static final MapleAndroid loadFromDb(int itemid, int uid) {
        try {
            MapleAndroid ret = new MapleAndroid(itemid, uid);

            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM androids WHERE uniqueid = ?")) {
                ps.setInt(1, uid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }

                    ret.setHair(rs.getInt("hair"));
                    ret.setFace(rs.getInt("face"));
                    ret.setName(rs.getString("name"));
                    ret.changed = false;
                }
            }

            return ret;
        } catch (SQLException ex) {
        }
        return null;
    }

    public final void saveToDb() {
        if (!this.changed) {
            return;
        }
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE androids SET hair = ?, face = ?, name = ? WHERE uniqueid = ?")) {
                ps.setInt(1, this.hair);
                System.out.println(this.hair);
                ps.setInt(2, this.face);
                ps.setString(3, this.name);
                ps.setInt(4, this.uniqueid);
                ps.executeUpdate();
            }
            this.changed = false;
        } catch (SQLException ex) {
        }
    }

    public static final MapleAndroid create(int itemid, int uniqueid) {
        Triple<List<Integer>, List<Integer>, List<Integer>> aInfo;
        aInfo = MapleItemInformationProvider.getInstance().getAndroidInfo(itemid == 1662006 ? 5 : itemid - 1661999);
        if (aInfo == null) {
            return null;
        }
        return create(itemid, uniqueid, ((Integer) ((List) aInfo.left).get(Randomizer.nextInt(((List) aInfo.left).size()))).intValue(), ((Integer) ((List) aInfo.right).get(Randomizer.nextInt(((List) aInfo.right).size()))).intValue());
    }

    public static final MapleAndroid create(int itemid, int uniqueid, int hair, int face) {
        if (uniqueid <= -1) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try {
            hair = 30202;//male hair TODO make better lmao
            try (PreparedStatement pse = DatabaseConnection.getConnection().prepareStatement("INSERT INTO androids (uniqueid, hair, face, name) VALUES (?, ?, ?, ?)")) {
                pse.setInt(1, uniqueid);
                pse.setInt(2, hair);
                pse.setInt(3, face);
                pse.setString(4, "Android");
                pse.executeUpdate();
            }
        } catch (SQLException ex) {
            return null;
        }
        MapleAndroid pet = new MapleAndroid(itemid, uniqueid);
        pet.setHair(hair);
        pet.setFace(face);
        pet.setName("Android");

        return pet;
    }

    public int getUniqueId() {
        return this.uniqueid;
    }

    public final void setHair(int closeness) {
        this.hair = closeness;
        this.changed = true;
    }

    public final int getHair() {
        return this.hair;
    }

    public final void setFace(int closeness) {
        this.face = closeness;
        this.changed = true;
    }

    public final int getFace() {
        return this.face;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
        this.changed = true;
    }

    public final Point getPos() {
        return this.pos;
    }

    public final void setPos(Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return this.stance;
    }

    public final void setStance(int stance) {
        this.stance = stance;
    }

    public final int getItemId() {
        return this.itemid;
    }

    public final void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof StaticLifeMovement)) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}

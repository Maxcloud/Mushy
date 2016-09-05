package server;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleTrait.MapleTraitType;
import client.PlayerStats;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.InventoryException;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.RingSet;
import constants.GameConstants;
import constants.MapConstants;
import lib.data.MapleData;
import lib.data.MapleDataProvider;
import lib.data.MapleDataProviderFactory;
import lib.data.MapleDataTool;
import server.cash.CashItemFactory;
import server.cash.CashItemInfo;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import tools.ArrayUtil;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.CWvsContext.InventoryPacket;

public class MapleInventoryManipulator {

    public static void addRing(MapleCharacter chr, int itemId, int ringId, int sn, String partner) {
        CashItemInfo csi = CashItemFactory.getInstance().getItem(sn);
        if (csi == null) {
            return;
        }
        Item ring = chr.getCashInventory().toItem(csi, ringId);
        if (ring == null || ring.getUniqueId() != ringId || ring.getUniqueId() <= 0 || ring.getItemId() != itemId) {
            return;
        }
        chr.getCashInventory().addToInventory(ring);
        chr.getClient().getSession().write(CSPacket.sendBoughtRings(GameConstants.isCrushRing(itemId), ring, sn, chr.getClient().getAccID(), partner));
    }

    public static boolean addbyItem(final MapleClient c, final Item item) {
        return addbyItem(c, item, false) >= 0;
    }

    public static short addbyItem(final MapleClient c, final Item item, final boolean fromcs) {
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        final short newSlot = c.getPlayer().getInventory(type).addItem(item);
        if (newSlot == -1) {
            if (!fromcs) {
                c.getSession().write(InventoryPacket.getInventoryFull());
                c.getSession().write(InventoryPacket.getShowInventoryFull());
            }
            return newSlot;
        }
        if (GameConstants.isHarvesting(item.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        c.getSession().write(InventoryPacket.addInventorySlot(type, item));
        c.getPlayer().havePartyQuest(item.getItemId());
        return newSlot;
    }

    public static int getUniqueId(int itemId, MaplePet pet) {
        int uniqueid = -1;
        if (GameConstants.isPet(itemId)) {
            if (pet != null) {
                uniqueid = pet.getUniqueId();
            } else {
                uniqueid = MapleInventoryIdentifier.getInstance();
            }
        } else if (GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH || MapleItemInformationProvider.getInstance().isCash(itemId)) { //less work to do
            uniqueid = MapleInventoryIdentifier.getInstance(); //shouldnt be generated yet, so put it here
        }
        return uniqueid;
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String gmLog) {
        return addById(c, itemId, quantity, null, null, 0, false, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addById(c, itemId, quantity, owner, null, 0, false, gmLog);
    }

    public static byte addId(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addId(c, itemId, quantity, owner, null, 0, false, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, 0, false, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, boolean hours, String gmLog) {
        return addId(c, itemId, quantity, owner, pet, period, hours, gmLog) >= 0;
    }

    public static byte addId(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, boolean hours, String gmLog) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return -1;
        }
        if (itemId >= 4031332 && itemId <= 4031341) {
            c.getSession().write(CField.getGameMessage("Hint: Use @event to exchange a certificate of straight wins.", (short) 8));
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        int uniqueid = getUniqueId(itemId, pet);
        short newSlot = -1;
        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(itemId);
            final List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!GameConstants.isRechargable(itemId)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            Item eItem = (Item) i.next();
                            short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && (eItem.getOwner().equals(owner) || owner == null) && eItem.getExpiration() == -1) {
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.getSession().write(InventoryPacket.updateInventorySlot(type, eItem, false));
                            }
                        } else {
                            break;
                        }
                    }
                }
                Item nItem;
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        nItem = new Item(itemId, (byte) 0, newQ, (byte) 0, uniqueid);
                        newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1) {
                            c.getSession().write(InventoryPacket.getInventoryFull());
                            c.getSession().write(InventoryPacket.getShowInventoryFull());
                            return -1;
                        }
                        if (gmLog != null) {
                            nItem.setGMLog(gmLog);
                        }
                        if (owner != null) {
                            nItem.setOwner(owner);
                        }
                        if (period > 0) {
                            nItem.setExpiration(System.currentTimeMillis() + (period * (hours ? 1 : 24) * 60 * 60 * 1000));
                        }
                        if (pet != null) {
                            nItem.setPet(pet);
                            pet.setInventoryPosition(newSlot);
                            c.getPlayer().addPet(pet);
                        }
                        c.getSession().write(InventoryPacket.addInventorySlot(type, nItem));
                        if (GameConstants.isRechargable(itemId) && quantity == 0) {
                            break;
                        }
                    } else {
                        c.getPlayer().havePartyQuest(itemId);
                        c.getSession().write(CWvsContext.enableActions());
                        return (byte) newSlot;
                    }
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0, uniqueid);
                newSlot = c.getPlayer().getInventory(type).addItem(nItem);

                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return -1;
                }
                if (period > 0) {
                    nItem.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                }
                if (gmLog != null) {
                    nItem.setGMLog(gmLog);
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, nItem));
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            if (quantity == 1) {
                final Item nEquip = ii.getEquipById(itemId, uniqueid);
                if (owner != null) {
                    nEquip.setOwner(owner);
                }
                if (gmLog != null) {
                    nEquip.setGMLog(gmLog);
                }
                if (period > 0) {
                    nEquip.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                }
                newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return -1;
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, nEquip));
                if (GameConstants.isHarvesting(itemId)) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        c.getPlayer().havePartyQuest(itemId);
        return (byte) newSlot;
    }

    public static Item addbyId_Gachapon(final MapleClient c, final int itemId, short quantity) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
            return null;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return null;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);

        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(itemId);
            final List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);

            if (!GameConstants.isRechargable(itemId)) {
                Item nItem = null;
                boolean recieved = false;

                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            nItem = (Item) i.next();
                            short oldQ = nItem.getQuantity();

                            if (oldQ < slotMax) {
                                recieved = true;

                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                nItem.setQuantity(newQ);
                                c.getSession().write(InventoryPacket.updateInventorySlot(type, nItem, false));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        nItem = new Item(itemId, (byte) 0, newQ, (byte) 0);
                        final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1 && recieved) {
                            return nItem;
                        } else if (newSlot == -1) {
                            return null;
                        }
                        recieved = true;
                        c.getSession().write(InventoryPacket.addInventorySlot(type, nItem));
                        if (GameConstants.isRechargable(itemId) && quantity == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (recieved) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0);
                final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);

                if (newSlot == -1) {
                    return null;
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, nItem));
                c.getPlayer().havePartyQuest(nItem.getItemId());
                return nItem;
            }
        } else {
            if (quantity == 1) {
                final Item item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                final short newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    return null;
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, item, true));
                c.getPlayer().havePartyQuest(item.getItemId());
                return item;
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        return null;
    }

    public static boolean addFromDrop(final MapleClient c, final Item item, final boolean show) {
        return addFromDrop(c, item, show, false);
    }

    public static boolean addFromDrop(final MapleClient c, Item item, final boolean show, final boolean enhance) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        if (c.getPlayer() == null || (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) || (!ii.itemExists(item.getItemId()))) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return false;
        }
        final int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());

        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(item.getItemId());
            final List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!GameConstants.isRechargable(item.getItemId())) {
                if (quantity <= 0) { //wth
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.showItemUnavailable());
                    return false;
                }
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            final Item eItem = (Item) i.next();
                            final short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner()) && item.getExpiration() == eItem.getExpiration()) {
                                final short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.getSession().write(InventoryPacket.updateInventorySlot(type, eItem, true));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    final short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    final Item nItem = new Item(item.getItemId(), (byte) 0, newQ, item.getFlag());
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setPet(item.getPet());
                    nItem.setGMLog(item.getGMLog());
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    c.getSession().write(InventoryPacket.addInventorySlot(type, nItem, true));
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(item.getItemId(), (byte) 0, quantity, item.getFlag());
                nItem.setExpiration(item.getExpiration());
                nItem.setOwner(item.getOwner());
                nItem.setPet(item.getPet());
                nItem.setGMLog(item.getGMLog());
                final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, nItem));
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            if (quantity == 1) {
                if (enhance) {
                    item = checkEnhanced(item, c.getPlayer());
                }
                final short newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(InventoryPacket.addInventorySlot(type, item, true));
                if (GameConstants.isHarvesting(item.getItemId())) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
            } else {
                throw new RuntimeException("Trying to create equip with non-one quantity");
            }
        }
        if (item.getQuantity() >= 50 && item.getItemId() == 2340000) {
            c.setMonitored(true);
        }
        //if (before == 0) {
        //    switch (item.getItemId()) {
        //        case AramiaFireWorks.KEG_ID:
        //            c.getPlayer().dropMessage(5, "You have gained a Powder Keg.");
        //            break;
        //        case AramiaFireWorks.SUN_ID:
        //            c.getPlayer().dropMessage(5, "You have gained a Warm Sun.");
        //            break;
        //       case AramiaFireWorks.DEC_ID:
        //            c.getPlayer().dropMessage(5, "You have gained a Tree Decoration.");
        //            break;
        //    }
        //}
        c.getPlayer().havePartyQuest(item.getItemId());
        if (show) {
            c.getSession().write(InfoPacket.getShowItemGain(item.getItemId(), item.getQuantity()));
        }
        return true;
    }

    private static Item checkEnhanced(final Item before, final MapleCharacter chr) {
        if (before instanceof Equip) {
            final Equip eq = (Equip) before;
            if (eq.getState() == 0 && (eq.getUpgradeSlots() >= 1 || eq.getLevel() >= 1) && GameConstants.canScroll(eq.getItemId()) && Randomizer.nextInt(100) >= 80) { //20% chance of pot?
                eq.resetPotential();
            }
        }
        return before;
    }

    public static boolean checkSpace(final MapleClient c, final int itemid, int quantity, final String owner) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (c.getPlayer() == null || (ii.isPickupRestricted(itemid) && c.getPlayer().haveItem(itemid, 1, true, false)) || (!ii.itemExists(itemid))) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (quantity <= 0 && !GameConstants.isRechargable(itemid)) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        if (c == null || c.getPlayer() == null || c.getPlayer().getInventory(type) == null) { //wtf is causing this?
            return false;
        }
        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(itemid);
            final List<Item> existing = c.getPlayer().getInventory(type).listById(itemid);
            if (!GameConstants.isRechargable(itemid)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    for (Item eItem : existing) {
                        final short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner != null && owner.equals(eItem.getOwner())) {
                            final short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
            }
            // add new slots if there is still something left
            final int numSlotsNeeded;
            if (slotMax > 0 && !GameConstants.isRechargable(itemid)) {
                numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
            } else {
                numSlotsNeeded = 1;
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        } else {
            return !c.getPlayer().getInventory(type).isFull();
        }
    }

    public static boolean removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, final short quantity, final boolean fromDrop) {
        return removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static boolean removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, short quantity, final boolean fromDrop, final boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        final Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            final boolean allowZero = consume && GameConstants.isRechargable(item.getItemId());
            c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
            if (GameConstants.isHarvesting(item.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }

            if (item.getQuantity() == 0 && !allowZero) {
                c.getSession().write(InventoryPacket.clearInventoryItem(type, item.getPosition(), fromDrop));
            } else {
                c.getSession().write(InventoryPacket.updateInventorySlot(type, (Item) item, fromDrop));
            }
            return true;
        }
        return false;
    }

    public static boolean removeById(final MapleClient c, final MapleInventoryType type, final int itemId, final int quantity, final boolean fromDrop, final boolean consume) {
        int remremove = quantity;
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            int theQ = item.getQuantity();
            if (remremove <= theQ && removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume)) {
                remremove = 0;
                break;
            } else if (remremove > theQ && removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume)) {
                remremove -= theQ;
            }
        }
        return remremove <= 0;
    }

    public static boolean removeFromSlot_Lock(final MapleClient c, final MapleInventoryType type, final short slot, short quantity, final boolean fromDrop, final boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        final Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            if (ItemFlag.LOCK.check(item.getFlag()) || ItemFlag.UNTRADABLE.check(item.getFlag())) {
                return false;
            }
            return removeFromSlot(c, type, slot, quantity, fromDrop, consume);
        }
        return false;
    }

    public static boolean removeById_Lock(final MapleClient c, final MapleInventoryType type, final int itemId) {
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (removeFromSlot_Lock(c, type, item.getPosition(), (short) 1, false, false)) {
                return true;
            }
        }
        return false;
    }

    public static void move(final MapleClient c, final MapleInventoryType type, final short src, final short dst) {
        if (src < 0 || dst < 0 || src == dst || type == MapleInventoryType.EQUIPPED) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Item source = c.getPlayer().getInventory(type).getItem(src);
        final Item initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            return;
        }
        boolean bag = false, switchSrcDst = false, bothBag = false;
        short eqIndicator = -1;
        if (dst > c.getPlayer().getInventory(type).getSlotLimit()) {
            if (type == MapleInventoryType.ETC && dst > 100 && dst % 100 != 0) {
                final int eSlot = c.getPlayer().getExtendedSlot((dst / 100) - 1);
                if (eSlot > 0) {
                    final MapleStatEffect ee = ii.getItemEffect(eSlot);
                    if (dst % 100 > ee.getSlotCount() || ee.getType() != ii.getBagType(source.getItemId()) || ee.getType() <= 0) {
                        c.getPlayer().dropMessage(1, "You may not move that item to the bag.");
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    } else {
                        eqIndicator = 0;
                        bag = true;
                    }
                } else {
                    c.getPlayer().dropMessage(1, "You may not move it to that bag.");
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
            } else {
                c.getPlayer().dropMessage(1, "You may not move it there.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
        }
        if (src > c.getPlayer().getInventory(type).getSlotLimit() && type == MapleInventoryType.ETC && src > 100 && src % 100 != 0) {
            //source should be not null so not much checks are needed
            if (!bag) {
                switchSrcDst = true;
                eqIndicator = 0;
                bag = true;
            } else {
                bothBag = true;
            }
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        final short oldsrcQ = source.getQuantity();
        final short slotMax = ii.getSlotMax(source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        if (GameConstants.isHarvesting(source.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        if (!type.equals(MapleInventoryType.EQUIP) && initialTarget != null
                && initialTarget.getItemId() == source.getItemId()
                && initialTarget.getOwner().equals(source.getOwner())
                && initialTarget.getExpiration() == source.getExpiration()
                && !GameConstants.isRechargable(source.getItemId())
                && !type.equals(MapleInventoryType.CASH)) {
            if (GameConstants.isHarvesting(initialTarget.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            if ((olddstQ + oldsrcQ) > slotMax) {
                c.getSession().write(InventoryPacket.moveAndMergeWithRestInventoryItem(type, src, dst, (short) ((olddstQ + oldsrcQ) - slotMax), slotMax, bag, switchSrcDst, bothBag));
            } else {
                c.getSession().write(InventoryPacket.moveAndMergeInventoryItem(type, src, dst, ((Item) c.getPlayer().getInventory(type).getItem(dst)).getQuantity(), bag, switchSrcDst, bothBag));
            }
        } else {
            c.getSession().write(InventoryPacket.moveInventoryItem(type, switchSrcDst ? dst : src, switchSrcDst ? src : dst, eqIndicator, bag, bothBag));
        }
    }

    public static void equip(final MapleClient c, final short src, short dst) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleCharacter chr = c.getPlayer();
            if (chr == null || dst == -55) {
            return;
        }
        c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
        final PlayerStats statst = c.getPlayer().getStat();
        statst.recalcLocalStats(c.getPlayer());
        Equip source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src);
        Equip target;

        if (source == null || source.getDurability() == 0 || GameConstants.isHarvesting(source.getItemId())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (GameConstants.isGMEquip(source.getItemId()) && !c.getPlayer().isStaff()) {
            c.getPlayer().dropMessage(1, "Only Game Masters are allowed to use this item.");
            c.getPlayer().removeAll(source.getItemId(), false);
            c.getSession().write(CWvsContext.enableActions());
        return;
        }
        //if (GameConstants.isMadeByGM(c, source.getItemId(), src) && !c.getPlayer().isStaff()) {
            // c.getPlayer().dropMessage(1, "You are not allowed to use GM-Made equips.");
            // c.getSession().write(CWvsContext.enableActions());
        // return;
        //}
        if (GameConstants.isOverPoweredEquip(c, source.getItemId(), src) && !c.getPlayer().isStaff()) {
            c.getPlayer().dropMessage(1, "It seems that the item is way too over powered, please report to the Admin if you think that the system is wrong.");
            //c.getPlayer().removeAll(source.getItemId(), false); //System might be wrong
            c.getSession().write(CWvsContext.enableActions());
        return;
        }
        if (!c.getPlayer().isGM()) {
            if (source.getItemId() == 1112663 || source.getItemId() == 1112586) {
                c.getPlayer().dropMessage(1, "White Angelic Blessing, and Dark Angelic Blessing are currently not working.");
                c.getSession().write(CWvsContext.enableActions());
            return;
            }
        }

    final Map<String, Integer> stats = ii.getEquipStats(source.getItemId());

        if (stats == null) {
            c.getSession().write(CWvsContext.enableActions());
        return;
        }
        if (dst > -1200 && dst < -999 && !GameConstants.isEvanDragonItem(source.getItemId()) && !GameConstants.isMechanicItem(source.getItemId())) {
            c.getSession().write(CWvsContext.enableActions());
        return;
    // } else if ((dst <= -1200 || (dst >= -999 && dst < -99)) && !stats.containsKey("cash")) {
        // c.getSession().write(CWvsContext.enableActions());
        // return;
    // }
        } else if (((dst < -5003) || ((dst >= -999) && (dst < -99))) &&(!stats.containsKey("cash"))) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }else if (dst > -1400 && dst <= -1300 && c.getPlayer().getAndroid() == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        } else if (dst > -5000 && dst <= -1400) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        } else if (dst > -5100 && dst <= -5000 && source.getItemId() / 10000 != 120) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (!ii.canEquip(stats, source.getItemId(), chr.getLevel(), chr.getJob(), chr.getFame(), statst.getTotalStr(), statst.getTotalDex(), statst.getTotalLuk(), statst.getTotalInt(), c.getPlayer().getStat().levelBonus, source.getReqLevel())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (GameConstants.isWeapon(source.getItemId()) && dst != -10 && dst != -11) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (dst == -18 && !GameConstants.isMountItemAvailable(source.getItemId(), c.getPlayer().getJob())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (dst == -118 && source.getItemId() / 10000 != 190) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        //totem2
        if ((dst <= -5000) && (dst > -5003) && (source.getItemId() / 10000 != 120)) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (dst == -59) { //pendant
            MapleQuestStatus stat = c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
            if (stat == null || stat.getCustomData() == null || Long.parseLong(stat.getCustomData()) < System.currentTimeMillis()) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
        }
        if (GameConstants.isKatara(source.getItemId()) || source.getItemId() / 10000 == 135) {
            dst = (byte) -10; //shield slot
        }
        if (GameConstants.isEvanDragonItem(source.getItemId()) && (chr.getJob() < 2200 || chr.getJob() > 2218)) {
            c.getSession().write(CWvsContext.enableActions());
        return;
        }

        if (GameConstants.isMechanicItem(source.getItemId()) && (chr.getJob() < 3500 || chr.getJob() > 3512)) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        if (source.getItemId() / 1000 == 1112) { //ring
            for (RingSet s : RingSet.values()) {
                if (s.id.contains(Integer.valueOf(source.getItemId()))) {
                    List<Integer> theList = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).listIds();
                    for (Integer i : s.id) {
                        if (theList.contains(i)) {
                            c.getPlayer().dropMessage(1, "You may not equip this item because you already have a " + (StringUtil.makeEnumHumanReadable(s.name())) + " equipped.");
                            c.getSession().write(CWvsContext.enableActions());
                        return;
                        }
                    }
                }
            }
        }
        
        switch (dst) {
            case -6: { // Top
                final Item top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
                if (top != null && GameConstants.isOverall(top.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return;
                    }
                unequip(c, (byte) -5, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                }
            break;
            }
            case -5: {
                final Item top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
                final Item bottom = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
                if (top != null && GameConstants.isOverall(source.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull(bottom != null && GameConstants.isOverall(source.getItemId()) ? 1 : 0)) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return;
                    }
                unequip(c, (byte) -5, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                }
                if (bottom != null && GameConstants.isOverall(source.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return;
                    }
                unequip(c, (byte) -6, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                }
                break;
            }
            case -10: { // Shield
                Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
                if (GameConstants.isKatara(source.getItemId())) {
                    if ((chr.getJob() != 900 && !GameConstants.isDualBlade(chr.getJob())) || weapon == null || !GameConstants.isDagger(weapon.getItemId())) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return;
                    }
                } else if (weapon != null && GameConstants.isTwoHanded(weapon.getItemId()) && !GameConstants.isSpecialShield(source.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return;
                    }
                unequip(c, (byte) -11, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
            break;
        }
        case -11: { // Weapon
            Item shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
            if (shield != null && GameConstants.isTwoHanded(source.getItemId()) && !GameConstants.isSpecialShield(shield.getItemId())) {
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                return;
                }
            unequip(c, (byte) -10, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
            break;
        }
    }
    source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src); // Equip
    target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst); // Currently equipping
    if (source == null) {
        c.getSession().write(CWvsContext.enableActions());
    return;
}
    short flag = source.getFlag();
        if (stats.get("equipTradeBlock") != null || source.getItemId() / 10000 == 167) { // Block trade when equipped.
            if (!ItemFlag.UNTRADABLE.check(flag)) {
                flag |= ItemFlag.UNTRADABLE.getValue();
                source.setFlag(flag);
                c.getSession().write(InventoryPacket.updateSpecialItemUse_(source, MapleInventoryType.EQUIP.getType(), c.getPlayer()));
            }
        }
    if (source.getItemId() / 10000 == 166) {
        if (source.getAndroid() == null) {
            int uid = MapleInventoryIdentifier.getInstance();
            source.setUniqueId(uid);
            source.setAndroid(MapleAndroid.create(source.getItemId(), uid));
            flag = (short) (flag | ItemFlag.LOCK.getValue());
            flag = (short) (flag | ItemFlag.UNTRADABLE.getValue());
            flag = (short) (flag | ItemFlag.ANDROID_ACTIVATED.getValue());
            source.setFlag(flag);
            c.getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse_(source, MapleInventoryType.EQUIP.getType(), c.getPlayer()));
        }
        chr.removeAndroid();
        chr.setAndroid(source.getAndroid());
    } else if ((dst <= -1300) && (chr.getAndroid() != null)) {
        chr.setAndroid(chr.getAndroid());
    }
    if (source.getCharmEXP() > 0 && !ItemFlag.CHARM_EQUIPPED.check(flag)) {
        chr.getTrait(MapleTraitType.charm).addExp(source.getCharmEXP(), chr);
        source.setCharmEXP((short) 0);
        flag |= ItemFlag.CHARM_EQUIPPED.getValue();
        source.setFlag(flag);
        c.getSession().write(InventoryPacket.updateSpecialItemUse_(source, GameConstants.getInventoryType(source.getItemId()).getType(), c.getPlayer()));
    }

    chr.getInventory(MapleInventoryType.EQUIP).removeSlot(src);
    if (target != null) {
    chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
    }
    source.setPosition(dst);
    chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
    if (target != null) {
        target.setPosition(src);
        chr.getInventory(MapleInventoryType.EQUIP).addFromDB(target);
    }
    if (GameConstants.isWeapon(source.getItemId())) {
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.BOOSTER);
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_CLAW);
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SOULARROW);
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.LIGHTNING_CHARGE);
    }
    if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) {
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
    } else if (source.getItemId() == 1122017) {
        chr.startFairySchedule(true, true);
    }
    if (source.getState() >= 17) {
        final Map<Skill, SkillEntry> ss = new HashMap<>();
        int[] potentials = ArrayUtil.concat(source.getPotential(), source.getBonusPotential());
        for (int i : potentials) {
            if (i > 0) {
                StructItemOption pot = ii.getPotentialInfo(i).get(ii.getReqLevel(source.getItemId()) / 10);
                    if (pot != null && pot.get("skillID") > 0) {
                        ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(pot.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                    }
                }
            }
            c.getPlayer().changeSkillLevel_Skip(ss, true);
        }
    if (source.getSocketState() > 15) {
        final Map<Skill, SkillEntry> ss = new HashMap<>();
        int[] sockets = source.getSockets();
        for (int i : sockets) {
            if (i > 0) {
                StructItemOption soc = ii.getSocketInfo(i);
                    if (soc != null && soc.get("skillID") > 0) {
                        ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(soc.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                    }
                }
            }
            c.getPlayer().changeSkillLevel_Skip(ss, true);
        }
    c.getSession().write(InventoryPacket.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 2, false, false));
    chr.equipChanged();
}

    public static void unequip(final MapleClient c, final short src, final short dst) {
        Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
        Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);

        if (dst < 0 || source == null || src == -55) {
            return;
        }
        if (target != null && src <= 0) { // do not allow switching with equip
            c.getSession().write(InventoryPacket.getInventoryFull());
            return;
        }
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
        if (target != null) {
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
        }
        source.setPosition(dst);
        c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
        if (target != null) {
            target.setPosition(src);
            c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
        }

        if (GameConstants.isWeapon(source.getItemId())) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.BOOSTER);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_CLAW);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SOULARROW);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.LIGHTNING_CHARGE);
        } else if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
        } else if (source.getItemId() / 10000 == 166 || source.getItemId() / 10000 == 167) {
            c.getPlayer().removeAndroid();
        } else if (src <= -1300 && c.getPlayer().getAndroid() != null) {
            c.getPlayer().setAndroid(c.getPlayer().getAndroid());
        } else if (source.getItemId() == 1122017) {
            c.getPlayer().cancelFairySchedule(true);
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (source.getState() >= 17) {
            final Map<Skill, SkillEntry> ss = new HashMap<>();
            int[] potentials = ArrayUtil.concat(source.getPotential(), source.getBonusPotential());
            for (int i : potentials) {
                if (i > 0) {
                    StructItemOption pot = ii.getPotentialInfo(i).get(ii.getReqLevel(source.getItemId()) / 10);
                    if (pot != null && pot.get("skillID") > 0) {
                        ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(pot.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 0, (byte) 0, -1));
                    }
                }
            }
            c.getPlayer().changeSkillLevel_Skip(ss, true);
        }
        if (source.getSocketState() > 15) {
            final Map<Skill, SkillEntry> ss = new HashMap<>();
            int[] sockets = source.getSockets();
            for (int i : sockets) {
                if (i > 0) {
                    StructItemOption soc = ii.getSocketInfo(i);
                    if (soc != null && soc.get("skillID") > 0) {
                        ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(soc.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                    }
                }
            }
            c.getPlayer().changeSkillLevel_Skip(ss, true);
        }
        c.getSession().write(InventoryPacket.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 1, false, false));
        c.getPlayer().equipChanged();
    }

    public static boolean drop(final MapleClient c, MapleInventoryType type, final short src, final short quantity) {
        return drop(c, type, src, quantity, false);
    }

    public static boolean drop(final MapleClient c, MapleInventoryType type, final short src, short quantity, final boolean npcInduced) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return false;
        }
        final Item source = c.getPlayer().getInventory(type).getItem(src);
        if (quantity < 0 || source == null || (src == -55) || (!npcInduced && GameConstants.isPet(source.getItemId())) || (quantity == 0 && !GameConstants.isRechargable(source.getItemId())) || c.getPlayer().inPVP()) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }

        final short flag = source.getFlag();
        if (quantity > source.getQuantity() && !GameConstants.isRechargable(source.getItemId())) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (ItemFlag.LOCK.check(flag) || (quantity != 1 && type == MapleInventoryType.EQUIP)) { // hack
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        final Point dropPos = new Point(c.getPlayer().getPosition());
        if (quantity < source.getQuantity() && !GameConstants.isRechargable(source.getItemId())) {
            final Item target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.getSession().write(InventoryPacket.dropInventoryItemUpdate(type, source));

            if (ii.isDropRestricted(target.getItemId()) || ii.isAccountShared(target.getItemId())) {
                if (ItemFlag.KARMA_EQ.check(flag)) {
                    target.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else if (ItemFlag.KARMA_USE.check(flag)) {
                    target.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else if (MapConstants.isAnyDropMap(c.getPlayer().getMapId())) {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                }
            } else {
                if ((GameConstants.isPet(source.getItemId()) || ItemFlag.UNTRADABLE.check(flag)) && !MapConstants.isAnyDropMap(c.getPlayer().getMapId())) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                }
            }
        } else {
            c.getPlayer().getInventory(type).removeSlot(src);
            if (GameConstants.isHarvesting(source.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            c.getSession().write(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
            if (src < 0) {
                c.getPlayer().equipChanged();
            }
            if (ii.isDropRestricted(source.getItemId()) || ii.isAccountShared(source.getItemId())) {
                if (ItemFlag.KARMA_EQ.check(flag)) {
                    source.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                } else if (ItemFlag.KARMA_USE.check(flag)) {
                    source.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                } else if (MapConstants.isAnyDropMap(c.getPlayer().getMapId())) {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                } else {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
                }
            } else {
                if ((GameConstants.isPet(source.getItemId()) || ItemFlag.UNTRADABLE.check(flag)) && !MapConstants.isAnyDropMap(c.getPlayer().getMapId())) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                }
            }
        }
        return true;
    }

    public static String searchId(int type, String search) {
        String result = "";
        MapleData data = null;
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider("String.wz");
        //result += "<<Type: " + type + " | Search: " + search + ">>";
        switch (type) {
            case 1:
                List<String> retNpcs = new ArrayList<>();
                data = dataProvider.getData("Npc.img");
                List<Pair<Integer, String>> npcPairList = new LinkedList<>();
                for (MapleData npcIdData : data.getChildren()) {
                    npcPairList.add(new Pair<>(Integer.parseInt(npcIdData.getName()), MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME")));
                }
                for (Pair<Integer, String> npcPair : npcPairList) {
                    if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                        retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
                    }
                }
                if (retNpcs != null && retNpcs.size() > 0) {
                    for (String singleRetNpc : retNpcs) {
                        result += singleRetNpc;
                    }
                } else {
                    result += "No NPC's Found";
                }
                break;
            case 2:
                List<String> retMaps = new ArrayList<>();
                data = dataProvider.getData("Map.img");
                List<Pair<Integer, String>> mapPairList = new LinkedList<>();
                for (MapleData mapAreaData : data.getChildren()) {
                    for (MapleData mapIdData : mapAreaData.getChildren()) {
                        mapPairList.add(new Pair<>(Integer.parseInt(mapIdData.getName()), MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
                    }
                }
                for (Pair<Integer, String> mapPair : mapPairList) {
                    if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                        retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
                    }
                }
                if (retMaps != null && retMaps.size() > 0) {
                    for (String singleRetMap : retMaps) {
                        result += singleRetMap;
                    }
                } else {
                    result += "No Maps Found";
                }
                break;
            case 3:
                List<String> retMobs = new ArrayList<>();
                data = dataProvider.getData("Mob.img");
                List<Pair<Integer, String>> mobPairList = new LinkedList<>();
                for (MapleData mobIdData : data.getChildren()) {
                    mobPairList.add(new Pair<>(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
                }
                for (Pair<Integer, String> mobPair : mobPairList) {
                    if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                        retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                    }
                }
                if (retMobs != null && retMobs.size() > 0) {
                    for (String singleRetMob : retMobs) {
                        result += singleRetMob;
                    }
                } else {
                    result += "No Mobs Found";
                }
                break;
            case 4:
                List<String> retItems = new ArrayList<>();
                for (ItemInformation itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                    if (itemPair != null && itemPair.name != null && itemPair.name.toLowerCase().contains(search.toLowerCase())) {
                        retItems.add("\r\n#b" + itemPair.itemId + " " + " #k- " + " #r#z" + itemPair.itemId + "##k");
                    }
                }
                if (retItems != null && retItems.size() > 0) {
                    for (String singleRetItem : retItems) {
                        if (result.length() < 10000) {
                            result += singleRetItem;
                        } else {
                            result += "\r\n#bCouldn't load all items, there are too many results.#k";
                            return result;
                        }
                    }
                } else {
                    result += "No Items Found";
                }
                break;
            case 5:
                List<String> retQuests = new ArrayList<>();
                for (MapleQuest itemPair : MapleQuest.getAllInstances()) {
                    if (itemPair.getName().length() > 0 && itemPair.getName().toLowerCase().contains(search.toLowerCase())) {
                        retQuests.add(itemPair.getId() + " - " + itemPair.getName());
                    }
                }
                if (retQuests != null && retQuests.size() > 0) {
                    for (String singleRetQuest : retQuests) {
                        result += singleRetQuest;
                    }
                } else {
                    result += "No Quests Found";
                }
                break;
            case 6:
                List<String> retSkills = new ArrayList<>();
                for (Skill skil : SkillFactory.getAllSkills()) {
                    if (skil.getName() != null && skil.getName().toLowerCase().contains(search.toLowerCase())) {
                        retSkills.add(skil.getId() + " - " + skil.getName());
                    }
                }
                if (retSkills != null && retSkills.size() > 0) {
                    for (String singleRetSkill : retSkills) {
                        result += singleRetSkill;
                    }
                } else {
                    result += "No Skills Found";
                }
                break;
            default:
                result += "Invalid Type";
        }
        return result;
    }
}

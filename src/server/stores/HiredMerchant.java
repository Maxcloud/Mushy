/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.stores;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import constants.GameConstants;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.TimerManager;
import server.maps.MapleMapObjectType;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;

public class HiredMerchant extends AbstractPlayerStore {

    public ScheduledFuture<?> schedule;
    private final List<String> blacklist;
    private int storeid;
    private final long start;

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6);
        start = System.currentTimeMillis();
        blacklist = new LinkedList<>();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (getMCOwner() != null && getMCOwner().getPlayerShop() == HiredMerchant.this) {
                    getMCOwner().setPlayerShop(null);
                }
                removeAllVisitors(-1, -1);
                closeShop(true, true);
            }
        }, 1000 * 60 * 60 * 24);
    }

    @Override
    public byte getShopType() {
        return IMaplePlayerShop.HIRED_MERCHANT;
    }

    public final void setStoreid(final int storeid) {
        this.storeid = storeid;
    }

    public List<MaplePlayerShopItem> searchItem(final int itemSearch) {
        final List<MaplePlayerShopItem> itemz = new LinkedList<>();
        for (MaplePlayerShopItem item : items) {
            if (item.item.getItemId() == itemSearch && item.bundles > 0) {
                itemz.add(item);
            }
        }
        return itemz;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        final MaplePlayerShopItem pItem = items.get(item);
        final Item shopItem = pItem.item;
        final Item newItem = shopItem.copy();
        final short perbundle = newItem.getQuantity();
        final int theQuantity = (pItem.price * quantity);
        newItem.setQuantity((short) (quantity * perbundle));

        short flag = newItem.getFlag();

        if (ItemFlag.KARMA_EQ.check(flag)) {
            newItem.setFlag((short) (flag - ItemFlag.KARMA_EQ.getValue()));
        } else if (ItemFlag.KARMA_USE.check(flag)) {
            newItem.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
        }

        if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner())) {
            final int gainmeso = getMeso() + theQuantity - GameConstants.EntrustedStoreTax(theQuantity);
            if (gainmeso > 0) {
                setMeso(gainmeso);
                pItem.bundles -= quantity; // Number remaining in the store
                MapleInventoryManipulator.addFromDrop(c, newItem, false);
                bought.add(new BoughtItem(newItem.getItemId(), quantity, theQuantity, c.getPlayer().getName()));
                c.getPlayer().gainMeso(-theQuantity, false);
                saveItems();
                MapleCharacter chr = getMCOwnerWorld();
                if (chr != null) {
                    chr.dropMessage(-5, "Item " + MapleItemInformationProvider.getInstance().getName(newItem.getItemId()) + " (" + perbundle + ") x " + quantity + " has sold in the Hired Merchant. Quantity left: " + pItem.bundles);
                }
            } else {
                c.getPlayer().dropMessage(1, "The seller has too many mesos.");
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            c.getPlayer().dropMessage(1, "Your inventory is full.");
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
        if (schedule != null) {
            schedule.cancel(false);
        }
        if (saveItems) {
            saveItems();
            items.clear();
        }
        if (remove) {
            ChannelServer.getInstance(channel).removeMerchant(this);
            getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
        getMap().removeMapObject(this);
        schedule = null;
    }

    public int getTimeLeft() {
        return (int) ((System.currentTimeMillis() - start) / 1000);
    }

    public final int getStoreId() {
        return storeid;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }

    public final boolean isInBlackList(final String bl) {
        return blacklist.contains(bl);
    }

    public final void addBlackList(final String bl) {
        blacklist.add(bl);
    }

    public final void removeBlackList(final String bl) {
        blacklist.remove(bl);
    }

    public final void sendBlackList(final MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantBlackListView(blacklist));
    }

    public final void sendVisitor(final MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantVisitorView(visitors));
    }
}

/*
This file is part of the ZeroFusion MapleStory Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <mat@odinms.de>
Jan Christian Meyer <vimes@odinms.de>
ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

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
package server.events;

import java.util.LinkedList;
import java.util.List;

import client.MapleCharacter;
import server.TimerManager;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleCoconut extends MapleEvent {

    private List<MapleCoconuts> coconuts = new LinkedList<>();
    private int[] coconutscore = new int[2];
    private int countBombing = 0;
    private int countFalling = 0;
    private int countStopped = 0;

    public MapleCoconut(final int channel, final MapleEventType type) {
	super(channel,type);
    }

    @Override
    public void finished(MapleCharacter chr) { //do nothing.
    }

    @Override
    public void reset() {
        super.reset();
        resetCoconutScore();
    }

    @Override
    public void unreset() {
        super.unreset();
        resetCoconutScore();
        setHittable(false);
    }

    @Override
    public void onMapLoad(MapleCharacter chr) {
	super.onMapLoad(chr);
        chr.getClient().getSession().write(CField.coconutScore(getCoconutScore()));
    }

    public MapleCoconuts getCoconut(int id) {
	if (id >= coconuts.size()) {
	    return null;
	}
        return coconuts.get(id);
    }

    public List<MapleCoconuts> getAllCoconuts() {
        return coconuts;
    }

    public void setHittable(boolean hittable) {
        for (MapleCoconuts nut : coconuts) {
            nut.setHittable(hittable);
        }
    }

    public int getBombings() {
        return countBombing;
    }

    public void bombCoconut() {
        countBombing--;
    }

    public int getFalling() {
        return countFalling;
    }

    public void fallCoconut() {
        countFalling--;
    }

    public int getStopped() {
        return countStopped;
    }

    public void stopCoconut() {
        countStopped--;
    }

    public int[] getCoconutScore() { // coconut event
        return coconutscore;
    }

    public int getMapleScore() { // Team Maple, coconut event
        return coconutscore[0];
    }

    public int getStoryScore() { // Team Story, coconut event
        return coconutscore[1];
    }

    public void addMapleScore() { // Team Maple, coconut event
        coconutscore[0]++;
    }

    public void addStoryScore() { // Team Story, coconut event
        coconutscore[1]++;
    }

    public void resetCoconutScore() {
        coconutscore[0] = 0;
        coconutscore[1] = 0;
        countBombing = 80;
        countFalling = 401;
        countStopped = 20;
        coconuts.clear();
        for (int i = 0; i < 506; i++) {
            coconuts.add(new MapleCoconuts());
        }
    }

    @Override
    public void startEvent() {
        reset();
        setHittable(true);
        getMap(0).broadcastMessage(CWvsContext.broadcastMsg(5, "The event has started!!"));
        getMap(0).broadcastMessage(CField.hitCoconut(true, 0, 0));
        getMap(0).broadcastMessage(CField.getClock(300));

        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    bonusTime();
                } else {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getTeam() == (getMapleScore() > getStoryScore() ? 0 : 1)) {
                            chr.getClient().getSession().write(CField.showMapEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(CField.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(CField.showMapEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(CField.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                }
            }
        }, 300000);
    }

    public void bonusTime() {
        getMap(0).broadcastMessage(CField.getClock(60));
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (getMapleScore() == getStoryScore()) {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        chr.getClient().getSession().write(CField.showMapEffect("event/coconut/lose"));
                        chr.getClient().getSession().write(CField.playSound("Coconut/Failed"));
                    }
                    warpOut();
                } else {
                    for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                        if (chr.getTeam() == (getMapleScore() > getStoryScore() ? 0 : 1)) {
                            chr.getClient().getSession().write(CField.showMapEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(CField.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(CField.showMapEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(CField.playSound("Coconut/Failed"));
                        }
                    }
                    warpOut();
                }
            }
        }, 60000);

    }

    public void warpOut() {
        setHittable(false);
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                for (MapleCharacter chr : getMap(0).getCharactersThreadsafe()) {
                    if ((getMapleScore() > getStoryScore() && chr.getTeam() == 0) || (getStoryScore() > getMapleScore() && chr.getTeam() == 1)) {
                        givePrize(chr);
                    }
		    warpBack(chr);
                }
                unreset();
            }
        }, 10000);
    }

    public static class MapleCoconuts {

        private int hits = 0;
        private boolean hittable = false;
        private boolean stopped = false;
        private long hittime = System.currentTimeMillis();

        public void hit() {
            this.hittime = System.currentTimeMillis() + 1000; // test
            hits++;
        }

        public int getHits() {
            return hits;
        }

        public void resetHits() {
            hits = 0;
        }

        public boolean isHittable() {
            return hittable;
        }

        public void setHittable(boolean hittable) {
            this.hittable = hittable;
        }

        public boolean isStopped() {
            return stopped;
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        public long getHitTime() {
            return hittime;
        }
    }
}

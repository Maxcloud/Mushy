/*
This file is part of the ZeroFusion MapleStory Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
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

import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import client.MapleStat;
import server.TimerManager;
import server.events.MapleOxQuizFactory.MapleOxQuizEntry;
import server.maps.MapleMap;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleOxQuiz extends MapleEvent {

    private ScheduledFuture<?> oxSchedule, oxSchedule2;
    private int timesAsked = 0;
    private boolean finished = false;

    public MapleOxQuiz(final int channel, final MapleEventType type) {
	super(channel,type);
    }

    @Override
    public void finished(MapleCharacter chr) { //do nothing.
    }

    private void resetSchedule() {
        if (oxSchedule != null) {
            oxSchedule.cancel(false);
            oxSchedule = null;
        }
        if (oxSchedule2 != null) {
            oxSchedule2.cancel(false);
            oxSchedule2 = null;
        }
    }

    @Override
    public void onMapLoad(MapleCharacter chr) {
	super.onMapLoad(chr);
        if (chr.getMapId() == type.mapids[0] && !chr.isGM()) {
            chr.canTalk(false);
        }
    }

    @Override
    public void reset() {
        super.reset();
        getMap(0).getPortal("join00").setPortalState(false);
        resetSchedule();
        timesAsked = 0;
    }

    @Override
    public void unreset() {
        super.unreset();
        getMap(0).getPortal("join00").setPortalState(true);
        resetSchedule();
    }
    //apparently npc says 10 questions

    @Override
    public void startEvent() {
        sendQuestion();
	finished = false;
    }

    public void sendQuestion() {
        sendQuestion(getMap(0));
    }

    public void sendQuestion(final MapleMap toSend) {
	final Entry<Pair<Integer, Integer>, MapleOxQuizEntry> question = MapleOxQuizFactory.getInstance().grabRandomQuestion();
        if (oxSchedule2 != null) {
            oxSchedule2.cancel(false);
        }
        oxSchedule2 = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                int number = 0;
                for (MapleCharacter mc : toSend.getCharactersThreadsafe()) {
                    if (mc.isGM() || !mc.isAlive()) {
                        number++;
                    }
                }
                if (toSend.getCharactersSize() - number <= 1 || timesAsked == 10) {
                    toSend.broadcastMessage(CWvsContext.broadcastMsg(6, "The event has ended"));
                    unreset();
                    for (MapleCharacter chr : toSend.getCharactersThreadsafe()) {
                        if (chr != null && !chr.isGM() && chr.isAlive()) {
                            chr.canTalk(true);
                            givePrize(chr);
                            warpBack(chr);
                        }
                    }
                    //prizes here
		    finished = true;
                    return;
                }

                toSend.broadcastMessage(CField.showOXQuiz(question.getKey().left, question.getKey().right, true));
                toSend.broadcastMessage(CField.getClock(10)); //quickly change to 12
            }
        }, 10000);
        if (oxSchedule != null) {
            oxSchedule.cancel(false);
        }
        oxSchedule = TimerManager.getInstance().schedule(new Runnable() {

             @Override
             public void run() {
		if (finished) {
		    return;
		}
                toSend.broadcastMessage(CField.showOXQuiz(question.getKey().left, question.getKey().right, false));
                timesAsked++;
                for (MapleCharacter chr : toSend.getCharactersThreadsafe()) {
                    if (chr != null && !chr.isGM() && chr.isAlive()) { // make sure they aren't null... maybe something can happen in 12 seconds.
                        if (!isCorrectAnswer(chr, question.getValue().getAnswer())) {
                            chr.getStat().setHp((short) 0,chr);
                            chr.updateSingleStat(MapleStat.HP, 0);
                        } else {
                            chr.gainExp(3000, true, true, false);
                        }
                    }
                }
                sendQuestion();
             }
         }, 20000); // Time to answer = 30 seconds ( Ox Quiz packet shows a 30 second timer.
    }

    private boolean isCorrectAnswer(MapleCharacter chr, int answer) {
        double x = chr.getTruePosition().getX();
        double y = chr.getTruePosition().getY();
        if ((x > -234 && y > -26 && answer == 0) || (x < -234 && y > -26 && answer == 1)) {
            chr.dropMessage(6, "[Ox Quiz] Correct!"); //i think this is its own packet
            return true;
        }
        chr.dropMessage(6, "[Ox Quiz] Incorrect!");
        return false;
    }
}

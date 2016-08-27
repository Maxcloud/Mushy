/*  
  This file is part of Mushy.
  Copyright (c) 2015 ~ 2016 Maxcloud <no-email@provided.com>
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.  See the file
  COPYING included with this distribution for more information.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.quest;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import client.MapleCharacter;
import client.MapleTrait.MapleTraitType;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import lib.ReadBin;

/**
 * 
 * @author Maxcloud
 */
class MapleQuestRequirement implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;

    private MapleQuest quest;
    
    private boolean dayByDay;
    private boolean normalAutoStart;
    
    private short lvmin, lvmax;
    private short mbmin;
    private short charismaMin, charmMin, craftMin, insightMin, senseMin, willMin;
    private short pop;
    private short pettamenessmin;
    private short subJobFlag;
    
    private int npc;
    private int interval;
    
    private String end;
    private String startscript, endscript;
    
    private List<Short> jobs = new LinkedList<Short>();
    private List<Integer> fieldEnter = new LinkedList<Integer>();
    private List<Integer> pets = new LinkedList<Integer>();
    
    private Map<Integer, Boolean> skills = new LinkedHashMap<Integer, Boolean>();
    private Map<Integer, Byte> quests = new LinkedHashMap<Integer, Byte>();
    private Map<Integer, Integer> items = new LinkedHashMap<Integer, Integer>();
    private Map<Integer, Integer> mobs = new LinkedHashMap<Integer, Integer>();
    
    
    
    /**
     * Creates a new instance of MapleQuestRequirement.
     * @param quest
     * @param data
     * @throws IOException
     */
    protected MapleQuestRequirement(MapleQuest quest, ReadBin data) throws IOException {
    	this.quest = quest;
    	
    	this.dayByDay = data.readByte() > 0;
    	this.normalAutoStart = data.readByte() > 0;
    	
    	this.lvmin = data.readShort();
    	this.lvmax = data.readShort();
    	this.mbmin = data.readShort();
    	
    	this.charismaMin = data.readShort();
    	this.charmMin = data.readShort();
    	this.craftMin = data.readShort();
    	this.insightMin = data.readShort();
    	this.senseMin = data.readShort();
    	this.willMin = data.readShort();
    	
    	this.pop = data.readShort();
    	this.pettamenessmin = data.readShort();
    	this.subJobFlag = data.readShort();
    	this.npc = data.readInt();																																																																
    	this.interval = data.readInt();
    	
    	this.end = data.readString();
    	this.startscript = data.readString();
    	this.endscript = data.readString();
    	
    	// fieldenter
    	short size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		int field = data.readInt();
    	
    		fieldEnter.add(field);
    	}
    																																							
    	// job
    	size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		short job = data.readShort();

    		jobs.add(job);
    	}
    	
    	// skill
    	size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		int id = data.readInt();
    		boolean acquire = data.readByte() > 0;
    		
    		skills.put(id, acquire);
    	}
    	
    	// quest
    	size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		int id = data.readInt();
    		byte state = data.readByte();
    		byte order = data.readByte();
    		
    		quests.put(id, state);
    	}
    	
    	// item
    	size = data.readShort();
    	for(int i = 0; i < size ; i++) {
    		int id = data.readInt();
    		int count = data.readInt();
    		byte order = data.readByte();

    		items.put(id, count);
    	}
    	
    	// mob
    	size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		int id = data.readInt();
    		int count = data.readInt();
    		
    		mobs.put(id, count);
    	}
    	
    	// pet
    	size = data.readShort();
    	for(int i = 0; i < size; i++) {
    		int id = data.readInt();
    		
    		pets.add(id);
    	}

    }
    
    public int getInterval() {
    	return interval;
    }
    
    public boolean getNormalAutoStart() {
    	return normalAutoStart;
    }
    
    public String getStartScript() {
    	return startscript;
    }
    
    public String getEndScript() {
    	return endscript;
    }
    
    public Map<Integer, Integer> getMobs() {
    	return mobs;
    }
    
    
    /**
     * Provide a check using quest requirements.
     * @param c
     * @param npcid
     * @return
     */
    protected boolean check(MapleCharacter c, int npcid) {
    	
    	if(dayByDay) {
    		// TODO: Handle 'dayByDay'.
    	}
    	
    	if (normalAutoStart) {
    		// TODO: Handle 'normalAutoStart'.
    	}
    	
    	if (lvmin > -1) {
    		return c.getLevel() >= lvmin;
    	}
    	
    	if (lvmax > -1) {
    		return c.getLevel() >= lvmax;
    	}
    	
    	if (mbmin > 0) {
    		return c.getMonsterBook().getSeen() >= mbmin;
    	}
    	
    	// traits
    	if (charismaMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("charisma")).getLevel() >= charismaMin;
    	}
    	
    	if (charmMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("charm")).getLevel() >= charmMin;
    	}

    	if (craftMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("craft")).getLevel() >= craftMin;
    	}
    	
    	if (insightMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("insight")).getLevel() >= insightMin;
    	}
    	
    	if (senseMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("sense")).getLevel() >= senseMin;
    	}
    	
    	if (willMin > 0) {
    		return c.getTrait(MapleTraitType.getByQuestName("will")).getLevel() >= willMin;
    	}
    	
    	if (pop > 0) {
    		return c.getFame() >= pop;
    	}
    	
    	if (pettamenessmin > 0) {
    		Stream<MaplePet> pets = c.getPets().stream();
    		
    		Predicate<MaplePet> pet = p-> p.getSummoned() &&
    			p.getCloseness() >= pettamenessmin;
    		
    		return pets.anyMatch(pet);
    	}
    	
    	if (subJobFlag > 0) {
    		return c.getSubcategory() == (subJobFlag / 2);
    	}
    	
    	if (npc > 0) {
    		return npcid == npc;
    	}
    	
    	if (interval > -1) {
    		long time = System.currentTimeMillis() - interval * 60 * 1000L;
    		return c.getQuest(quest).getStatus() != 2 || c.getQuest(quest).getCompletionTime() <= time;
    	}
    	
    	if (!end.isEmpty()) {
    		String timeStr = end;
            
    		if (timeStr == null || timeStr.length() <= 0)
                return true;
            
            Calendar cal = Calendar.getInstance();
            
            int year = Integer.parseInt(timeStr.substring(0, 4));
            int month = Integer.parseInt(timeStr.substring(4, 6));
            int date = Integer.parseInt(timeStr.substring(6, 8));
            int hour = Integer.parseInt(timeStr.substring(8, 10));
            
            cal.set(year, month, date, hour, 0);
            
            return cal.getTimeInMillis() >= System.currentTimeMillis();
    	}
    	
    	if (!startscript.isEmpty()) {
    		// TODO: Handle 'startscript'.
    	}
    	
    	if (!endscript.isEmpty()) {
    		// TODO: Handle 'endscript'.
    	}
    	
    	if (!fieldEnter.isEmpty()) {
    		return fieldEnter.contains(c.getMapId());
    	}
    	
    	if (!jobs.isEmpty()) {
    		return (jobs.contains(c.getJob()) || c.isGM());
    	}
    	
    	if (!skills.isEmpty()) {
    		for (Entry<Integer, Boolean> a : skills.entrySet()) {
                
    			int skillid = a.getKey();
    			boolean acquire = a.getValue();
                
                Skill skill = SkillFactory.getSkill(skillid);
                if (acquire) {
                	int s = skill.isFourthJob() ? c.getMasterLevel(skill) : c.getSkillLevel(skill);
                	if (s == 0) {
                		return false;
                	}
                } else {
                    if (c.getSkillLevel(skill) > 0 || c.getMasterLevel(skill) > 0) {
                        return false;
                    }
                }
            }
            return true;
    	}
    	
    	if (!quests.isEmpty()) {
    		for (Entry<Integer, Byte> a : quests.entrySet()) {
                MapleQuestStatus q = c.getQuest(MapleQuest.getInstance(a.getKey()));
                byte state = a.getValue();
                
                if (state != 0) {
                    if (q == null && state == 0) {
                        continue;
                    }
                    if (q == null || q.getStatus() != state) {
                        return false;
                    }
                }
            }
            return true;
    	}
    	
    	if (!items.isEmpty()) {
             for (Entry<Integer, Integer> a : items.entrySet()) {
                 int itemid = a.getKey();
                 short quantity = 0;
                 MapleInventoryType iType = GameConstants.getInventoryType(itemid);
                 
                 for (Item item : c.getInventory(iType).listById(itemid)) {
                     quantity += item.getQuantity();
                 }
                 
                 int count = a.getValue();
                 if (quantity < count || (count <= 0 && quantity > 0)) {
                     return false;
                 }
             }
             return true;
    	}
    	
    	if (!mobs.isEmpty()) {
    		for (Entry<Integer, Integer> a : mobs.entrySet()) {
                int mobId = a.getKey();
                int killReq = a.getValue();
                
                if (c.getQuest(quest).getMobKills(mobId) < killReq) {
                    return false;
                }
            }
            return true;
    	}
    	
    	if (!pets.isEmpty()) {
    		return pets.stream().anyMatch(id-> c.getPetById(id) != -1);
    	}
    	
    	return true;
    }

}

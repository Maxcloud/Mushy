package server;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.World;
import server.Timer.EtcTimer;
import server.carnival.MapleCarnivalChallenge;
import server.maps.MapleMap;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleSquad {

    public static enum MapleSquadType {

        bossbalrog(2), zak(2), chaoszak(3), horntail(2), chaosht(3), pinkbean(2), nmm_squad(2), vergamot(2), dunas(2), nibergen_squad(2), dunas2(2), core_blaze(2), aufheben(2), cwkpq(3), tokyo_2095(2), vonleon(3), scartar(2), cygnus(3), hilla(2), darkhilla(2), arkarium(3);

        private MapleSquadType(int i) {
            this.i = i;
        }
        public int i;
        public HashMap<Integer, ArrayList<Pair<String, String>>> queuedPlayers = new HashMap<>();
        public HashMap<Integer, ArrayList<Pair<String, Long>>> queue = new HashMap<>();
    }

    private WeakReference<MapleCharacter> leader;
    private final String leaderName, toSay;
    private final Map<String, String> members = new LinkedHashMap<>();
    private final Map<String, String> bannedMembers = new LinkedHashMap<>();
    private final int ch;
    private final long startTime;
    private final int expiration;
    private final int beginMapId;
    private final MapleSquadType type;
    private byte status = 0;
    private ScheduledFuture<?> removal;

    public MapleSquad(final int ch, final String type, final MapleCharacter leader, final int expiration, final String toSay) {
        this.leader = new WeakReference<>(leader);
        this.members.put(leader.getName(), MapleCarnivalChallenge.getJobBasicNameById(leader.getJob()));
        this.leaderName = leader.getName();
        this.ch = ch;
        this.toSay = toSay;
        this.type = MapleSquadType.valueOf(type.toLowerCase());
        this.status = 1;
        this.beginMapId = leader.getMapId();
        leader.getMap().setSquad(this.type);
        if (this.type.queue.get(ch) == null) {
            this.type.queue.put(ch, new ArrayList<Pair<String, Long>>());
            this.type.queuedPlayers.put(ch, new ArrayList<Pair<String, String>>());
        }
        this.startTime = System.currentTimeMillis();
        this.expiration = expiration;
    }

    public void copy() {
        while (type.queue.get(ch).size() > 0 && ChannelServer.getInstance(ch).getMapleSquad(type) == null) {
            int index = 0;
            long lowest = 0;
            for (int i = 0; i < type.queue.get(ch).size(); i++) {
                if (lowest == 0 || type.queue.get(ch).get(i).right < lowest) {
                    index = i;
                    lowest = type.queue.get(ch).get(i).right;
                }
            }
            final String nextPlayerId = type.queue.get(ch).remove(index).left;
            final int theirCh = World.Find.findChannel(nextPlayerId);
            if (theirCh > 0) {
                final MapleCharacter lead = ChannelServer.getInstance(theirCh).getPlayerStorage().getCharacterByName(nextPlayerId);
                if (lead != null && lead.getMapId() == beginMapId && lead.getClient().getChannel() == ch) {
                    final MapleSquad squad = new MapleSquad(ch, type.name(), lead, expiration, toSay);
                    if (ChannelServer.getInstance(ch).addMapleSquad(squad, type.name())) {
                        getBeginMap().broadcastMessage(CField.getClock(expiration / 1000));
                        getBeginMap().broadcastMessage(CWvsContext.broadcastMsg(6, nextPlayerId + toSay));
                        type.queuedPlayers.get(ch).add(new Pair<>(nextPlayerId, "Success"));
                    } else {
                        squad.clear();
                        type.queuedPlayers.get(ch).add(new Pair<>(nextPlayerId, "Skipped"));
                    }
                    break;
                } else {
                    if (lead != null) {
                        lead.dropMessage(6, "Your squad has been skipped due to you not being in the right channel and map.");
                    }
                    getBeginMap().broadcastMessage(CWvsContext.broadcastMsg(6, nextPlayerId + "'s squad has been skipped due to the player not being in the right channel and map."));
                    type.queuedPlayers.get(ch).add(new Pair<>(nextPlayerId, "Not in map"));
                }
            } else {
                getBeginMap().broadcastMessage(CWvsContext.broadcastMsg(6, nextPlayerId + "'s squad has been skipped due to the player not being online."));
                type.queuedPlayers.get(ch).add(new Pair<>(nextPlayerId, "Not online"));
            }
        }
    }

    public MapleMap getBeginMap() {
        return ChannelServer.getInstance(ch).getMapFactory().getMap(beginMapId);
    }

    public void clear() {
        if (removal != null) {
            getBeginMap().broadcastMessage(CField.stopClock());
            removal.cancel(false);
            removal = null;
        }
        members.clear();
        bannedMembers.clear();
        leader = null;
        ChannelServer.getInstance(ch).removeMapleSquad(type);
        this.status = 0;
    }

    public MapleCharacter getChar(String name) {
        return ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(name);
    }

    public long getTimeLeft() {
        return expiration - (System.currentTimeMillis() - startTime);
    }

    public void scheduleRemoval() {
        removal = EtcTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (status != 0 && leader != null && (getLeader() == null || status == 1)) { //leader itself = null means we're already cleared
                    clear();
                    copy();
                }
            }
        }, expiration);
    }

    public String getLeaderName() {
        return leaderName;
    }

    public List<Pair<String, Long>> getAllNextPlayer() {
        return type.queue.get(ch);
    }

    public String getNextPlayer() {
        StringBuilder sb = new StringBuilder("\nQueued members : ");
        sb.append("#b").append(type.queue.get(ch).size()).append(" #k ").append("List of participants : \n\r ");
        int i = 0;
        for (Pair<String, Long> chr : type.queue.get(ch)) {
            i++;
            sb.append(i).append(" : ").append(chr.left);
            sb.append(" \n\r ");
        }
        sb.append("Would you like to #ebe next#n in the queue, or #ebe removed#n from the queue if you are in it?");
        return sb.toString();
    }

    public void setNextPlayer(String i) {
        Pair<String, Long> toRemove = null;
        for (Pair<String, Long> s : type.queue.get(ch)) {
            if (s.left.equals(i)) {
                toRemove = s;
                break;
            }
        }
        if (toRemove != null) {
            type.queue.get(ch).remove(toRemove);
            return;
        }
        for (ArrayList<Pair<String, Long>> v : type.queue.values()) {
            for (Pair<String, Long> s : v) {
                if (s.left.equals(i)) {
                    return;
                }
            }
        }
        type.queue.get(ch).add(new Pair<>(i, System.currentTimeMillis()));
    }

    public MapleCharacter getLeader() {
        if (leader == null || leader.get() == null) {
            if (members.size() > 0 && getChar(leaderName) != null) {
                leader = new WeakReference<>(getChar(leaderName));
            } else {
                if (status != 0) {
                    clear();
                }
                return null;
            }
        }
        return leader.get();
    }

    public boolean containsMember(MapleCharacter member) {
        for (String mmbr : members.keySet()) {
            if (mmbr.equalsIgnoreCase(member.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<String> getMembers() {
        return new LinkedList<>(members.keySet());
    }

    public List<String> getBannedMembers() {
        return new LinkedList<>(bannedMembers.keySet());
    }

    public int getSquadSize() {
        return members.size();
    }

    public boolean isBanned(MapleCharacter member) {
        return bannedMembers.containsKey(member.getName());
    }

    public int addMember(MapleCharacter member, boolean join) {
        if (getLeader() == null) {
            return -1;
        }
        final String job = MapleCarnivalChallenge.getJobBasicNameById(member.getJob());
        if (join) {
            if (!containsMember(member) && !getAllNextPlayer().contains(member.getName())) {
                if (members.size() <= 30) {
                    members.put(member.getName(), job);
                    getLeader().dropMessage(5, member.getName() + " (" + job + ") has joined the fight!");
                    return 1;
                }
                return 2;
            }
            return -1;
        } else {
            if (containsMember(member)) {
                members.remove(member.getName());
                getLeader().dropMessage(5, member.getName() + " (" + job + ") have withdrawed from the fight.");
                return 1;
            }
            return -1;
        }
    }

    public void acceptMember(int pos) {
        if (pos < 0 || pos >= bannedMembers.size()) {
            return;
        }
        final List<String> membersAsList = getBannedMembers();
        final String toadd = membersAsList.get(pos);
        if (toadd != null && getChar(toadd) != null) {
            members.put(toadd, bannedMembers.get(toadd));
            bannedMembers.remove(toadd);

            getChar(toadd).dropMessage(5, getLeaderName() + " has decided to add you back to the squad.");
        }
    }

    public void reAddMember(MapleCharacter chr) {
        removeMember(chr);
        members.put(chr.getName(), MapleCarnivalChallenge.getJobBasicNameById(chr.getJob()));
    }

    public void removeMember(MapleCharacter chr) {
        if (members.containsKey(chr.getName())) {
            members.remove(chr.getName());
        }
    }

    public void removeMember(String chr) {
        if (members.containsKey(chr)) {
            members.remove(chr);
        }
    }

    public void banMember(int pos) {
        if (pos <= 0 || pos >= members.size()) { //may not ban leader
            return;
        }
        final List<String> membersAsList = getMembers();
        final String toban = membersAsList.get(pos);
        if (toban != null && getChar(toban) != null) {
            bannedMembers.put(toban, members.get(toban));
            members.remove(toban);

            getChar(toban).dropMessage(5, getLeaderName() + " has removed you from the squad.");
        }
    }

    public void setStatus(byte status) {
        this.status = status;
        if (status == 2 && removal != null) {
            removal.cancel(false);
            removal = null;
        }
    }

    public int getStatus() {
        return status;
    }

    public int getBannedMemberSize() {
        return bannedMembers.size();
    }

    public String getSquadMemberString(byte type) {
        switch (type) {
            case 0: {
                StringBuilder sb = new StringBuilder("Squad members : ");
                sb.append("#b").append(members.size()).append(" #k ").append("List of participants : \n\r ");
                int i = 0;
                for (Entry<String, String> chr : members.entrySet()) {
                    i++;
                    sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(Leader of the squad)");
                    }
                    sb.append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            }
            case 1: {
                StringBuilder sb = new StringBuilder("Squad members : ");
                sb.append("#b").append(members.size()).append(" #n ").append("List of participants : \n\r ");
                int i = 0, selection = 0;
                for (Entry<String, String> chr : members.entrySet()) {
                    i++;
                    sb.append("#b#L").append(selection).append("#");
                    selection++;
                    sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(Leader of the squad)");
                    }
                    sb.append("#l").append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            }
            case 2: {
                StringBuilder sb = new StringBuilder("Squad members : ");
                sb.append("#b").append(members.size()).append(" #n ").append("List of participants : \n\r ");
                int i = 0, selection = 0;
                for (Entry<String, String> chr : bannedMembers.entrySet()) {
                    i++;
                    sb.append("#b#L").append(selection).append("#");
                    selection++;
                    sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
                    sb.append("#l").append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            }
            case 3: { //CWKPQ
                StringBuilder sb = new StringBuilder("Jobs : ");
                final Map<String, Integer> jobs = getJobs();
                for (Entry<String, Integer> chr : jobs.entrySet()) {
                    sb.append("\r\n").append(chr.getKey()).append(" : ").append(chr.getValue());
                }
                return sb.toString();
            }
        }
        return null;
    }

    public final MapleSquadType getType() {
        return type;
    }

    public final Map<String, Integer> getJobs() {
        final Map<String, Integer> jobs = new LinkedHashMap<>();
        for (Entry<String, String> chr : members.entrySet()) {
            if (jobs.containsKey(chr.getValue())) {
                jobs.put(chr.getValue(), jobs.get(chr.getValue()) + 1);
            } else {
                jobs.put(chr.getValue(), 1);
            }
        }
        return jobs;
    }
}

package server.buffs.buffclasses.cygnus;

import client.MapleBuffStat;
import constants.GameConstants;
import server.MapleStatEffect;
import server.MapleStatInfo;
import server.buffs.AbstractBuffClass;

/**
 *
 * @author Maple
 */
public class WindArcherBuff extends AbstractBuffClass {

    public WindArcherBuff() {
        buffs = new int[]{
            13001022, //Storm Elemental
            13101024, //Sylvan Aid
            13101023, //Bow Booster
            13111023, //Albatross
            13111024, //Emerald Flower
            13121004, //Touch of the Wind
            13121005, //Sharp Eyes
            13121053, //Glory of the Guardians
            13121054, //Storm Bringer
        };
    }
    
    @Override
    public boolean containsJob(int job) {
        return GameConstants.isKOC(job) && (job / 100) % 10 == 3;
    }

    @Override
    public void handleBuff(MapleStatEffect eff, int skill) {
        switch (skill) {
            case 13001022:// Storm Elemental
                eff.statups.put(MapleBuffStat.STORM_ELEMENTAL, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.IndieDamR, eff.info.get(MapleStatInfo.indieDamR));
                break;
            case 13101023:// Bow Booster
                eff.statups.put(MapleBuffStat.Booster, eff.info.get(MapleStatInfo.x));
                break;
            case 13111024:// Emerald Flower
                //spawn
                break;
            case 13101024:// Sylvan Aid
                eff.statups.put(MapleBuffStat.IndieCr, eff.info.get(MapleStatInfo.x));    
                eff.statups.put(MapleBuffStat.IndiePAD, eff.info.get(MapleStatInfo.indiePad));
                eff.statups.put(MapleBuffStat.SoulArrow, eff.info.get(MapleStatInfo.x));
                 break;
               case 13111023:// Albatross
                eff.statups.put(MapleBuffStat.Albatross, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.IndiePAD, eff.info.get(MapleStatInfo.indiePad));
                eff.statups.put(MapleBuffStat.IncMaxHP, eff.info.get(MapleStatInfo.indieMhp));
                eff.statups.put(MapleBuffStat.IndieBooster, eff.info.get(MapleStatInfo.indieBooster));
                eff.statups.put(MapleBuffStat.IndieCr, eff.info.get(MapleStatInfo.indieCr));
                break;
            case 13121004:// Touch of the Wind
                eff.statups.put(MapleBuffStat.TOUCH_OF_THE_WIND2, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.IllusionStep, eff.info.get(MapleStatInfo.y));
                eff.statups.put(MapleBuffStat.TOUCH_OF_THE_WIND1, eff.info.get(MapleStatInfo.prop));
                eff.statups.put(MapleBuffStat.IndieMHPR, eff.info.get(MapleStatInfo.indieMhpR));
                break;
            case 13121005:// Sharp Eyes
                eff.statups.put(MapleBuffStat.SharpEyes, (eff.info.get(MapleStatInfo.x) << 8) + eff.info.get(MapleStatInfo.criticaldamageMax));
                break;
            case 13121053:// Glory of the Guardians
                eff.statups.put(MapleBuffStat.IndieDamR, eff.info.get(MapleStatInfo.indieDamR));
                eff.statups.put(MapleBuffStat.IncMaxDamage, eff.info.get(MapleStatInfo.indieMaxDamageOver));
                break;
            case 13121054:// Storm Bringer
                eff.statups.put(MapleBuffStat.StormBringer, eff.info.get(MapleStatInfo.x));
                break;
            default:
                System.out.println("Unhandled Buff: " + skill);
                break;
        }
    }
}

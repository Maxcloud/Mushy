/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.buffs.buffclasses.resistance;

import client.MapleBuffStat;
import constants.GameConstants;
import server.MapleStatEffect;
import server.MapleStatInfo;
import server.buffs.AbstractBuffClass;

/**
 *
 * @author Sunny
 */
public class DemonBuff extends AbstractBuffClass {
    
    public DemonBuff() {
        buffs = new int[]{
                31001001, // booster
                31101003, // Vengeance
                31111004, // Black-Hearted Strength
                31121007, // Boundless Rage
                31121004, // MW
                31121002, // Leech Aura
                31011001, // Overload Release
                31201002, // Battle Pact
                31201003, // Abyssal Connection
                31211003, // Ward Evil
                31211004, // Diabolic Recovery
                31221004, // Overwhelming Power
                31221008, // MW
                31121054, // Blue Blood
                31221054,
        };
    }
    
    @Override
    public boolean containsJob(int job) {
        return GameConstants.isDemonSlayer(job) || GameConstants.isDemonAvenger(job);
    }

    @Override
    public void handleBuff(MapleStatEffect eff, int skill) {
        switch (skill) {
           case 31001001:
           case 31201002: // booster
                eff.statups.put(MapleBuffStat.Booster, eff.info.get(MapleStatInfo.x) * 2);
                break;
           case 31011001: // Overload Release
                eff.statups.put(MapleBuffStat.IndieMHPR, eff.info.get(MapleStatInfo.indieMhpR));
                break;
           case 31101003: // Vengeance
                eff.statups.put(MapleBuffStat.PowerGuard, eff.info.get(MapleStatInfo.y));
                break;
           case 31201003: // Abyssal Connection 
               eff.statups.put(MapleBuffStat.IndiePAD, eff.info.get(MapleStatInfo.indiePad));
                break;
           case 31211003: // Ward Evil
                eff.statups.put(MapleBuffStat.DamAbsorbShield, eff.info.get(MapleStatInfo.y));
                eff.statups.put(MapleBuffStat.TerR, eff.info.get(MapleStatInfo.z));
                eff.statups.put(MapleBuffStat.AsrR, eff.info.get(MapleStatInfo.x));
                break;
           case 31211004: // Diabolic Recovery
                eff.statups.put(MapleBuffStat.DiabolikRecovery, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.IndieMHPR, eff.info.get(MapleStatInfo.indieMhpR));
                break;
           case 31221004:
                    eff.statups.put(MapleBuffStat.IndieDamR, eff.info.get(MapleStatInfo.indieDamR));
                    eff.statups.put(MapleBuffStat.IndieBooster, +2);
          //          eff.statups.put(MapleBuffStat.Booster, 2);
                    break;
           case 31111004: // Black-Hearted Strength
                eff.statups.put(MapleBuffStat.AsrR, eff.info.get(MapleStatInfo.y));
                eff.statups.put(MapleBuffStat.TerR, eff.info.get(MapleStatInfo.z));
                eff.statups.put(MapleBuffStat.DEFENCE_BOOST_R, eff.info.get(MapleStatInfo.x));
                break;
           case 31121007: // Boundless Rage
                eff.statups.put(MapleBuffStat.InfinityForce, 1);
                break;
           case 31121004: // MW
           case 31221008: // MW
                eff.statups.put(MapleBuffStat.BasicStatUp, eff.info.get(MapleStatInfo.x));
                break;
           case 31121002: //Leech Aura
                eff.statups.put(MapleBuffStat.LEECH_AURA, eff.info.get(MapleStatInfo.x));
                break;
           case 31121054:
                eff.statups.put(MapleBuffStat.ShadowPartner, eff.info.get(MapleStatInfo.x));
                break;
           case 31221054:
               eff.statups.put(MapleBuffStat.IndieMHPR, eff.info.get(MapleStatInfo.indieMhpR));
            default:
           //     System.out.println("Unhandled Demon Buff: " + skill);
                break;
        }
    }
}

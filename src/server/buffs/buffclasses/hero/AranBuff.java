/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.buffs.buffclasses.hero;

import client.MapleBuffStat;
import constants.GameConstants;
import server.MapleStatEffect;
import server.MapleStatInfo;
import server.buffs.AbstractBuffClass;

/**
 *
 * @author Charmander
 */
public class AranBuff extends AbstractBuffClass {

    public AranBuff() {
        buffs = new int[]{
            21001003, // Polearm Booster
            21101006, // Snow Charge
            21101005, // Combo Drain
            21111001, // Might
            21111009, // Combo Recharge
            21111012, // Maha Blessing
            21121007, // Combo Barrier
            21121000, // Maple Warrior
            21121054, // Unlimited Combo
            21121053, // Heroic Memories
        };
    }

    @Override
    public boolean containsJob(int job) {
        return GameConstants.isAran(job);
    }

    @Override
    public void handleBuff(MapleStatEffect eff, int skill) {
        switch (skill) {
            case 21001003: // Polearm Booster
                eff.statups.put(MapleBuffStat.Booster, eff.info.get(MapleStatInfo.x));
                break;
            case 21101006: // Snow Charge
                eff.statups.put(MapleBuffStat.WeaponCharge, eff.info.get(MapleStatInfo.x));
                break;
            case 21101005: // Combo Drain
                eff.statups.put(MapleBuffStat.ComboDrain, eff.info.get(MapleStatInfo.x));
                break;
            case 21111001: // Might
                eff.statups.put(MapleBuffStat.KnockBack, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.EPAD, eff.info.get(MapleStatInfo.epad));
                eff.statups.put(MapleBuffStat.EPDD, eff.info.get(MapleStatInfo.epdd));
                eff.statups.put(MapleBuffStat.EMDD, eff.info.get(MapleStatInfo.emdd));
                break;
            case 21111009: // Combo Recharge
                eff.statups.put(MapleBuffStat.ComboAbilityBuff, eff.info.get(MapleStatInfo.x));
                break;
            case 21111012: // Maha Blessing
                eff.statups.put(MapleBuffStat.MAD, eff.info.get(MapleStatInfo.mad));
                eff.statups.put(MapleBuffStat.PAD, eff.info.get(MapleStatInfo.pad));
                break;
            case 21121007: // Combo Barrier
                eff.statups.put(MapleBuffStat.ComboBarrier, eff.info.get(MapleStatInfo.x));
                eff.statups.put(MapleBuffStat.MDD, eff.info.get(MapleStatInfo.mdd));
                eff.statups.put(MapleBuffStat.PDD, eff.info.get(MapleStatInfo.pdd));
                break;
            case 21121000: // Maple Warrior
                eff.statups.put(MapleBuffStat.BasicStatUp, eff.info.get(MapleStatInfo.x));
                break;
            case 21121054: // Unlimited Combo
                eff.statups.put(MapleBuffStat.DEFAULT_BUFFSTAT, eff.info.get(MapleStatInfo.indieDamR));
                break;
            case 21121053: // Heroic Memories
                eff.statups.put(MapleBuffStat.IncMaxDamage, eff.info.get(MapleStatInfo.indieMaxDamageOver));
                eff.statups.put(MapleBuffStat.IndieDamR, eff.info.get(MapleStatInfo.indieDamR));
                break;
            default:
                //System.out.println("Aran skill not coded: " + skill);
                break;
        }
    }
}

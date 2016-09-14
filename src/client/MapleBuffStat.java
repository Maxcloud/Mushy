package client;

import java.io.Serializable;
import handling.Buffstat;

public enum MapleBuffStat implements Serializable, Buffstat {

	// 0
	DAMAGE_PERCENT(6, true),
	ANGEL_ACC(16),
	
    // 2
	STANCE(67),
    MAPLE_WARRIOR(68),
    BOOSTER(91),
    
    // 8
    DAMAGE_ABSORBED(280),
	
    // 17
	INDIE_PAD(558, true),
	
	// 1st
    WATK(0x1, 1),
    WDEF(0x2, 1),
    MATK(0x4, 1),
    MDEF(0x8, 1),
    ACC(0x10, 1),
    AVOID(0x20, 1),
    HANDS(0x40, 1),
    SPEED(0x80, 1),
    JUMP(0x100, 1),
    MAGIC_GUARD(0x200, 1),
    DARKSIGHT(0x400, 1),
    POWERGUARD(0x1000, 1),
    MAXHP(0x2000, 1),
    MAXMP(0x4000, 1),
    INVINCIBLE(0x8000, 1),
    SOULARROW(0x10000, 1),
    STUN(0x20000, 1),
    POISON(0x40000, 1),
    SEAL(0x80000, 1),
    DARKNESS(0x100000, 1),
    COMBO(0x200000, 1),
    SUMMON(0x200000, 1),
    WK_CHARGE(0x400000, 1),
    DRAGONBLOOD(0x800000, 1),
    HOLY_SYMBOL(0x1000000, 1),
    MESOUP(0x2000000, 1),
    CRITICAL_DAMAGE(32768, 6),
    SHADOWPARTNER(0x4000000, 1), // d
    PICKPOCKET(0x8000000, 1),
    PUPPET(0x8000000, 1),
    MESOGUARD(0x10000000, 1),
    HP_LOSS_GUARD(0x20000000, 1),
    WEAKEN(0x40000000, 1),
    CURSE(0x80000000, 1),
    
    // 2nd
    SLOW(0x1, 2),
    MORPH(0x2, 2),
    RECOVERY(0x4, 2),

    STATUS_RESIST(0x10, 2),
    SHARP_EYES(0x20, 2),
    ELEMENT_RESIST(0x20, 2),
    MANA_REFLECTION(0x40, 2),
    SEDUCE(0x80, 2),
    SPIRIT_CLAW(0x100, 2),
    INFINITY(0x200, 2),
    HOLY_SHIELD(0x400, 2),
    HAMSTRING(0x800, 2),
    BLIND(0x1000, 2),
    CONCENTRATE(0x2000, 2),
    ZOMBIFY(0x4000, 2),
    ECHO_OF_HERO(0x8000, 2),
    MESO_RATE(0x10000, 2),
    GHOST_MORPH(0x20000, 2),
    ARIANT_COSS_IMU(0x40000, 2),
    REVERSE_DIRECTION(0x80000, 2),
    DROP_RATE(0x100000, 2), //d
    EXPRATE(0x400000, 2),
    ACASH_RATE(0x800000, 2),
    ILLUSION(0x1000000, 2),
    //2 and 4 are unknown
    BERSERK_FURY(0x8000000, 2),
    DIVINE_BODY(0x10000000, 2),
    SPARK(0x20000000, 2),
    ARIANT_COSS_IMU2(0x40000000, 2),
    FINALATTACK(0x80000000, 2),

    // 3rd
    ELEMENT_RESET(0x2, 3),
    WIND_WALK(0x4, 3),
    ARAN_COMBO(0x4, 3),
    COMBO_DRAIN(0x8, 3),
    COMBO_BARRIER(0x10, 3),
    BODY_PRESSURE(0x20, 3),
    SMART_KNOCKBACK(0x40, 3),
    PYRAMID_PQ(0x200, 3),
    POTION_CURSE(0x800, 3),
    SHADOW(0x1000, 3),
    BLINDNESS(0x2000, 3),
    SLOWNESS(0x4000, 3),
    MAGIC_SHIELD(0x8000, 3),
    MAGIC_RESISTANCE(16384, 3),
    SOUL_STONE(0x20000, 3),
    SOARING(0x10000, 3),
    FREEZE(0x80000, 3),
    LIGHTNING_CHARGE(0x100000, 3),
    LIGHTNING(0x80, 10),
    ENRAGE(0x200000, 3),
    OWL_SPIRIT(0x400000, 3),
    FINAL_CUT(0x1000000, 3),
    DAMAGE_BUFF(0x2000000, 3),
    ATTACK_BUFF(16777216, 3),
    RAINING_MINES(0x8000000, 3),
    ENHANCED_MAXHP(0x4000000, 3),
    ENHANCED_MAXMP(0x4000000, 3),
    ENHANCED_WATK(0x10000000, 3),
    ENHANCED_MATK(0x20000000, 3),
    ENHANCED_WDEF(0x40000000, 3),
    ENHANCED_MDEF(0x80000000, 3),
    
    // 4th
    PERFECT_ARMOR(0x1, 4),
    SATELLITESAFE_PROC(0x2, 4),
    SATELLITESAFE_ABSORB(0x4, 4),
    TORNADO(0x20, 4),
    CRITICAL_RATE_BUFF(0x40, 4),
    MP_BUFF(0x80, 4),
    DAMAGE_TAKEN_BUFF(0x100, 4),
    DODGE_CHANGE_BUFF(0x200, 4),
    CONVERSION(0x400, 4),
    REAPER(0x800, 4),
    INFILTRATE(0x1000, 4),
    MECH_CHANGE(0x2000, 4),
    FIRE_AURA(0x80000000, 10),
    AURA(4096, 4),
    DARK_AURA(0x2000, 4),
    BLUE_AURA(0x4000, 4),
    YELLOW_AURA(0x8000, 4),
    BODY_BOOST(0x10000, 4),
    FELINE_BERSERK(0x20000, 4),
    OverWelming_PWR(0x80000000, 7),
    INDIEBOOSTER(4096, 7, true),
    DICE_ROLL(0x40000, 4),
    DIVINE_SHIELD(0x80000, 4),
    DAMAGE_RATE(0x100000, 4),
    TELEPORT_MASTERY(0x200000, 4),
    COMBAT_ORDERS(0x400000, 4),
    BEHOLDER(0x200000, 4),
    DISABLE_POTENTIAL(0x4000000, 4),
    GIANT_POTION(0x8000000, 4),
    ONYX_SHROUD(0x10000000, 4),
    ONYX_WILL(0x8000000, 4),//-2
    TORNADO_CURSE(0x40000000, 4),
    BLESS(0x20000000, 4),
    PIRATES_REVENGE(1048576, 4),

    // 5th
    THREATEN_PVP(0x4, 5),
    ICE_KNIGHT(0x8, 5),
    STR(0x10, 5),//-2
    INT(0x20, 5),//-2
    DEX(0x40, 5),//-2
    LUK(0x80, 5),//-2
    ATTACK(0x100, 5),

    INDIE_MAD(0x800, 5, true),
    HP_BOOST(0x1000, 5, true),
    MP_BOOST(0x2000, 5, true),
    
    ANGEL_AVOID(0x8000, 5, true),
    ANGEL_JUMP(0x10000, 5, true),
    INDIE_SPEED(0x20000, 5, true),
    ANGEL_STAT(0x40000, 5, true),
    PVP_DAMAGE(0x200000, 5),
    PVP_ATTACK(0x400000, 5),
    INVINCIBILITY(0x800000, 5),
    HIDDEN_POTENTIAL(0x1000000, 5),
    ELEMENT_WEAKEN(0x2000000, 5),
    STACK_ALLSTATS(0x40000, 5),
    SNATCH(0x4000000, 5),
    FROZEN(0x8000000, 5),
    ICE_SKILL(0x20000000, 5),
    BOUNDLESS_RAGE(0x20000000, 5),
    
    // 6th
    PVP_FLAG(0x2, 6),
    HOLY_MAGIC_SHELL(0x1, 6),
    MANY_USES(0x4, 6, true),
    BUFF_MASTERY(0x200, 2),
    ABNORMAL_STATUS_R(0x10, 6),
    ELEMENTAL_STATUS_R(0x20, 6), 
    WATER_SHIELD(0x40, 6),
    DARK_METAMORPHOSIS(128, 6),
    BARREL_ROLL(0x1000, 6),
    DAMAGE_R(0x200, 6),
    MDEF_BOOST(0x2000, 6),
    WDEF_BOOST(0x4000, 6),
    SPIRIT_LINK(0x4000, 6, true),
    CRITICAL_RATE(0x8000, 6),
    VIRTUE_EFFECT(0x10000, 6),
    NO_SLIP(0x100000, 6),
    FAMILIAR_SHADOW(0x200000, 6),
    LEECH_AURA(0x2000000, 6),
    ABSORB_DAMAGE_HP(0x20000000, 6),
    DEFENCE_BOOST_R(0x4000000, 6),
    Dusk_Guard(0x2000, 6, true),

    // 7th
    HP_R(0x8, 7),
    UNKNOWN8(0x20, 7),
    ATTACK_SPEED(0x100, 7),
    ATTACK_SPEED2(0x100, 7, true),
    HP_BOOST_PERCENT(0x200, 7, true),
    MP_BOOST_PERCENT(0x400, 7, true),
    UNKNOWN12(0x1000, 7),
    MP_R(0x80000, 7, true),
    MOUNT_MORPH(0x100000, 7),
    UNKNOWN9(0x800000, 7),
    KILL_COUNT(0x800000, 7),
    IGNORE_DEF(0x2000000, 7),
    
    
    // 8th
    PHANTOM_MOVE(0x8, 8),
    JUDGMENT_DRAW(0x10, 8),
    HYBRID_DEFENSES(0x400, 8),
    UNKNOWN10(0x10, 8),
    LUMINOUS_GAUGE(0x200, 8),
    DARK_CRESCENDO(0x400, 8),
    BLACK_BLESSING(0x800, 8),
    PRESSURE_VOID(0x1000, 8),
    LUNAR_TIDE(0x2000, 8),
    KAISER_COMBO(0x8000, 8),
    KAISER_MORPH_GAUGE(0x8000, 1),
    IGNORE_MONSTER_DEF(0x10000, 5),
    KAISER_MODE_CHANGE(0x20000, 8),
    TEMPEST_BLADES(0x100000, 8),
    CRIT_DAMAGE(0x200000, 8),

    ENERGY_CHARGE(0x2000000, 8),
    DASH_SPEED(0x4000000, 12),
    DASH_JUMP(0x8000000, 12),
    MONSTER_RIDING(0x10000000, 10),
    STORM_BRINGER((int) 0x20000000000L, 3),
    SPEED_INFUSION(0x20000000, 8),
    HOMING_BEACON(0x40000000, 8),
    DAMAGE_CAP_INCREASE(0x40000000, 8, true),
    DEFAULT_BUFFSTAT(0x80000000, 8),
    
    // 9th
    PRETTY_EXALTATION(0x1, 9),
    KAISER_MAJESTY3(0x2, 9),
    KAISER_MAJESTY4(0x4, 9),
    PARTY_STANCE(0x10, 9),
    STATUS_RESIST_TWO(0x10, 9),
    ELEMENT_RESIST_TWO(0x20, 9, true),
    ANGEL(0x100, 9),
    BOWMASTERHYPER(0x400, 9),
    DAMAGE_RESIST(0x8000, 9),
    CRITICAL_PERCENT_UP(0x40000, 9),
    MOON_STANCE2(0x80000000, 9),
    ATTACK_COUNT(0x80000000, 9),
    EXCEED_ATTACK(0x4000000, 9),
    EXCEED(0x40000000, 9),
    DIABOLIC_RECOVERY(0x8000000, 9),
    BOSS_DAMAGE(0x10000, 9, true),
    BOSS_ATTDMG(16777216, 9),
    
    
    // 10th
    SUPPLY_SURPLUS(0x2, 10),
    XENON_FLY(0x10, 10),
    AMARANTH_GENERATOR(0x20, 10),
    STORM_ELEMENTAL(0x80, 10),
    PROP(0x100, 10),
    FROZEN_SHIKIGAMI(0x400, 10),
    TOUCH_OF_THE_WIND1(0x1000, 10),
    TOUCH_OF_THE_WIND2(0x2000, 10, true),
    ALBATROSS(0x4000, 10),
    SPEED_LEVEL(0x8000, 10),//0x8000, 10
    ADD_AVOIDABILITY(0x1000, 10),
    ACCURACY_PERCENT(0x20000, 10),
    WARRIOR_STANCE(0x10000, 10, true),
    SOUL_ELEMENT(0x40000, 10),
    EQUINOX_STANCE(0x80000, 10),
    SOLUNA_EFFECT(0x10000,10),
    
    // 12th
    BATTOUJUTSU_STANCE(0x2, 12),//confirm
    HAYATO_ATTACK_SPEED(0x4, 12),//confirm
    HAYATO_STANCE(0x100, 12),//not sure  
    BATTOUJUTSU_STANCEnew(0x40000, 11),
    CROSS_SURGE(0x8000000, 10),
    HP_RECOVER(0x4000000, 10),
    PARASHOCK_GUARD(0x80000000, 10, true),
    PASSIVE_BLESS(0x4, 11),
    DIVINE_FORCE_AURA(0x1000, 11),
    DIVINE_SPEED_AURA(0x2000, 11),
    HAYATO1(0x2, 12),
    HAYATO2(0x4, 12),
    SHIKIGAMI(0x10, 12),
    HAYATO3(0x20, 12),
    HAYATO4(0x40, 12),
    HAYATO5(0x80, 12),
    FOX_FIRE(0x2000, 12),
    HAKU_REBORN(0x4000, 12), // 0x100000, 10
    HAKU_BLESS(0x8000, 12),
    BEARASSAULT(0x6000, 1),
    ANIMAL_SELECT(0x100000, 12),
    ASURA(0x10000, 9);
	
    private static final long serialVersionUID = 0L;
    private final int buffstat;
    private final int pos;
    private boolean stacked = false;

    private MapleBuffStat(int buffstat, int pos) {
        this.buffstat = buffstat;
        this.pos = pos;
    }

    private MapleBuffStat(int buffstat, int first, boolean stacked) {
        this.buffstat = buffstat;
        this.pos = first;
        this.stacked = stacked;
    }
    
    private MapleBuffStat(int flag) {
    	this.buffstat = (1 << (flag % 32));
    	this.pos = (byte) Math.floor(flag / 32);
    }
    
    private MapleBuffStat(int flag, boolean stacked) {
    	this.buffstat = (1 << (flag % 32));
    	this.pos = (byte) Math.floor(flag / 32);
    	this.stacked = stacked;
    }

    @Override
    public int getValue() {
        return buffstat;
    }
    
    @Override
    public int getPosition() {
        return (17 - pos);
    }

    public final boolean canStack() {
        return stacked;
    }
}

package client;

import java.io.Serializable;
import handling.Buffstat;

public enum MapleBuffStat implements Serializable, Buffstat {

	IndiePAD(0),
	IndieMAD(1),
	IndiePDD(2),
	IndieMDD(3),
	IndieMHP(4),
	IndieMHPR(5),
	IndieMMP(6),
	IndieMMPR(7),
	IndieACC(8),
	IndieEVA(9),
	IndieJump(10),
    IndieSpeed(11),
    IndieAllStat(12),
    IndieDodgeCriticalTime(13),
    IndieEXP(14),
    IndieBooster(15),
    IndieFixedDamageR(16),
    PyramidStunBuff(17),
    PyramidFrozenBuff(18),
    PyramidFireBuff(19),
    PyramidBonusDamageBuff(20),
    IndieRelaxEXP(21),
    IndieSTR(22),
    IndieDEX(23),
    IndieINT(24),
    IndieLUK(25),
    IndieDamR(26),
    IndieScriptBuff(27),
    IndieMDF(28),
    IndieMaxDamageOver(29),
    IndieAsrR(30),
    IndieTerR(31),
    IndieCr(32),
    IndiePDDR(33),
    IndieCrMax(34),
    IndieBDR(35),
    IndieStatR(36),
    IndieStance(37),
    IndieIgnoreMobpdpR(38),
    IndieEmpty(39),
    IndiePADR(40),
    IndieMADR(41),
    IndieCrMaxR(42),
    IndieEVAR(43),
    IndieMDDR(44),
    IndieDrainHP(45),
    IndiePMdR(46),
    IndieMaxDamageOverR(47),
    IndieForceJump(48),
    IndieForceSpeed(49),
    IndieQrPointTerm(50),

    // missing 5
    
    INDIE_STAT_COUNT(56),
    PAD(57),
    PDD(58),
    MAD(59),
    MDD(60),
    ACC(61),
    EVA(62),
    Craft(63),
    Speed(64),
    Jump(65),
    MagicGuard(66),
    DarkSight(67),
    Booster(68),
    PowerGuard(69),
    MaxHP(70),
    MaxMP(71),
    Invincible(72),
    SoulArrow(73),
    Stun(74),
    Poison(75),
    Seal(76),
    Darkness(77),
    ComboCounter(78),
    WeaponCharge(79),
    HolySymbol(80),
    MesoUp(81),
    ShadowPartner(82),
    PickPocket(83),
    MesoGuard(84),
    Thaw(85),
    Weakness(86),
    Curse(87),
    Slow(88),
    Morph(89),
    Regen(90),
    BasicStatUp(91),
    Stance(92),
    SharpEyes(93),
    ManaReflection(94),
    Attract(95),
    NoBulletConsume(96),
    Infinity(97),
    AdvancedBless(98),
    IllusionStep(99),
    Blind(100),
    Concentration(101),
    BanMap(102),
    MaxLevelBuff(103),
    MesoUpByItem(104),
    Ghost(105),
    Barrier(106),
    ReverseInput(107),
    ItemUpByItem(108),
    RespectPImmune(109),
    RespectMImmune(110),
    DefenseAtt(111),
    DefenseState(112),
    DojangBerserk(113),
    DojangInvincible(114),
    DojangShield(115),
    SoulMasterFinal(116),
    WindBreakerFinal(117),
    ElementalReset(118),
    HideAttack(119),
    EventRate(120),
    ComboAbilityBuff(121),
    ComboDrain(122),
    ComboBarrier(123),
    BodyPressure(124),
    RepeatEffect(125),
    ExpBuffRate(126),
    StopPortion(127),
    StopMotion(128),
    Fear(129),
    HiddenPieceOn(130),
    MagicShield(131),
    MagicResistance(132),
    SoulStone(133),
    Flying(134),
    Frozen(135),
    AssistCharge(136),
    Enrage(137),
    DrawBack(138),
    NotDamaged(139),
    FinalCut(140),
    HowlingAttackDamage(141),
    BeastFormDamageUp(142),
    Dance(143),
    EMHP(144),
    EMMP(145),
    EPAD(146),
    EMAD(147),
    EPDD(148),
    EMDD(149),
    GUARD(150),
    Cyclone(151),
    HowlingCritical(152),
    HowlingMaxMP(153),
    HowlingDefence(154),
    HowlingEvasion(155),
    Conversion(156),
    Revive(157),
    PinkbeanMinibeenMove(158),
    Sneak(159),
    Mechanic(160),
    BeastFormMaxHP(161),
    Dice(162),
    BlessingArmor(163),
    DamR(164),
    TeleportMasteryOn(165),
    CombatOrders(166),
    Beholder(167),
    DispelItemOption(168),
    Inflation(169),
    OnyxDivineProtection(170),
    Web(171),
    Bless(172),
    TimeBomb(173),
    DisOrder(174),
    Thread(175),
    Team(176),
    Explosion(177),
    BuffLimit(178),
    STR(179),
    INT(180),
    DEX(181),
    LUK(182),
    DispelItemOptionByField(183),
    DarkTornado(184),
    PVPDamage(185),
    PvPScoreBonus(186),
    PvPInvincible(187),
    PvPRaceEffect(188),
    WeaknessMdamage(189),    
    Frozen2(190),
    PVPDamageSkill(191),
    AmplifyDamage(192),
    IceKnight(193),
    Shock(194),
    InfinityForce(195),
    IncMaxHP(196),
    IncMaxMP(197),
    HolyMagicShell(198),
    KeyDownTimeIgnore(199),
    ArcaneAim(200),
    MasterMagicOn(201),
    AsrR(202),
    TerR(203),
    DamAbsorbShield(204),
    DevilishPower(205),
    Roulette(206),
    SpiritLink(207),
    AsrRByItem(208),
    Event(209),
    CriticalBuff(210),
    DropRate(211),
    PlusExpRate(212),
    ItemInvincible(213),
    Awake(214),
    ItemCritical(215),
    ItemEvade(216),
    Event2(217),
    VampiricTouch(218),
    DDR(219),
    IncCriticalDamMin(220),
    IncCriticalDamMax(221),
    IncTerR(222),
    IncAsrR(223),
    DeathMark(224),
    UsefulAdvancedBless(225),
    Lapidification(226),
    VenomSnake(227),
    CarnivalAttack(228),
    CarnivalDefence(229),
    CarnivalExp(230),
    SlowAttack(231),
    PyramidEffect(232),
    KillingPoint(233),
    HollowPointBullet(234),
    KeyDownMoving(235),
    IgnoreTargetDEF(236),
    ReviveOnce(237),
    Invisible(238),
    EnrageCr(239),
    EnrageCrDamMin(240),
    Judgement(241),
    DojangLuckyBonus(242),
    PainMark(243),
    Magnet(244),
    MagnetArea(245),
    VampDeath(246),
    BlessingArmorIncPAD(247),
    KeyDownAreaMoving(248),
    Larkness(249),
    StackBuff(250),
    BlessOfDarkness(251),
    AntiMagicShell(252),
    LifeTidal(253),
    HitCriDamR(254),
    SmashStack(255),
    PartyBarrier(256),
    ReshuffleSwitch(257),
    SpecialAction(258),
    VampDeathSummon(259),
    StopForceAtomInfo(260),
    SoulGazeCriDamR(261),
    SoulRageCount(262),
    PowerTransferGauge(263),
    AffinitySlug(264),
    Trinity(265),
    IncMaxDamage(266),
    BossShield(267),
    MobZoneState(268),
    GiveMeHeal(269),
    TouchMe(270),
    Contagion(271),
    ComboUnlimited(272),
    SoulExalt(273),
    IgnorePCounter(274),
    IgnoreAllCounter(275),
    IgnorePImmune(276),
    IgnoreAllImmune(277),
    FinalJudgement(278),
    IceAura(279),
    FireAura(280),
    VengeanceOfAngel(281),
    HeavensDoor(282),
    Preparation(283),
    BullsEye(284),
    IncEffectHPPotion(285),
    IncEffectMPPotion(286),
    BleedingToxin(287),
    IgnoreMobDamR(288),
    Asura(289),
    FlipTheCoin(290),
    UnityOfPower(291),
    Stimulate(292),
    ReturnTeleport(293),
    DropRIncrease(294),
    IgnoreMobpdpR(295),
    BDR(296),
    CapDebuff(297),
    Exceed(298),
    DiabolikRecovery(299),
    FinalAttackProp(300),
    ExceedOverload(301),
    OverloadCount(302),
    BuckShot(303),
    FireBomb(304),
    HalfstatByDebuff(305),
    SurplusSupply(306),
    SetBaseDamage(307),
    EVAR(308),
    NewFlying(309),
    AmaranthGenerator(310),
    OnCapsule(311),
    CygnusElementSkill(312),
    StrikerHyperElectric(313),
    EventPointAbsorb(314),
    EventAssemble(315),
    StormBringer(316),
    ACCR(317),
    DEXR(318),
    Albatross(319),
    Translucence(320),
    PoseType(321),
    LightOfSpirit(322),
    ElementSoul(323),
    GlimmeringTime(324),
    TrueSight(325),
    SoulExplosion(326),
    SoulMP(327),
    FullSoulMP(328),
    SoulSkillDamageUp(329),
    ElementalCharge(330),
    Restoration(331),
    CrossOverChain(332),
    ChargeBuff(333),
    Reincarnation(334),
    KnightsAura(335),
    ChillingStep(336),
    DotBasedBuff(337),
    BlessEnsenble(338),
    ComboCostInc(339),
    ExtremeArchery(340),
    NaviFlying(341),
    QuiverCatridge(342),
    AdvancedQuiver(343),
    UserControlMob(344),
    ImmuneBarrier(345),
    ArmorPiercing(346),
    ZeroAuraStr(347),
    ZeroAuraSpd(348),
    CriticalGrowing(349),
    QuickDraw(350),
    BowMasterConcentration(351),
    TimeFastABuff(352),
    TimeFastBBuff(353),
    GatherDropR(354),
    AimBox2D(355),
    IncMonsterBattleCaptureRate(356),
    CursorSniping(357),
    DebuffTolerance(358),
    DotHealHPPerSecond(359),
    SpiritGuard(360),
    PreReviveOnce(361),
    SetBaseDamageByBuff(362),
    LimitMP(363),
    ReflectDamR(364),
    ComboTempest(365),
    MHPCutR(366),
    MMPCutR(367),
    SelfWeakness(368),
    ElementDarkness(369),
    FlareTrick(370),
    Ember(371),
    Dominion(372),
    SiphonVitality(373),
    DarknessAscension(374),
    BossWaitingLinesBuff(375),
    DamageReduce(376),
    ShadowServant(377),
    ShadowIllusion(378),
    KnockBack(379),
    AddAttackCount(380),
    ComplusionSlant(381),
    JaguarSummoned(382),
    JaguarCount(383),
    SSFShootingAttack(384),
    DevilCry(385),
    ShieldAttack(386),
    BMageAura(387),
    DarkLighting(388),
    AttackCountX(389),
    BMageDeath(390),
    BombTime(391),
    NoDebuff(392),
    BattlePvP_Mike_Shield(393),
    BattlePvP_Mike_Bugle(394),
    XenonAegisSystem(395),
    AngelicBursterSoulSeeker(396),
    HiddenPossession(397),
    NightWalkerBat(398),
    NightLordMark(399),
    WizardIgnite(400),
    FireBarrier(401),
    ChangeFoxMan(402),
    BattlePvP_Helena_Mark(403),
    BattlePvP_Helena_WindSpirit(404),
    BattlePvP_LangE_Protection(405),
    BattlePvP_LeeMalNyun_ScaleUp(406),
    BattlePvP_Revive(407),
    PinkbeanAttackBuff(408),
    PinkbeanRelax(409),
    PinkbeanRollingGrade(410),
    PinkbeanYoYoStack(411),
    RandAreaAttack(412),
    NextAttackEnhance(413),
    AranBeyonderDamAbsorb(414),
    AranCombotempastOption(415),
    NautilusFinalAttack(416),
    ViperTimeLeap(417),
    RoyalGuardState(418),
    RoyalGuardPrepare(419),
    MichaelSoulLink(420),
    MichaelStanceLink(421),
    TriflingWhimOnOff(422),
    AddRangeOnOff(423),
    KinesisPsychicPoint(424),
    KinesisPsychicOver(425),
    KinesisPsychicShield(426),
    KinesisIncMastery(427),
    KinesisPsychicEnergeShield(428),
    BladeStance(429),
    DebuffActiveSkillHPCon(430),
    DebuffIncHP(431),
    BowMasterMortalBlow(432),
    AngelicBursterSoulResonance(433),
    Fever(434),
    IgnisRore(435),
    RpSiksin(436),
    TeleportMasteryRange(437),
    FixCoolTime(438),
    IncMobRateDummy(439),
    AdrenalinBoost(440),
    AranSmashSwing(441),
    AranDrain(442),
    AranBoostEndHunt(443),
    HiddenHyperLinkMaximization(444),
    RWCylinder(445),
    RWCombination(446),
    RWMagnumBlow(447),
    RWBarrier(448),
    RWBarrierHeal(449),
    RWMaximizeCannon(450),
    RWOverHeat(451),
    UsingScouter(452),
    RWMovingEvar(453),
    Stigma(454),

    // Sengoku/Hayato is 455 -> 473/479
    // Beast Tamer 480 -> 491
    
    // TwoStateTemporaryStats 492 -> 499
    EnergyCharged(492),
    Dash_Speed(493),
    Dash_Jump(494),
    RideVehicle(495),
    PartyBooster(496),
    GuidedBullet(497),
    Undead(498),
    RideVehicleExpire(499),
    
    // 500 -> 543 Variety of interesting flags.
    // SoulMasterFinal = DropBuffRate && WindBreakerFinal = BladeClone
    
    // TODO:
    SUMMON(74),
    DRAGONBLOOD(76),
    ACASH_RATE(99),
    
    // 1st
    BEARASSAULT(0x6000, 1),
    PUPPET(0x8000000, 1),
    
    // 2nd
    BUFF_MASTERY(0x200, 2),

    ILLUSION(0x1000000, 2),
    BERSERK_FURY(0x8000000, 2),
    DIVINE_BODY(0x10000000, 2),
    SPARK(0x20000000, 2),
    ARIANT_COSS_IMU2(0x40000000, 2),

    // 3rd
    WIND_WALK(0x4, 3),
    PYRAMID_PQ(0x200, 3),
    POTION_CURSE(0x800, 3),
    SHADOW(0x1000, 3),
    BLINDNESS(0x2000, 3),
    SLOWNESS(0x4000, 3),
    OWL_SPIRIT(0x400000, 3),
    RAINING_MINES(0x8000000, 3),

    // 4th
    SATELLITESAFE_PROC(0x2, 4),
    SATELLITESAFE_ABSORB(0x4, 4),
    TORNADO(0x20, 4),
    DAMAGE_TAKEN_BUFF(0x100, 4),
    DODGE_CHANGE_BUFF(0x200, 4),
    REAPER(0x800, 4),
    INFILTRATE(0x1000, 4),
    AURA(0x1000, 4),
    DAMAGE_RATE(0x100000, 4),
    ONYX_WILL(0x8000000, 4),
    TORNADO_CURSE(0x40000000, 4),

    // 5th
    THREATEN_PVP(0x4, 5),
    ATTACK(0x100, 5),
    PVP_ATTACK(0x400000, 5),
    INVINCIBILITY(0x800000, 5),
    STACK_ALLSTATS(0x40000, 5),
    SNATCH(0x4000000, 5),

    ICE_SKILL(0x20000000, 5),
    
    // 6th
    PVP_FLAG(0x2, 6),

    DARK_METAMORPHOSIS(0x80, 6),

    MDEF_BOOST(0x2000, 6),
    WDEF_BOOST(0x4000, 6),
    VIRTUE_EFFECT(0x10000, 6),
    NO_SLIP(0x100000, 6),
    FAMILIAR_SHADOW(0x200000, 6),
    LEECH_AURA(0x2000000, 6),
    ABSORB_DAMAGE_HP(0x20000000, 6),
    DEFENCE_BOOST_R(0x4000000, 6),
    Dusk_Guard(0x2000, 6),
    
    // 7th
    KILL_COUNT(0x800000, 7),
    
    // 8th
    PHANTOM_MOVE(0x8, 8),
    HYBRID_DEFENSES(0x400, 8),

    LUMINOUS_GAUGE(0x200, 8),

    KAISER_MODE_CHANGE(0x20000, 8),
    CRIT_DAMAGE(0x200000, 8),
    DEFAULT_BUFFSTAT(0x80000000, 8),
    
    // 9th
    KAISER_MAJESTY3(0x2, 9),
    KAISER_MAJESTY4(0x4, 9),
    PARTY_STANCE(0x10, 9),
    STATUS_RESIST_TWO(0x10, 9),
    ELEMENT_RESIST_TWO(0x20, 9),
    ANGEL(0x100, 9),
    BOWMASTERHYPER(0x400, 9),
    DAMAGE_RESIST(0x8000, 9),
    MOON_STANCE2(0x80000000, 9),
    EXCEED_ATTACK(0x4000000, 9),
    EXCEED(0x40000000, 9),
    
    // 10th
    LIGHTNING(0x80, 10),
    STORM_ELEMENTAL(0x80, 10),
    TOUCH_OF_THE_WIND1(0x1000, 10),
    ADD_AVOIDABILITY(0x1000, 10),
    TOUCH_OF_THE_WIND2(0x2000, 10),
    ACCURACY_PERCENT(0x20000, 10),
    WARRIOR_STANCE(0x10000, 10),
    EQUINOX_STANCE(0x80000, 10),
    SOLUNA_EFFECT(0x10000,10),
    HP_RECOVER(0x4000000, 10),
    CROSS_SURGE(0x8000000, 10),
    PARASHOCK_GUARD(0x80000000, 10),
    
    // 11th
    PASSIVE_BLESS(0x4, 11),
    DIVINE_FORCE_AURA(0x1000, 11),
    DIVINE_SPEED_AURA(0x2000, 11),
    
    // 12th
    BATTOUJUTSU_STANCE(0x2, 12),
    HAYATO_STANCE(0x100, 12),
    SHIKIGAMI(0x10, 12),
    HAYATO3(0x20, 12),
    HAYATO4(0x40, 12),
    HAYATO5(0x80, 12),
    FOX_FIRE(0x2000, 12),
    HAKU_REBORN(0x4000, 12),
    HAKU_BLESS(0x8000, 12);
	
    private static final long serialVersionUID = 0L;
    private final int nValue;
    private final int nPos;
    private final boolean isIndie;

    private MapleBuffStat(int nValue, int nPos) {
        this.nValue = nValue;
        this.nPos = nPos;
        this.isIndie = false;
    }
    
    private MapleBuffStat(int flag) {
    	this.nValue = (1 << (31 - (flag % 32)));
    	this.nPos = 17 - (byte) Math.floor(flag / 32);
    	this.isIndie = name().contains("Indie");
    }

    @Override
    public int getValue() {
        return nValue;
    }
    
    @Override
    public int getPosition() {
        return nPos;
    }
    
    public boolean isIndie() {
    	return isIndie;
    }
    
    public boolean isEnDecode4Byte() {
    	switch(this) {
    		case RideVehicle:
    		case CarnivalDefence:
    		case SpiritLink:
    		case DojangLuckyBonus:
    		case SoulGazeCriDamR:
    		case PowerTransferGauge:
    		case ReturnTeleport:
    		case ShadowPartner:
    		case SetBaseDamage:
    		case QuiverCatridge:
    		case ImmuneBarrier:
    		case NaviFlying:
    		case Dance:
    		case SetBaseDamageByBuff:
    		case DotHealHPPerSecond:
    		case MagnetArea:
    			return true;
    		default:
    			return false;
    	}
    }
    
    public boolean isMovementAffectingStat() {
    	switch(this) {
    		case Jump:
    		case Stun:
    		case Weakness:
    		case Slow:
    		case Morph:
    		case Ghost:
    		case BasicStatUp:
    		case Attract:
    		case Dash_Speed:
    		case Dash_Jump:
    		case Flying:
    		case Frozen:
    		case Frozen2:
    		case Lapidification:
    		case IndieSpeed:
    		case IndieJump:
    		case KeyDownMoving:
    		case EnergyCharged:
    		case Mechanic:
    		case Magnet:
    		case MagnetArea:
    		case VampDeath:
    		case VampDeathSummon:
    		case GiveMeHeal:
    		case DarkTornado:
    		case NewFlying:
    		case NaviFlying:
    		case UserControlMob:
    		case Dance:
    		case SelfWeakness:
    		case BattlePvP_Helena_WindSpirit:
    		case BattlePvP_LeeMalNyun_ScaleUp:
    		case TouchMe:
    		case IndieForceSpeed:
    		case IndieForceJump:
    			return true;
			default:
				return false;
    	}
    }
    
    public static MapleBuffStat getCTSFromTSIndex(int index) {
    	switch(index) {
	    	case 0: 
	    		return EnergyCharged;
	    	case 1:
	    		return Dash_Speed;
	    	case 2:
	    		return Dash_Jump;
	    	case 3:
	    		return RideVehicle;
	    	case 4:
	    		return PartyBooster;
	    	case 5:
	    		return GuidedBullet;
	    	case 6:
	    		return Undead;
	    	case 7:
	    		return RideVehicleExpire;
    	}
		return null;
    }
}

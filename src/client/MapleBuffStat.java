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
    
    INDIE_STAT_COUNT(57),
    PAD(58),
    PDD(59),
    MAD(60),
    MDD(61),
    ACC(62),
    EVA(63),
    Craft(64),
    Speed(65),
    Jump(66),
    MagicGuard(67),
    DarkSight(68),
    Booster(69),
    PowerGuard(70),
    MaxHP(71),
    MaxMP(72),
    Invincible(73),
    SoulArrow(74),
    Stun(75),
    Poison(76),
    Seal(77),
    Darkness(78),
    ComboCounter(79),
    WeaponCharge(80),
    HolySymbol(81),
    MesoUp(82),
    ShadowPartner(83),
    PickPocket(84),
    MesoGuard(85),
    Thaw(86),
    Weakness(87),
    Curse(88),
    Slow(89),
    Morph(90),
    Regen(91),
    BasicStatUp(92),
    Stance(93),
    SharpEyes(94),
    ManaReflection(95),
    Attract(96),
    NoBulletConsume(97),
    Infinity(98),
    AdvancedBless(99),
    IllusionStep(100),
    Blind(101),
    Concentration(102),
    BanMap(103),
    MaxLevelBuff(104),
    MesoUpByItem(105),
    Ghost(106),
    Barrier(107),
    ReverseInput(108),
    ItemUpByItem(109),
    RespectPImmune(110),
    RespectMImmune(111),
    DefenseAtt(112),
    DefenseState(113),
    DojangBerserk(114),
    DojangInvincible(115),
    DojangShield(116),
    SoulMasterFinal(117),
    WindBreakerFinal(118),
    ElementalReset(119),
    HideAttack(120),
    EventRate(121),
    ComboAbilityBuff(122),
    ComboDrain(123),
    ComboBarrier(124),
    BodyPressure(125),
    RepeatEffect(126),
    ExpBuffRate(127),
    StopPortion(128),
    StopMotion(129),
    Fear(130),
    HiddenPieceOn(131),
    MagicShield(132),
    MagicResistance(133),
    SoulStone(134),
    Flying(135),
    Frozen(136),
    AssistCharge(137),
    Enrage(138),
    DrawBack(139),
    NotDamaged(140),
    FinalCut(141),
    HowlingAttackDamage(142),
    BeastFormDamageUp(143),
    Dance(144),
    EMHP(145),
    EMMP(146),
    EPAD(147),
    EMAD(148),
    EPDD(149),
    EMDD(150),
    GUARD(151),
    Cyclone(152),
    HowlingCritical(153),
    HowlingMaxMP(154),
    HowlingDefence(155),
    HowlingEvasion(156),
    Conversion(157),
    Revive(158),
    PinkbeanMinibeenMove(159),
    Sneak(160),
    Mechanic(161),
    BeastFormMaxHP(162),
    Dice(163),
    BlessingArmor(164),
    DamR(165),
    TeleportMasteryOn(166),
    CombatOrders(167),
    Beholder(168),
    DispelItemOption(169),
    Inflation(170),
    OnyxDivineProtection(171),
    Web(172),
    Bless(173),
    TimeBomb(174),
    DisOrder(175),
    Thread(176),
    Team(177),
    Explosion(178),
    BuffLimit(179),
    STR(180),
    INT(181),
    DEX(182),
    LUK(183),
    DispelItemOptionByField(184),
    DarkTornado(185),
    PVPDamage(186),
    PvPScoreBonus(187),
    PvPInvincible(188),
    PvPRaceEffect(189),
    WeaknessMdamage(190),    
    Frozen2(191),
    PVPDamageSkill(192),
    AmplifyDamage(193),
    IceKnight(194),
    Shock(195),
    InfinityForce(196),
    IncMaxHP(197),
    IncMaxMP(198),
    HolyMagicShell(199),
    KeyDownTimeIgnore(200),
    ArcaneAim(201),
    MasterMagicOn(202),
    AsrR(203),
    TerR(204),
    DamAbsorbShield(205),
    DevilishPower(206),
    Roulette(207),
    SpiritLink(208),
    AsrRByItem(209),
    Event(210),
    CriticalBuff(211),
    DropRate(212),
    PlusExpRate(213),
    ItemInvincible(214),
    Awake(215),
    ItemCritical(216),
    ItemEvade(217),
    Event2(218),
    VampiricTouch(219),
    DDR(220),
    IncCriticalDamMin(221),
    IncCriticalDamMax(222),
    IncTerR(223),
    IncAsrR(224),
    DeathMark(225),
    UsefulAdvancedBless(226),
    Lapidification(227),
    VenomSnake(228),
    CarnivalAttack(229),
    CarnivalDefence(230),
    CarnivalExp(231),
    SlowAttack(232),
    PyramidEffect(233),
    KillingPoint(234),
    HollowPointBullet(235),
    KeyDownMoving(236),
    IgnoreTargetDEF(237),
    ReviveOnce(238),
    Invisible(239),
    EnrageCr(240),
    EnrageCrDamMin(241),
    Judgement(242),
    DojangLuckyBonus(243),
    PainMark(244),
    Magnet(245),
    MagnetArea(246),
    VampDeath(247),
    BlessingArmorIncPAD(248),
    KeyDownAreaMoving(249),
    Larkness(250),
    StackBuff(251),
    BlessOfDarkness(252),
    AntiMagicShell(253),
    LifeTidal(254),
    HitCriDamR(255),
    SmashStack(256),
    PartyBarrier(257),
    ReshuffleSwitch(258),
    SpecialAction(259),
    VampDeathSummon(260),
    StopForceAtomInfo(261),
    SoulGazeCriDamR(262),
    SoulRageCount(263),
    PowerTransferGauge(264),
    AffinitySlug(265),
    Trinity(266),
    IncMaxDamage(267),
    BossShield(268),
    MobZoneState(269),
    GiveMeHeal(270),
    TouchMe(271),
    Contagion(272),
    ComboUnlimited(273),
    SoulExalt(274),
    IgnorePCounter(275),
    IgnoreAllCounter(276),
    IgnorePImmune(277),
    IgnoreAllImmune(278),
    FinalJudgement(279),
    
    // +2 (177.1)
    
    IceAura(281),
    FireAura(282),
    VengeanceOfAngel(283),
    HeavensDoor(284),
    Preparation(285),
    BullsEye(286),
    IncEffectHPPotion(287),
    IncEffectMPPotion(288),
    BleedingToxin(289),
    IgnoreMobDamR(290),
    Asura(291),
    FlipTheCoin(292),
    UnityOfPower(293),
    Stimulate(294),
    ReturnTeleport(295),
    DropRIncrease(296),
    IgnoreMobpdpR(297),
    BDR(298),
    CapDebuff(299),
    Exceed(300),
    DiabolikRecovery(301),
    FinalAttackProp(302),
    ExceedOverload(303),
    OverloadCount(304),
    BuckShot(305),
    FireBomb(306),
    HalfstatByDebuff(307),
    SurplusSupply(308),
    SetBaseDamage(309),
    EVAR(310),
    NewFlying(311),
    AmaranthGenerator(312),
    OnCapsule(313),
    CygnusElementSkill(314),
    StrikerHyperElectric(315),
    EventPointAbsorb(316),
    EventAssemble(317),
    StormBringer(318),
    ACCR(319),
    DEXR(320),
    Albatross(321),
    Translucence(322),
    PoseType(323),
    LightOfSpirit(324),
    ElementSoul(325),
    GlimmeringTime(326),
    TrueSight(327),
    SoulExplosion(328),
    SoulMP(329),
    FullSoulMP(330),
    SoulSkillDamageUp(331),
    ElementalCharge(332),
    Restoration(333),
    CrossOverChain(334),
    ChargeBuff(335),
    Reincarnation(336),
    KnightsAura(337),
    ChillingStep(338),
    DotBasedBuff(339),
    BlessEnsenble(340),
    ComboCostInc(341),
    ExtremeArchery(342),
    NaviFlying(343),
    QuiverCatridge(344),
    AdvancedQuiver(345),
    UserControlMob(346),
    ImmuneBarrier(347),
    ArmorPiercing(348),
    ZeroAuraStr(349),
    ZeroAuraSpd(350),
    CriticalGrowing(351),
    QuickDraw(352),
    BowMasterConcentration(353),
    TimeFastABuff(354),
    TimeFastBBuff(355),
    GatherDropR(356),
    AimBox2D(357),
    IncMonsterBattleCaptureRate(358),
    CursorSniping(359),
    DebuffTolerance(360),
    DotHealHPPerSecond(361),
    SpiritGuard(362),
    PreReviveOnce(363),
    SetBaseDamageByBuff(364),
    LimitMP(365),
    ReflectDamR(366),
    ComboTempest(367),
    MHPCutR(368),
    MMPCutR(369),
    SelfWeakness(370),
    ElementDarkness(371),
    FlareTrick(372),
    Ember(373),
    Dominion(374),
    SiphonVitality(375),
    DarknessAscension(376),
    BossWaitingLinesBuff(377),
    DamageReduce(378),
    ShadowServant(379),
    ShadowIllusion(380),
    KnockBack(381),
    AddAttackCount(382),
    ComplusionSlant(383),
    JaguarSummoned(384),
    JaguarCount(385),
    SSFShootingAttack(386),
    DevilCry(387),
    ShieldAttack(388),
    BMageAura(389),
    DarkLighting(390),
    AttackCountX(391),
    BMageDeath(392),
    BombTime(393),
    NoDebuff(394),
    BattlePvP_Mike_Shield(395),
    BattlePvP_Mike_Bugle(396),
    XenonAegisSystem(397),
    AngelicBursterSoulSeeker(398),
    HiddenPossession(399),
    NightWalkerBat(400),
    NightLordMark(401),
    WizardIgnite(402),
    FireBarrier(403),
    ChangeFoxMan(404),
    BattlePvP_Helena_Mark(405),
    BattlePvP_Helena_WindSpirit(406),
    BattlePvP_LangE_Protection(407),
    BattlePvP_LeeMalNyun_ScaleUp(408),
    BattlePvP_Revive(409),
    PinkbeanAttackBuff(410),
    PinkbeanRelax(411),
    PinkbeanRollingGrade(412),
    PinkbeanYoYoStack(413),
    RandAreaAttack(414),
    NextAttackEnhance(415),
    AranBeyonderDamAbsorb(416),
    AranCombotempastOption(417),
    NautilusFinalAttack(418),
    ViperTimeLeap(419),
    RoyalGuardState(420),
    RoyalGuardPrepare(421),
    MichaelSoulLink(422),
    MichaelStanceLink(423),
    TriflingWhimOnOff(424),
    AddRangeOnOff(425),
    KinesisPsychicPoint(426),
    KinesisPsychicOver(427),
    KinesisPsychicShield(428),
    KinesisIncMastery(429),
    KinesisPsychicEnergeShield(430),
    BladeStance(431),
    DebuffActiveSkillHPCon(432),
    DebuffIncHP(433),
    BowMasterMortalBlow(434),
    AngelicBursterSoulResonance(435),
    Fever(436),
    IgnisRore(437),
    RpSiksin(438),
    TeleportMasteryRange(439),
    FixCoolTime(440),
    IncMobRateDummy(441),
    AdrenalinBoost(442),
    AranSmashSwing(443),
    AranDrain(444),
    AranBoostEndHunt(445),
    HiddenHyperLinkMaximization(446),
    RWCylinder(447),
    RWCombination(448),
    RWMagnumBlow(449),
    RWBarrier(450),
    RWBarrierHeal(451),
    RWMaximizeCannon(452),
    RWOverHeat(453),
    UsingScouter(454),
    RWMovingEvar(455),
    Stigma(456),

    // Sengoku/Hayato
    // Beast Tamer
    
    // TwoStateTemporaryStats 501 -> 508
    EnergyCharged(501),
    Dash_Speed(502),
    Dash_Jump(503),
    RideVehicle(504),
    PartyBooster(505),
    GuidedBullet(506),
    Undead(507),
    RideVehicleExpire(508),
    
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

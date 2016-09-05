package handling;

public enum SendPacketOpcode {

    // General
    PING(0x12),
    
    // CLogin::OnPacket (this has been updated.)
    LOGIN_STATUS(0x00),
    SERVERLIST(0x01),
    ENABLE_RECOMMENDED(0x02), 
    SEND_RECOMMENDED(0x03),
    SET_CLIENT_KEY(0x04),
    SELECT_WORLD(0x05),
    CHARLIST(0x06),
    SERVER_IP(0x07), // character select result
    LOGIN_SECOND(0x08),
    CHAR_NAME_RESPONSE(0x0A),
    ADD_NEW_CHAR_ENTRY(0x0B),
    DELETE_CHAR_RESPONSE(0x0C),
    CHANGE_CHANNEL(0x11),
    TOGGLE_CASHSHOP(0x14),
    AUTH_RESPONSE(0x17),
    PART_TIME(0x1D),
    PIC_RESPONSE(0x19),
    SERVERSTATUS(0x26),
    AUTHSERVER(0x2F),
    REGISTER_PIC_RESPONSE(0x1A), // needs updating
    SPECIAL_CREATION(0x20), // needs updating
    SECONDPW_ERROR(0x25), // needs updating
    
    // CWvsContext::OnPacket (this has been updated.)
    INVENTORY_OPERATION(0x47),
    INVENTORY_GROW(0x48),
    UPDATE_STATS(0x49), // OnStatUpdate
    GIVE_BUFF(0x4A),
    CANCEL_BUFF(0x4B),
    TEMP_STATS(0x4C),
    TEMP_STATS_RESET(0x4D),
    UPDATE_SKILLS(0x4E), // OnChangeSkillRecordResult
    UPDATE_STOLEN_SKILLS(0x4F),
    TARGET_SKILL(0x56),
    FAME_RESPONSE(0x58),
    SHOW_STATUS_INFO(0x59),
    SHOW_NOTES(0x5A),
    TROCK_LOCATIONS(0x5B),
    LIE_DETECTOR(0x5C),
    REPORT_RESPONSE(0x5F),
    REPORT_TIME(0x60),
    REPORT_STATUS(0x61),
    UPDATE_MOUNT(0x63),
    SHOW_QUEST_COMPLETION(0x64),
    SEND_TITLE_BOX(0x65),
    USE_SKILL_BOOK(0x66),
    SP_RESET(0x67),
    AP_RESET(0x68),
    EXPAND_CHARACTER_SLOTS(0x6B),
    FINISH_GATHER(0x6D),
    FINISH_SORT(0x6E),
    EXP_POTION(0x43), // needs updating
    CHAR_INFO(0x71),
    PARTY_OPERATION(0x72),
    MEMBER_SEARCH(0x73),
    PARTY_SEARCH(0x5A), // needs updating
    BOOK_INFO(0x5B), // needs updating
    CODEX_INFO_RESPONSE(0x5C), // needs updating
    EXPEDITION_OPERATION(0x7A),
    BUDDYLIST(0x7B),
    GUILD_OPERATION(0x7F),
    ALLIANCE_OPERATION(0x80),
    SPAWN_PORTAL(0x81),
    SERVERMESSAGE(0x82),
    PIGMI_REWARD(0x84),
    OWL_OF_MINERVA(0x86),
    OWL_RESULT(0x87), 
    ENGAGE_REQUEST(0x8A),
    ENGAGE_RESULT(0x8B),
    WEDDING_GIFT(0x8C),
    WEDDING_MAP_TRANSFER(0x8D),
    USE_CASH_PET_FOOD(0x8E),
    YELLOW_CHAT(0x93),
    SHOP_DISCOUNT(0x94),
    CATCH_MOB(0x95),
    PLAYER_NPC(0x97),
    DISABLE_NPC(0x99),
    GET_CARD(0x9A),
    CARD_SET(0x9B),
    BOOK_STATS(0x81), // needs updating
    FAMILIAR_INFO(0x84), // needs updating
    WEB_BOARD_UPDATE(0xA0),
    SESSION_VALUE(0xA1),
    PARTY_VALUE(0xA2),
    MAP_VALUE(0xA3),
    EXP_BONUS(0xA5),
    SEND_PEDIGREE(0xA6),
    OPEN_FAMILY(0xA7),
    FAMILY_MESSAGE(0xA8),
    FAMILY_INVITE(0xA9),
    FAMILY_JUNIOR(0xAA), 
    SENIOR_MESSAGE(0xAB),
    FAMILY(0xAC),
    REP_INCREASE(0xAD),
    FAMILY_LOGGEDIN(0xAE),
    FAMILY_BUFF(0xAF),
    FAMILY_USE_REQUEST(0xB0),
    LEVEL_UPDATE(0xB1),
    MARRIAGE_UPDATE(0xB2),
    JOB_UPDATE(0xB3),
    SLOT_UPDATE(0xB4),
    FOLLOW_REQUEST(0xB5),
    TOP_MSG(0xB7),
    MID_MSG(0xB9),
    CLEAR_MID_MSG(0xBA),
    SPECIAL_MSG(0xBB),
    MAPLE_ADMIN_MSG(0xB5), // needs updating
    UPDATE_JAGUAR(0xC0),
    YOUR_INFORMATION(0xB9), // needs updating
    FIND_FRIEND(0xBA), // needs updating
    VISITOR(0xBB), // needs updating
    ULTIMATE_EXPLORER(0xC4),
	SPECIAL_STAT(0xC6),
    UPDATE_IMP_TIME(0xC7),
    ITEM_POT(0xC8),
    MULUNG_DOJO_RANKING(0xD1),
    REPLACE_SKILLS(0xD5),
    INNER_ABILITY_MSG(0xD8), // needs updating
    ENABLE_INNER_ABILITY(0xD9),
    DISABLE_INNER_ABILITY(0xDA),
    UPDATE_HONOUR(0xDB),
    AZWAN_KILLED(0xDE),
    SILENT_CRUSADE_MSG(0xE2),
    SILENT_CRUSADE_SHOP(0xE3),
    UNLOCK_CHARGE_SKILL(0xFF),
    LOCK_CHARGE_SKILL(0x100),
    EVOLVING_ACTION(0x103),
    CANDY_RANKING(0x108),
    MESSENGER_OPEN(0x10D), // needs updating
    AVATAR_MEGA(0x112),
    AVATAR_MEGA_REMOVE(0x113),
    EVENT_CROWN(0x118),
    UPDATE_GENDER(0x17F),
    BBS_OPERATION(0x180),
    CARD_DROPS(0x187),
    GM_POLICE(0x18E),
    GM_STORY_BOARD(0x196),
    PINKBEAN_CHOCO(0x198),
    PAM_SONG(0x199),
    DISALLOW_DELIVERY_QUEST(0x19B),
    MAGIC_WHEEL(0x1A2),
    REWARD(0x1A3),
    SKILL_MACRO(0x1AB),
    
    // CStage::OnPacket (this has been updated.)
    WARP_TO_MAP(0x1AC), // 0x126
    FARM_OPEN(0x1AD), // 0x127
    CS_OPEN(0x1AF), // 0x129
    
    // CMapLoadable::OnPacket
    REMOVE_BG_LAYER(0x12A), // 111
    SET_MAP_OBJECT_VISIBLE(0x12B), // 112
    CHANGE_BACKGROUND(0x36), // 112, 0x12B
    RESET_SCREEN(0x12C), // 113
    MAP_BLOCKED(0x12D), // 114
    
    // CField::OnPacket (this has been updated.)
    SERVER_BLOCKED(0x1B0),
    PARTY_BLOCKED(0x1B1),
    SHOW_EQUIP_EFFECT(0x1B3),
    MULTICHAT(0x1B4),
    WHISPER(0x1B6),
    SPOUSE_CHAT(0x1B9),
    BOSS_ENV(0x1B8),
    MOVE_ENV(0x1BA),
    UPDATE_ENV(0x1BB),
    MAP_EFFECT(0x1C0),
    CASH_SONG(0x1C1),
    GM_EFFECT(0x1C2),
    OX_QUIZ(0x1C3),
    GMEVENT_INSTRUCTIONS(0x1C4),
    CLOCK(0x1C5),
    BOAT_MOVE(0x1C6),
    BOAT_STATE(0x1C7),
    STOP_CLOCK(0x1CB),
    ARIANT_SCOREBOARD(0x14A), // needs updating
    PYRAMID_UPDATE(0x14E), // needs updating
    PYRAMID_RESULT(0x14F), // needs updating
    QUICK_SLOT(0x1D0), // 0x151   
    MOVE_PLATFORM(0x152), // needs updating
    PYRAMID_KILL_COUNT(0x153), // needs updating
    PVP_INFO(0x1DB), // 0x156
    DIRECTION_STATUS(0x1DC),
    GAIN_FORCE(0x1DD),
    ACHIEVEMENT_RATIO(0x1DE),
    QUICK_MOVE(0x1DF),
    INTRUSION(0x1EA),
    
    // CUserPool::OnPacket (this has been updated.)
    SPAWN_PLAYER(0x204),
    REMOVE_PLAYER_FROM_MAP(0x205),
    
    // CUserPool::OnUserCommonPacket (this has been updated.)
    CHATTEXT(0x206),
    CHALKBOARD(0x207),
    UPDATE_CHAR_BOX(0x208),
    SHOW_CONSUME_EFFECT(0x209),
    SHOW_SCROLL_EFFECT(0x20A),
    SHOW_MAGNIFYING_EFFECT(0x20E),
    SHOW_POTENTIAL_RESET(0x20F),
    SHOW_FIREWORKS_EFFECT(0x214),
    SHOW_NEBULITE_EFFECT(0x171), // needs updating
    SHOW_FUSION_EFFECT(0x172), // needs updating
    PVP_ATTACK(0x217),
    PVP_MIST(0x218),
    PVP_COOL(0x219),
    TESLA_TRIANGLE(0x21B),
    FOLLOW_EFFECT(0x21C),
    SHOW_PQ_REWARD(0x21D),
    CRAFT_EFFECT(0x21E),
    CRAFT_COMPLETE(0x21F),
    HARVESTED(0x221),
    PLAYER_DAMAGED(0x223),
    NETT_PYRAMID(0x224),
    PAMS_SONG(0x168), // needs updating
    
    // CUser::OnPetPacket (this has been updated.)
    SPAWN_PET(0x243),
    SPAWN_PET_2(0x192), //  needs updating
    MOVE_PET(0x244),
    PET_CHAT(0x245),
    PET_NAMECHANGE(0x247),
    PET_EXCEPTION_LIST(0x248),
    PET_COLOR(0x249),
    PET_SIZE(0x24A),
    PET_COMMAND(0x24D),
    
    // CUser::OnDragonPacket (this has been updated.)
    DRAGON_SPAWN(0x24E),    
    DRAGON_MOVE(0x24F),
    DRAGON_REMOVE(0x250),
    
    // CUser::OnAndroidPacket (this has been updated.)
    ANDROID_SPAWN(0x252),
    ANDROID_MOVE(0x253),
    ANDROID_EMOTION(0x254),
    ANDROID_UPDATE(0x255),
    ANDROID_DEACTIVATED(0x256),
    
    // CUser::OnFoxManPacket (this has been updated.)
    HAKU_CHANGE_1(0x1A2), // needs updating
    HAKU_CHANGE_0(0x1A5), // needs updating
    SPAWN_HAKU(0x257),
    HAKU_MOVE(0x258),
    HAKU_CHANGE(0x25A),
    
    CHATTEXT_1(0x265),
    
    // CUser::OnFamiliarPacket (this has been updated.)
    SPAWN_FAMILIAR(0x26E),
    MOVE_FAMILIAR(0x26F),
    TOUCH_FAMILIAR(0x270),
    ATTACK_FAMILIAR(0x271),
    RENAME_FAMILIAR(0x272),
    SPAWN_FAMILIAR_2(0x273),
    UPDATE_FAMILIAR(0x274),
    
    // CUserPool::OnUserRemotePacket (this has been updated.)
    MOVE_PLAYER(0x276),
    CLOSE_RANGE_ATTACK(0x277),
    RANGED_ATTACK(0x278),
    MAGIC_ATTACK(0x279),
    ENERGY_ATTACK(0x27A),
    SKILL_EFFECT(0x27B),
    MOVE_ATTACK(0x27C),
    CANCEL_SKILL_EFFECT(0x27D),
    DAMAGE_PLAYER(0x27E),
    FACIAL_EXPRESSION(0x27F),
    SHOW_EFFECT(0x281),
    SHOW_TITLE(0x283),
    ANGELIC_CHANGE(0x284),
    SHOW_CHAIR(0x287),
    UPDATE_CHAR_LOOK(0x288),
    SHOW_FOREIGN_EFFECT(0x289),
    GIVE_FOREIGN_BUFF(0x28A),
    CANCEL_FOREIGN_BUFF(0x28B),
    UPDATE_PARTYMEMBER_HP(0x28C),
    LOAD_GUILD_NAME(0x28D),
    LOAD_GUILD_ICON(0x28E),
    LOAD_TEAM(0x28F),
    SHOW_HARVEST(0x290),
    PVP_HP(0x1D7), // needs updating
    CANCEL_CHAIR(0x22B), // needs updating
    
    INNER_ABILITY_RESET_MSG(0x2CC),
    
    // CUserPool::OnUserLocalPacket (this has been updated.)
    DIRECTION_FACIAL_EXPRESSION(0x2AA),
    MOVE_SCREEN(0x2AB),
    SHOW_SPECIAL_EFFECT(0x2AC),
    CURRENT_MAP_WARP(0x2AD),
    MESOBAG_SUCCESS(0x2AF),
    MESOBAG_FAILURE(0x2B0),
    UPDATE_QUEST_INFO(0x2B1),
    HP_DECREASE(0x2B2),
    PLAYER_HINT(0x2B4),
    PLAY_EVENT_SOUND(0x2B5),
    PLAY_MINIGAME_SOUND(0x2B6),
    MAKER_SKILL(0x2B7),
    OPEN_UI(0x2BA),
    OPEN_UI_OPTION(0x2BC),
    INTRO_LOCK(0x2BE),
    INTRO_ENABLE_UI(0x2BF),
    INTRO_DISABLE_UI(0x2C0),
    SUMMON_HINT(0x2C1),
    SUMMON_HINT_MSG(0x2C2),
    ARAN_COMBO(0x2C8),
    ARAN_COMBO_RECHARGE(0x2C9),
    RADIO_SCHEDULE(0x2CA),
    OPEN_SKILL_GUIDE(0x2CB),
    GAME_MSG(0x2CC),
    GAME_MESSAGE(0x2CD),
    BUFF_ZONE_EFFECT(0x2CF),
    TIME_BOMB_ATTACK(0x2D0),
    FOLLOW_MOVE(0x2D2),
    FOLLOW_MSG(0x2D3),
    HARVEST_MESSAGE(0x2D6),
    OPEN_BAG(0x2D8),
    DRAGON_BLINK(0x2D9),
    PVP_ICEGAGE(0x2DA),
    DIRECTION_INFO(0x2DB),
    REISSUE_MEDAL(0x2DC),
    PLAY_MOVIE(0x2DF),
    PHANTOM_CARD(0x2E1),
    LUMINOUS_COMBO(0x2E4),
	MOVE_SCREEN_X(0x1A9),
	MOVE_SCREEN_DOWN(0x1AA),
	SEALED_BOX(0x222), // needs updating.
    COOLDOWN(0x333),
    R_MESOBAG_SUCCESS(0x34C),
    R_MESOBAG_FAILURE(0x34D),
    MAP_FADE(0x34E),
    MAP_FADE_FORCE(0x34F),
    REGISTER_FAMILIAR(0x35B),
    FAMILIAR_MESSAGE(0x35C),
    CREATE_ULTIMATE(0x35D), // needs testing
    
    // CSummonedPool::OnPacket (this has been updated.)
    SPAWN_SUMMON(0x379),
    REMOVE_SUMMON(0x37A),
    MOVE_SUMMON(0x37B),
    SUMMON_ATTACK(0x37C),
    PVP_SUMMON(0x37D),
    SUMMON_SKILL(0x37E),
    SUMMON_SKILL_2(0x37F),
    DAMAGE_SUMMON(0x380), // needs testing
    
    // CMobPool::OnPacket (this has been updated.)
    SPAWN_MONSTER(0x389),
    KILL_MONSTER(0x38A),
    SPAWN_MONSTER_CONTROL(0x38B),
    
    // CMobPool::OnMobPacket
    MOVE_MONSTER(0x38F),
    MOVE_MONSTER_RESPONSE(0x390),
    APPLY_MONSTER_STATUS(0x392),
    CANCEL_MONSTER_STATUS(0x393),
    DAMAGE_MONSTER(0x396),
    SKILL_EFFECT_MOB(0x397),
    TELE_MONSTER(0x3A8), // needs updating.
    MONSTER_SKILL(0x3A1), // needs updating.
    SHOW_MONSTER_HP(0x39B),
    SHOW_MAGNET(0x39C),
    ITEM_EFFECT_MOB(0x39D),
    CATCH_MONSTER(0x39E),
    MONSTER_PROPERTIES(0x3A2),
    REMOVE_TALK_MONSTER(0x3A4),
    TALK_MONSTER(0x1BB), // needs updating
    CYGNUS_ATTACK(0x3A9), // ?
    MONSTER_RESIST(0x3AC), 
    MOB_TO_MOB_DAMAGE(0x3CF),
    AZWAN_MOB_TO_MOB_DAMAGE(0x1C9), // needs updating
    AZWAN_SPAWN_MONSTER(0x999), // needs updating
    AZWAN_KILL_MONSTER(0x999), // needs updating
    AZWAN_SPAWN_MONSTER_CONTROL(0x999), // needs updating
    
    // CNpcPool::OnPacket (this has been updated.)
    SPAWN_NPC(0x3D5),
    REMOVE_NPC(0x3D6),
    SPAWN_NPC_REQUEST_CONTROLLER(0x3D8),
    NPC_ACTION(0x3D9),
    NPC_TOGGLE_VISIBLE(0x3DA),
    INITIAL_QUIZ(0x3DC),
    NPC_UPDATE_LIMITED_INFO(0x3DD),
    NPC_SET_SPECIAL_ACTION(0x3DF),
    NPC_SCRIPTABLE(0x3E0), 
    RED_LEAF_HIGH(0x3E1), 
    
    // CEmployeePool::OnPacket (this has been updated.)
    SPAWN_HIRED_MERCHANT(0x3EB),
    DESTROY_HIRED_MERCHANT(0x3EC),
    UPDATE_HIRED_MERCHANT(0x3ED),
    
    // CDropPool::OnPacket (this has been updated.)
    DROP_ITEM_FROM_MAPOBJECT(0x3EE),
    REMOVE_ITEM_FROM_MAP(0x3F0),
    
    // CMessageBoxPool::OnPacket (this has been updated.)
    SPAWN_KITE_ERROR(0x3F1),
    SPAWN_KITE(0x3F2),
    DESTROY_KITE(0x3F3),
    
    // CAffectedAreaPool::OnPacket (this has been updated.)
    SPAWN_MIST(0x3F5),
    REMOVE_MIST(0x3F6),
    
    // CTownPortalPool::OnPacket (this has been updated.)
    SPAWN_DOOR(0x3F7),
    REMOVE_DOOR(0x3F8),
    
    // COpenGatePool::OnPacket (this has been updated.)
    MECH_DOOR_SPAWN(0x3FC),
    MECH_DOOR_REMOVE(0x3FD),
    
    // CReactorPool::OnPacket (this has been updated.)
    REACTOR_HIT(0x3FF),
    REACTOR_MOVE(0x400),
    REACTOR_SPAWN(0x401),
    REACTOR_DESTROY(0x403),
    
    // CFishingZonePool::OnPacket (this has been updated.)
    FISHING_INFO(0x406),
    FISHING_REWARD(0x407),
    FISHING_ZONE_INFO(0x408),
    
    // CExtractor::OnPacket (this has been updated.)
    SPAWN_EXTRACTOR(0x409),
    REMOVE_EXTRACTOR(0x40A),
    
    // Snowball::OnPacket (this has been updated.)
    ROLL_SNOWBALL(0x40B),
    HIT_SNOWBALL(0x40C),
    SNOWBALL_MESSAGE(0x40D),
    LEFT_KNOCK_BACK(0x40E),
    
    // Coconut::OnPacket (this has been updated.)
    HIT_COCONUT(0x40F),
    COCONUT_SCORE(0x410),
    MOVE_HEALER(0x411),
    PULLEY_STATE(0x412),
    
    // CField_MonsterCarnival::OnPacket (this has been updated.)
    MONSTER_CARNIVAL_START(0x413),
    MONSTER_CARNIVAL_OBTAINED_CP(0x414),
    MONSTER_CARNIVAL_STATS(0x415),
    MONSTER_CARNIVAL_SUMMON(0x2CD),
    MONSTER_CARNIVAL_MESSAGE(0x418),
    MONSTER_CARNIVAL_DIED(0x419),
    MONSTER_CARNIVAL_LEAVE(0x41A),
    MONSTER_CARNIVAL_RESULT(0x41B),
    MONSTER_CARNIVAL_RANKING(0x41C),
    
    
    ARIANT_SCORE_UPDATE(0x300),
    SHEEP_RANCH_INFO(0x301),
    SHEEP_RANCH_CLOTHES(0x999), // 0x302
    WITCH_TOWER(0x999), // 0x303
    EXPEDITION_CHALLENGE(0x999), // 0x304
    ZAKUM_SHRINE(0x305),
    CHAOS_ZAKUM_SHRINE(0x306),
    PVP_TYPE(0x307),
    PVP_TRANSFORM(0x308),
    PVP_DETAILS(0x309),
    PVP_ENABLED(0x30A),
    PVP_SCORE(0x30B),
    PVP_RESULT(0x30C),
    PVP_TEAM(0x30D),
    PVP_SCOREBOARD(0x30E),
    PVP_POINTS(0x310),
    PVP_KILLED(0x311),
    PVP_MODE(0x312),
    PVP_ICEKNIGHT(0x313), // 
    HORNTAIL_SHRINE(0x2E1),
    CAPTURE_FLAGS(0x2E2),
    CAPTURE_POSITION(0x2E3),
    CAPTURE_RESET(0x2E4),
    PINK_ZAKUM_SHRINE(0x2E5),
    
    // CScriptMan::OnPacket (this has been updated.)
    NPC_TALK(0x4FE),
    
    // CShopDlg::OnPacket (this has been updated.)
    OPEN_NPC_SHOP(0x4FF),
    CONFIRM_SHOP_TRANSACTION(0x500),
    
    // CStoreBankDlg::OnPacket
    OPEN_STORAGE(0x344), // 2F1
    MERCH_ITEM_MSG(0x345), // 2F2
    MERCH_ITEM_STORE(0x346), // 2F3
    RPS_GAME(0x347), // 2F4
    MESSENGER(0x348), // 2F5
    PLAYER_INTERACTION(0x349), // 2F6
    VICIOUS_HAMMER(0x2F4),
    LOGOUT_GIFT(0x2FB),
    TOURNAMENT(0x236),
    TOURNAMENT_MATCH_TABLE(0x237),
    TOURNAMENT_SET_PRIZE(0x238),
    TOURNAMENT_UEW(0x239),
    TOURNAMENT_CHARACTERS(0x23A),
    WEDDING_PROGRESS(0x236),
    WEDDING_CEREMONY_END(0x237),
    PACKAGE_OPERATION(0x353), // v143
    
    // CCashShop::OnPacket (this has been updated.)
    CS_UPDATE(0x534),
    CS_OPERATION(0x535),
    PURCHASE_EXP_CHANGED(0x536),
    CS_MESO_UPDATE(0x538),
    GACHAPON_STAMPS(0x543),
    FREE_CASH_ITEM(0x544),
    CS_SURPRISE(0x545),
    XMAS_SURPRISE(0x546),
    CASH_SHOP(0x54C),
    CASH_SHOP_UPDATE(0x54D),
    ONE_A_DAY(0x545), // needs updating (lol yolo)
    
    // CFuncKeyMappedMan::OnPacket (this has been updated.)
    KEYMAP(0x587),
    PET_AUTO_HP(0x588),
    PET_AUTO_MP(0x589),
    PET_AUTO_CURE(0x58A),
    
    // CMapleTVMan::OnPacket (this has been updated.)
    START_TV(0x37D),
    REMOVE_TV(0x37E),
    ENABLE_TV(0x37F),
    
    GM_ERROR(0x26D),
    ALIEN_SOCKET_CREATOR(0x341),
    GOLDEN_HAMMER(0x279),
    // BATTLE_RECORD_DAMAGE_INFO(0x27A),
    // CALCULATE_REQUEST_RESULT(0x27B),
    
    // CFarm::OnPacket
    FARM_PACKET1(0x35C),
    FARM_ITEM_PURCHASED(0x35D),
    FARM_ITEM_GAIN(0x358),
    HARVEST_WARU(0x35A),
    FARM_MONSTER_GAIN(0x35B),
    FARM_INFO(0x368),
    FARM_MONSTER_INFO(0x369),
    FARM_QUEST_DATA(0x36A),
    FARM_QUEST_INFO(0x36B),
    FARM_MESSAGE(0x36C),
    UPDATE_MONSTER(0x36D),
    AESTHETIC_POINT(0x36E),
    UPDATE_WARU(0x36F),
    FARM_EXP(0x374),
    FARM_PACKET4(0x375),
    QUEST_ALERT(0x377),
    FARM_PACKET8(0x378),
    FARM_FRIENDS_BUDDY_REQUEST(0x37B),
    FARM_FRIENDS(0x37C),
    FARM_USER_INFO(0x388),
    FARM_AVATAR(0x38A),
    FRIEND_INFO(0x38D),
    FARM_RANKING(0x38F),
    SPAWN_FARM_MONSTER1(0x393),
    SPAWN_FARM_MONSTER2(0x394),
    RENAME_MONSTER(0x395),
    STRENGTHEN_UI(0x402),
    
    //Unplaced:
    MAPLE_POINT(0xED), // E6
    DEATH_COUNT(0x206),
    
    SHOW_DAMAGE_SKIN(0xDA),//:v
	
	// Unknown
    BOOSTER_PACK(0x999),
    BOOSTER_FAMILIAR(0x999),
    BLOCK_PORTAL(0x999),
    NPC_CONFIRM(0x999),
    RSA_KEY(0x999),
    LOGIN_AUTH(0x999),
    PET_FLAG_CHANGE(0x999),
    BUFF_BAR(0x999),
    GAME_POLL_REPLY(0x999),
    GAME_POLL_QUESTION(0x999),
    ENGLISH_QUIZ(0x999),
    BOAT_EFFECT(0x999),
    FISHING_CAUGHT(0x999);

    private short code = -2;

    public void setValue(short code) {
        this.code = code;
    }
    
    public void setValue(int code) {
    	setValue((short) code);
    }

    public short getValue() {
        return code;
    }

    private SendPacketOpcode(short code) {
        this.code = code;
    }
    
    private SendPacketOpcode(int code) {
    	this.code = ((short) code);
    }
    
	public static String getNameByValue(int value) {
		for (SendPacketOpcode opcode : SendPacketOpcode.values()) {
			if (opcode.getValue() == value) {
				return opcode.name();
			}
		}
		return "UNKNOWN";
	}
}

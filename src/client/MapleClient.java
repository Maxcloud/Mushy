package client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;

import org.apache.mina.common.IoSession;

import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.guild.MapleGuildCharacter;
import net.DatabaseConnection;
import net.DatabaseException;
import server.CharacterCardFactory;
import server.Timer.PingTimer;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import server.stores.IMaplePlayerShop;
import tools.MapleAESOFB;
import tools.Pair;
import tools.packet.CField;
import tools.packet.LoginPacket;

public class MapleClient implements Serializable {

	private static final long serialVersionUID = 9179541993413738569L;
	public static final byte LOGIN_NOTLOGGEDIN = 0, LOGIN_SERVER_TRANSITION = 1, LOGIN_LOGGEDIN = 2, CHANGE_CHANNEL = 3;
	private static final int DEFAULT_CHARSLOT = 8;
	public static final String CLIENT_KEY = "CLIENT";
	private final transient MapleAESOFB send, receive;
	private final transient IoSession session;
	private MapleCharacter player;
	private int channel = 1, accId = -1, world;
	private int charslots = DEFAULT_CHARSLOT;
	private boolean loggedIn = false, serverTransition = false;
	private transient Calendar tempban = null;
	private String accountName;
	private transient long lastPong = 0, lastPing = 0;
	private boolean monitored = false, receiving = true;
	private boolean gm;
	private byte greason = 1, gender = -1;
	public transient short loginAttempt = 0;
	public transient short couponAttempt = 0;
	private final transient List<Integer> allowedChar = new LinkedList<>();
	private final transient Set<String> macs = new HashSet<>();
	private final transient Map<String, ScriptEngine> engines = new HashMap<>();
	private transient ScheduledFuture<?> idleTask = null;
	private transient String secondPassword, salt2, tempIP = ""; // To be used
																	// only on
																	// login
	private final transient Lock mutex = new ReentrantLock(true);
	private final transient Lock npc_mutex = new ReentrantLock();
	private long lastNpcClick = 0;
	private final static Lock login_mutex = new ReentrantLock(true);
	private final Map<Integer, Pair<Short, Short>> charInfo = new LinkedHashMap<>();
	private int client_increnement = 1;

	public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
		this.send = send;
		this.receive = receive;
		this.session = session;
	}

	public final MapleAESOFB getReceiveCrypto() {
		return receive;
	}

	public final MapleAESOFB getSendCrypto() {
		return send;
	}

	public final IoSession getSession() {
		return session;
	}

	public final Lock getLock() {
		return mutex;
	}

	public final Lock getNPCLock() {
		return npc_mutex;
	}

	public MapleCharacter getPlayer() {
		return player;
	}

	public void setPlayer(MapleCharacter player) {
		this.player = player;
	}

	public void createdChar(final int id) {
		allowedChar.add(id);
	}

	public final boolean login_Auth(final int id) {
		return allowedChar.contains(id);
	}

	public final List<MapleCharacter> loadCharacters(final int serverId) {
		final List<MapleCharacter> chars = new LinkedList<>();

		final Map<Integer, CardData> cardss = CharacterCardFactory.getInstance().loadCharacterCards(accId, serverId);
		for (final CharNameAndId cni : loadCharactersInternal(serverId)) {
			final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false, cardss);
			chars.add(chr);
			charInfo.put(chr.getId(), new Pair<>(chr.getLevel(), chr.getJob()));
			if (!login_Auth(chr.getId())) {
				allowedChar.add(chr.getId());
			}
		}
		return chars;
	}

	public final void updateCharacterCards(final Map<Integer, Integer> cids) {
		System.out.println("updateCharacterCards: " + cids.toString());
		if (charInfo.isEmpty()) { // no characters
			return;
		}
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM `character_cards` WHERE `accid` = ?")) {
				ps.setInt(1, accId);
				ps.executeUpdate();
			}
			try (PreparedStatement psu = con.prepareStatement(
					"INSERT INTO `character_cards` (accid, worldid, characterid, position) VALUES (?, ?, ?, ?)")) {
				for (final Entry<Integer, Integer> ii : cids.entrySet()) {
					final Pair<Short, Short> info = charInfo.get(ii.getValue()); // charinfo we can use here as characters are already loaded
					if (info == null || ii.getValue() == 0 || !CharacterCardFactory.getInstance().canHaveCard(info.getLeft(), info.getRight())) {
						System.out.println("continue");
						continue;
					}
					psu.setInt(1, accId);
					psu.setInt(2, world);
					psu.setInt(3, ii.getValue());
					psu.setInt(4, ii.getKey()); // position shouldn't matter much, will reset upon login
					System.out.println(psu.getResultSet().toString());
					psu.executeUpdate();
				}
			}
		} catch (SQLException sqlE) {
			System.out.println("Failed to update character cards. Reason: " + sqlE.toString());
		}
	}

	public boolean canMakeCharacter(int serverId) {
		return loadCharactersSize(serverId) < getCharacterSlots();
	}

	public List<String> loadCharacterNames(int serverId) {
		List<String> chars = new LinkedList<>();
		for (CharNameAndId cni : loadCharactersInternal(serverId)) {
			chars.add(cni.name);
		}
		return chars;
	}

	private List<CharNameAndId> loadCharactersInternal(int serverId) {
		List<CharNameAndId> chars = new LinkedList<>();
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement("SELECT id, name, gm FROM characters WHERE accountid = ? AND world = ?")) {
				ps.setInt(1, accId);
				ps.setInt(2, serverId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
						LoginServer.getLoginAuth(rs.getInt("id"));
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("error loading characters internal");
		}
		return chars;
	}

	private int loadCharactersSize(int serverId) {
		int chars = 0;
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("SELECT count(*) FROM characters WHERE accountid = ? AND world = ?")) {
				ps.setInt(1, accId);
				ps.setInt(2, serverId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						chars = rs.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("error loading characters internal");
		}
		return chars;
	}

	public boolean isLoggedIn() {
		return loggedIn && accId >= 0;
	}

	private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
		Calendar lTempban = Calendar.getInstance();
		if (rs.getLong("tempban") == 0) { // basically if timestamp in db is
											// 0000-00-00
			lTempban.setTimeInMillis(0);
			return lTempban;
		}
		Calendar today = Calendar.getInstance();
		lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
		if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
			return lTempban;
		}

		lTempban.setTimeInMillis(0);
		return lTempban;
	}

	public Calendar getTempBanCalendar() {
		return tempban;
	}

	public byte getBanReason() {
		return greason;
	}

	public String showBanReason(String AccountID, boolean permban) {
		boolean autoban = getTrueBanReason(AccountID).toLowerCase().equals("autoban")
				|| getTrueBanReason(AccountID) == null;
		return showBanReason((byte) 0x7F, AccountID, permban, autoban, false);
	}

	private String showBanReason(byte type, String AccountID, boolean permban, boolean autoban, boolean showId) {
		StringBuilder reason = new StringBuilder();
		reason.append("Your account ").append(AccountID).append(" has been blocked for ");
		switch (type) {
		case 1:
			reason.append("hacking or illegal use of third-party programs.");
			break;
		case 2:
			reason.append("using macro / auto-keyboard.");
			break;
		case 3:
			reason.append("illicit promotion and advertising.");
			break;
		case 4:
			reason.append("harassment.");
			break;
		case 5:
			reason.append("using profane language.");
			break;
		case 6:
			reason.append("scamming.");
			break;
		case 7:
			reason.append("misconduct.");
			break;
		case 8:
			reason.append("illegal cash transaction.");
			break;
		case 9:
			reason.append("illegal charging/funding. Please contact customer support for further details.");
			break;
		case 10:
			reason.append("temporary request. Please contact customer support for further details.");
			break;
		case 11:
			reason.append("impersonating GM.");
			break;
		case 12:
			reason.append("using illegal programs or violating the handling policy.");
			break;
		case 13:
			reason.append("one of cursing, scamming, or illegal trading via Megaphones.");
			break;
		case 16:
		case 17:
		case 18:
			reason.append("Unknown reason 1.");
			break;
		case 19:
		case 20:
		case 21:
			reason.append("Unknown reason 2.");
			break;
		default:
			if (autoban) {
				reason.append("System has detected hacking or illegal use of third-party programs.");
			} else if (showId) {
				reason.append("MapleGM has blocked your account ").append(AccountID)
						.append(" for the following reason: ").append(getTrueBanReason(AccountID)); // Default
																									// reason
			} else {
				reason.append("Your account was blocked by the MapleStory GM's for ")
						.append(getTrueBanReason(AccountID));
			}
			break;
		}
		reason.append(permban ? "\r\n\r\nThis ban will never be lifted." : "");
		return reason.toString();
	}

	private String getTrueBanReason(String name) {
		String ret = null;
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement("SELECT banreason FROM accounts WHERE name = ?")) {
				ps.setString(1, name);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						ret = rs.getString(1);
					}
				}
			}
			return ret;
		} catch (SQLException ex) {
			System.err.println("Error getting ban reason: " + ex);
		}
		return ret;
	}

	public boolean hasBannedIP() {
		boolean ret = false;
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")) {
				ps.setString(1, getSessionIPAddress());
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					if (rs.getInt(1) > 0) {
						ret = true;
					}
				}
			}
		} catch (SQLException ex) {
			System.err.println("Error checking ip bans" + ex);
		}
		return ret;
	}

	public boolean hasBannedMac() {
		if (macs.isEmpty()) {
			return false;
		}
		boolean ret = false;
		int i;
		try {
			Connection con = DatabaseConnection.getConnection();
			StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
			for (i = 0; i < macs.size(); i++) {
				sql.append("?");
				if (i != macs.size() - 1) {
					sql.append(", ");
				}
			}
			sql.append(")");
			try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
				i = 0;
				for (String mac : macs) {
					i++;
					ps.setString(i, mac);
				}
				try (ResultSet rs = ps.executeQuery()) {
					rs.next();
					if (rs.getInt(1) > 0) {
						ret = true;
					}
				}
			}
		} catch (SQLException ex) {
			System.err.println("Error checking mac bans" + ex);
		}
		return ret;
	}

	private void loadMacsIfNescessary() throws SQLException {
		if (macs.isEmpty()) {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?")) {
				ps.setInt(1, accId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						if (rs.getString("macs") != null) {
							String[] macData;
							macData = rs.getString("macs").split(", ");
							for (String mac : macData) {
								if (!mac.equals("")) {
									macs.add(mac);
								}
							}
						}
					} else {
						rs.close();
						ps.close();
						throw new RuntimeException("No valid account associated with this client.");
					}
				}
			}
		}
	}

	void banMacs() {
		try {
			loadMacsIfNescessary();
			if (this.macs.size() > 0) {
				String[] macBans = new String[this.macs.size()];
				int z = 0;
				for (String mac : this.macs) {
					macBans[z] = mac;
					z++;
				}
				banMacs(macBans);
			}
		} catch (SQLException e) {
		}
	}

	static void banMacs(String[] macs) {
		Connection con = DatabaseConnection.getConnection();
		try {
			List<String> filtered = new LinkedList<>();
			PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				filtered.add(rs.getString("filter"));
			}
			rs.close();
			ps.close();

			ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
			for (String mac : macs) {
				boolean matched = false;
				for (String filter : filtered) {
					if (mac.matches(filter)) {
						matched = true;
						break;
					}
				}
				if (!matched) {
					ps.setString(1, mac);
					try {
						ps.executeUpdate();
					} catch (SQLException e) {
						// can fail because of UNIQUE key, we dont care
					}
				}
			}
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error banning MACs" + e);
		}
	}

	/**
	 * Returns 0 on success, a state to be used for
	 * {@link CField#getLoginFailed(int)} otherwise.
	 *
	 * @return The state of the login.
	 */
	public int finishLogin() {
		login_mutex.lock();
		try {
			final byte state = getLoginState();
			if (state > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
				loggedIn = false;
				return 7;
			}
			updateLoginState(MapleClient.LOGIN_LOGGEDIN, getSessionIPAddress());
		} finally {
			login_mutex.unlock();
		}
		return 0;
	}

	public void clearInformation() {
		accountName = null;
		accId = -1;
		secondPassword = null;
		salt2 = null;
		gm = false;
		loggedIn = false;
		greason = (byte) 1;
		tempban = null;
		gender = (byte) -1;
		charInfo.clear();
	}

	public int login(String login, String pwd, boolean ipMacBanned) {
		int loginok = 5;
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
			ps.setString(1, login);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				final int banned = rs.getInt("banned");
				final String passhash = rs.getString("password");
				final String salt = rs.getString("salt");
				final String oldSession = rs.getString("SessionIP");

				accountName = login;
				accId = rs.getInt("id");
				secondPassword = rs.getString("2ndpassword");
				salt2 = rs.getString("salt2");
				gm = rs.getInt("gm") > 0;
				greason = rs.getByte("greason");
				tempban = getTempBanCalendar(rs);
				gender = rs.getByte("gender");

				final boolean admin = rs.getInt("gm") > 1;

				if (secondPassword != null && salt2 != null) {
					secondPassword = LoginCrypto.rand_r(secondPassword);
				}
				ps.close();

				if (banned > 0 && gm) {
					loginok = 3;
				} else {
					if (banned == -1) {
						unban();
					}
					byte loginstate = getLoginState();
					if (loginstate > MapleClient.LOGIN_NOTLOGGEDIN) { // already
																		// loggedin
						loggedIn = false;
						loginok = 7;
						if (pwd.equalsIgnoreCase("fixme")) {
							try {
								ps = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE name = ?");
								ps.setString(1, login);
								ps.executeUpdate();
								ps.close();
							} catch (SQLException se) {
							}
						}
					} else {
						boolean updatePasswordHash = false;
						// Check if the passwords are correct here. :B
						if (passhash == null || passhash.isEmpty()) {
							// match by sessionIP
							if (oldSession != null && !oldSession.isEmpty()) {
								loggedIn = getSessionIPAddress().equals(oldSession);
								loginok = loggedIn ? 0 : 4;
								updatePasswordHash = loggedIn;
							} else {
								loginok = 4;
								loggedIn = false;
							}
						} else if (LoginCryptoLegacy.isLegacyPassword(passhash)
								&& LoginCryptoLegacy.checkPassword(pwd, passhash)) {
							// Check if a password upgrade is needed.
							loginok = 0;
							updatePasswordHash = true;
						} else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
							loginok = 0;
							updatePasswordHash = true;
						} else if (salt != null && LoginCrypto.checkSaltedSha1Hash(passhash, pwd, salt)) {
							loginok = 0; // new standard
						} else if (salt != null && LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
							updatePasswordHash = true; // migrates away from
														// Sha512, higher bit
														// count but
														// incompatible
							loginok = 0;
							/*
							 * Take out to reflect salted SHA1 Redirector Java's
							 * SHA512 implementation is incompatible Enable only
							 * if you know what you're doing
							 */
						} else {
							loggedIn = false;
							loginok = 4;
						}
						if (updatePasswordHash) {
							try (PreparedStatement pss = con.prepareStatement(
									"UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?")) {
								final String newSalt = LoginCrypto.makeSalt();
								pss.setString(1, LoginCrypto.makeSaltedSha1Hash(pwd, newSalt));
								pss.setString(2, newSalt);
								pss.setInt(3, accId);
								pss.executeUpdate();
							}
						}
					}
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.err.println("ERROR" + e);
		}
		return loginok;
	}

	public boolean CheckSecondPassword(String in) {
		boolean allow = false;
		boolean updatePasswordHash = false;

		// Check if the passwords are correct here. :B
		if (LoginCryptoLegacy.isLegacyPassword(secondPassword) && LoginCryptoLegacy.checkPassword(in, secondPassword)) {
			// Check if a password upgrade is needed.
			allow = true;
			updatePasswordHash = true;
		} else if (salt2 == null && LoginCrypto.checkSha1Hash(secondPassword, in)) {
			allow = true;
			updatePasswordHash = true;
		} else if (LoginCrypto.checkSaltedSha512Hash(secondPassword, in, salt2)) {
			allow = true;
		}
		if (updatePasswordHash) {
			Connection con = DatabaseConnection.getConnection();
			try {
				try (PreparedStatement ps = con
						.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?")) {
					final String newSalt = LoginCrypto.makeSalt();
					ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(in, newSalt)));
					ps.setString(2, newSalt);
					ps.setInt(3, accId);
					ps.executeUpdate();
				}
			} catch (SQLException e) {
				return false;
			}
		}
		return allow;
	}

	private void unban() {
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?")) {
				ps.setInt(1, accId);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
		}
	}

	public static byte unban(String charname) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
			ps.setString(1, charname);

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			final int accid = rs.getInt(1);
			rs.close();
			ps.close();

			ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
			ps.setInt(1, accid);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
			return -2;
		}
		return 0;
	}

	public void updateMacs(String macData) {
		macs.addAll(Arrays.asList(macData.split(", ")));
		StringBuilder newMacData = new StringBuilder();
		Iterator<String> iter = macs.iterator();
		while (iter.hasNext()) {
			newMacData.append(iter.next());
			if (iter.hasNext()) {
				newMacData.append(", ");
			}
		}
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?")) {
				ps.setString(1, newMacData.toString());
				ps.setInt(2, accId);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println("Error saving MACs" + e);
		}
	}

	public void setAccID(int id) {
		this.accId = id;
	}

	public int getAccID() {
		return this.accId;
	}

	public final void updateLoginState(final int newstate, final String SessionID) {
		try {
			final Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement(
					"UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?")) {
				ps.setInt(1, newstate);
				ps.setString(2, SessionID);
				ps.setInt(3, getAccID());
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println("error updating login state " + e);
		}
		if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
			loggedIn = false;
			serverTransition = false;
		} else {
			serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION
					|| newstate == MapleClient.CHANGE_CHANNEL);
			loggedIn = !serverTransition;
		}
	}

	public final void updateSecondPassword() {
		try {
			final Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?")) {
				final String newSalt = LoginCrypto.makeSalt();
				ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(secondPassword, newSalt)));
				ps.setString(2, newSalt);
				ps.setInt(3, accId);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			System.err.println("error updating login state " + e);
		}
	}

	public final byte getLoginState() {
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps;
			ps = con.prepareStatement(
					"SELECT loggedin, lastlogin, banned, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?");
			ps.setInt(1, getAccID());
			byte state;
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next() || rs.getInt("banned") > 0) {
					ps.close();
					rs.close();
					session.close();
					throw new DatabaseException("Account doesn't exist or is banned");
				}
				rs.getInt("bday");
				state = rs.getByte("loggedin");
				if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
					if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) { // connecting
																										// to
																										// chanserver
																										// timeout
						state = MapleClient.LOGIN_NOTLOGGEDIN;
						updateLoginState(state, getSessionIPAddress());
					}
				}
			}
			ps.close();
			loggedIn = state == MapleClient.LOGIN_LOGGEDIN;
			return state;
		} catch (SQLException e) {
			loggedIn = false;
			throw new DatabaseException("error getting login state", e);
		}
	}

	private final void removalTask(boolean shutdown) {
		try {
			player.cancelAllBuffs_();
			player.cancelAllDebuffs();
			if (player.getMarriageId() > 0) {
				final MapleQuestStatus stat1 = player.getQuestNoAdd(MapleQuest.getInstance(160001));
				final MapleQuestStatus stat2 = player.getQuestNoAdd(MapleQuest.getInstance(160002));
				if (stat1 != null && stat1.getCustomData() != null
						&& (stat1.getCustomData().equals("2_") || stat1.getCustomData().equals("2"))) {
					// dc in process of marriage
					if (stat2 != null && stat2.getCustomData() != null) {
						stat2.setCustomData("0");
					}
					stat1.setCustomData("3");
				}
			}
			player.changeRemoval(true);
			if (player.getEventInstance() != null) {
				player.getEventInstance().playerDisconnected(player, player.getId());
			}
			final IMaplePlayerShop shop = player.getPlayerShop();
			if (shop != null) {
				shop.removeVisitor(player);
				if (shop.isOwner(player)) {
					if (shop.getShopType() == 1 && shop.isAvailable() && !shutdown) {
						shop.setOpen(true);
					} else {
						shop.closeShop(true, !shutdown);
					}
				}
			}
			player.setMessenger(null);
			if (player.getMap() != null) {
				if (shutdown || (getChannelServer() != null && getChannelServer().isShutdown())) {
					int questID = -1;
					switch (player.getMapId()) {
					case 240060200: // HT
						questID = 160100;
						break;
					case 240060201: // ChaosHT
						questID = 160103;
						break;
					case 280030000: // Zakum
						questID = 160101;
						break;
					case 280030001: // ChaosZakum
						questID = 160102;
						break;
					case 270050100: // PB
						questID = 160101;
						break;
					case 105100300: // Balrog
					case 105100400: // Balrog
						questID = 160106;
						break;
					case 211070000: // VonLeon
					case 211070100: // VonLeon
					case 211070101: // VonLeon
					case 211070110: // VonLeon
						questID = 160107;
						break;
					case 551030200: // scartar
						questID = 160108;
						break;
					case 271040100: // cygnus
						questID = 160109;
						break;
					case 262030000:
					case 262031300: // hilla
						questID = 160110;
						break;
					case 272030400:
						questID = 160111;
						break;
					}
					if (questID > 0) {
						player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData("0"); // reset
																									// the
																									// time.
					}
				} else if (player.isAlive()) {
					switch (player.getMapId()) {
					case 541010100: // latanica
					case 541020800: // krexel
					case 220080001: // pap
						player.getMap().addDisconnected(player.getId());
						break;
					}
				}
				player.getMap().removePlayer(player);
			}
		} catch (final NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS) {
		disconnect(RemoveInChannelServer, fromCS, false);
	}

	public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown) {
		if (player != null) {
			MapleMap map = player.getMap();
			final MapleParty party = player.getParty();
			final boolean clone = player.isClone();
			final String namez = player.getName();
			final int idz = player.getId(),
					messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId(),
					gid = player.getGuildId();
			final BuddyList bl = player.getBuddylist();
			final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
			final MapleMessengerCharacter chrm = new MapleMessengerCharacter(player);
			final MapleGuildCharacter chrg = player.getMGC();

			removalTask(shutdown);
			LoginServer.getLoginAuth(player.getId());
			player.saveToDB(true, fromCS);
			if (shutdown) {
				player = null;
				receiving = false;
				return;
			}

			if (!fromCS) {
				final ChannelServer ch = ChannelServer.getInstance(map == null ? channel : map.getChannel());
				final int chz = World.Find.findChannel(idz);
				if (chz < -1) {
					disconnect(RemoveInChannelServer, true);// u lie
					return;
				}
				try {
					if (chz == -1 || ch == null || clone || ch.isShutdown()) {
						player = null;
						return;// no idea
					}
					if (messengerid > 0) {
						World.Messenger.leaveMessenger(messengerid, chrm);
					}
					if (party != null) {
						chrp.setOnline(false);
						World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
						if (map != null && party.getLeader().getId() == idz) {
							MaplePartyCharacter lchr = null;
							for (MaplePartyCharacter pchr : party.getMembers()) {
								if (pchr != null && map.getCharacterById(pchr.getId()) != null
										&& (lchr == null || lchr.getLevel() < pchr.getLevel())) {
									lchr = pchr;
								}
							}
							if (lchr != null) {
								World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
							}
						}
					}
					if (bl != null) {
						if (!serverTransition) {
							World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
						} else { // Change channel
							World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
						}
					}
					if (gid > 0 && chrg != null) {
						World.Guild.setGuildMemberOnline(chrg, false, -1);
					}
				} catch (final Exception e) {
					System.err.println(getLogMessage(this, "ERROR") + e);
				} finally {
					if (RemoveInChannelServer && ch != null) {
						ch.removePlayer(idz, namez);
					}
					player = null;
				}
			} else {
				final int ch = World.Find.findChannel(idz);
				if (ch > 0) {
					disconnect(RemoveInChannelServer, false);// u lie
					return;
				}
				try {
					if (party != null) {
						chrp.setOnline(false);
						World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
					}
					if (!serverTransition) {
						World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
					} else { // Change channel
						World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
					}
					if (gid > 0 && chrg != null) {
						World.Guild.setGuildMemberOnline(chrg, false, -1);
					}
					if (player != null) {
						player.setMessenger(null);
					}
				} catch (final Exception e) {
					System.err.println(getLogMessage(this, "ERROR") + e);
				} finally {
					if (RemoveInChannelServer && ch > 0) {
						CashShopServer.getPlayerStorage().deregisterPlayer(idz, namez);
					}
					player = null;
				}
			}
		}
		if (!serverTransition && isLoggedIn()) {
			updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getSessionIPAddress());
		}
		engines.clear();
	}

	public final String getSessionIPAddress() {
		return session.getRemoteAddress().toString().split(":")[0];
	}

	public final boolean CheckIPAddress() {
		if (this.accId < 0) {
			return false;
		}
		try {
			boolean canlogin = false;
			try (PreparedStatement ps = DatabaseConnection.getConnection()
					.prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?");) {
				ps.setInt(1, this.accId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						final String sessionIP = rs.getString("SessionIP");
						if (sessionIP != null) { // Probably a login proced
													// skipper?
							canlogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
						}
						if (rs.getInt("banned") > 0) {
							canlogin = false; // canlogin false = close client
						}
					}
				}
			}
			return canlogin;
		} catch (final SQLException e) {
			System.out.println("Failed in checking IP address for client.");
		}
		return true;
	}

	public final int getChannel() {
		return channel;
	}

	public final ChannelServer getChannelServer() {
		return ChannelServer.getInstance(channel);
	}

	public final int deleteCharacter(final int cid) {
		try {
			final Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con.prepareStatement(
					"SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?")) {
				ps.setInt(1, cid);
				ps.setInt(2, accId);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						rs.close();
						ps.close();
						return 9;
					}
					if (rs.getInt("guildid") > 0) { // is in a guild when
													// deleted
						if (rs.getInt("guildrank") == 1) { // cant delete when
															// leader
							rs.close();
							ps.close();
							return 22;
						}
						World.Guild.deleteGuildCharacter(rs.getInt("guildid"), cid);
					}
				}
			}

			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE buddyid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
			MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?", cid);
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 10;
	}

	public final byte getGender() {
		return gender;
	}

	public final void setGender(final byte gender) {
		this.gender = gender;
	}

	public final String getSecondPassword() {
		return secondPassword;
	}

	public final void setSecondPassword(final String secondPassword) {
		this.secondPassword = secondPassword;
	}

	public final String getAccountName() {
		return accountName;
	}

	public final void setAccountName(final String accountName) {
		this.accountName = accountName;
	}

	public final void setChannel(final int channel) {
		this.channel = channel;
	}

	public final int getWorld() {
		return world;
	}

	public final void setWorld(final int world) {
		this.world = world;
	}

	public final int getLatency() {
		return (int) (lastPong - lastPing);
	}

	public final long getLastPong() {
		return lastPong;
	}

	public final long getLastPing() {
		return lastPing;
	}

	public final void pongReceived() {
		lastPong = System.currentTimeMillis();
	}

	private static String getLogMessage(final MapleClient cfor, final String message) {
		return getLogMessage(cfor, message, new Object[0]);
	}

	public static String getLogMessage(final MapleCharacter cfor, final String message) {
		return getLogMessage(cfor == null ? null : cfor.getClient(), message);
	}

	private static String getLogMessage(final MapleClient cfor, final String message, final Object... parms) {
		final StringBuilder builder = new StringBuilder();
		if (cfor != null) {
			if (cfor.getPlayer() != null) {
				builder.append("<");
				builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
				builder.append(" (cid: ");
				builder.append(cfor.getPlayer().getId());
				builder.append(")> ");
			}
			if (cfor.getAccountName() != null) {
				builder.append("(Account: ");
				builder.append(cfor.getAccountName());
				builder.append(") ");
			}
		}
		builder.append(message);
		int start;
		for (final Object parm : parms) {
			start = builder.indexOf("{}");
			builder.replace(start, start + 2, parm.toString());
		}
		return builder.toString();
	}

	public final Set<String> getMacs() {
		return Collections.unmodifiableSet(macs);
	}

	public final boolean isGm() {
		return gm;
	}

	public final void setScriptEngine(final String name, final ScriptEngine e) {
		engines.put(name, e);
	}

	public final ScriptEngine getScriptEngine(final String name) {
		return engines.get(name);
	}

	public final void removeScriptEngine(final String name) {
		engines.remove(name);
	}

	public final ScheduledFuture<?> getIdleTask() {
		return idleTask;
	}

	public final void setIdleTask(final ScheduledFuture<?> idleTask) {
		this.idleTask = idleTask;
	}

	private static final class CharNameAndId {

		private final String name;
		private final int id;

		private CharNameAndId(final String name, final int id) {
			super();
			this.name = name;
			this.id = id;
		}
	}

	public int getCharacterSlots() {
		if (charslots != DEFAULT_CHARSLOT) {
			return charslots; // save a sql
		}
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?")) {
				ps.setInt(1, accId);
				ps.setInt(2, world);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						charslots = rs.getInt("charslots");
					} else {
						try (PreparedStatement psu = con.prepareStatement(
								"INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)")) {
							psu.setInt(1, accId);
							psu.setInt(2, world);
							psu.setInt(3, charslots);
							psu.executeUpdate();
						}
					}
				}
			}
		} catch (SQLException sqlE) {
		}

		return charslots;
	}

	public boolean gainCharacterSlot() {
		if (getCharacterSlots() >= 15) {
			return false;
		}
		charslots++;
		try {
			Connection con = DatabaseConnection.getConnection();
			try (PreparedStatement ps = con
					.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?")) {
				ps.setInt(1, charslots);
				ps.setInt(2, world);
				ps.setInt(3, accId);
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException sqlE) {
			return false;
		}
		return true;
	}

	public static byte unbanIPMacs(String charname) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
			ps.setString(1, charname);

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			final int accid = rs.getInt(1);
			rs.close();
			ps.close();

			ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
			ps.setInt(1, accid);
			rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			final String sessionIP = rs.getString("sessionIP");
			final String macs = rs.getString("macs");
			rs.close();
			ps.close();
			byte ret = 0;
			if (sessionIP != null) {
				try (PreparedStatement psa = con.prepareStatement("DELETE FROM ipbans WHERE ip like ?")) {
					psa.setString(1, sessionIP);
					psa.execute();
				}
				ret++;
			}
			if (macs != null) {
				String[] macz;
				macz = macs.split(", ");
				for (String mac : macz) {
					if (!mac.equals("")) {
						try (PreparedStatement psa = con.prepareStatement("DELETE FROM macbans WHERE mac = ?")) {
							psa.setString(1, mac);
							psa.execute();
						}
					}
				}
				ret++;
			}
			return ret;
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
			return -2;
		}
	}

	public static byte unHellban(String charname) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
			ps.setString(1, charname);

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			final int accid = rs.getInt(1);
			rs.close();
			ps.close();

			ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
			ps.setInt(1, accid);
			rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			final String sessionIP = rs.getString("sessionIP");
			final String email = rs.getString("email");
			rs.close();
			ps.close();
			ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?"
					+ (sessionIP == null ? "" : " OR sessionIP = ?"));
			ps.setString(1, email);
			if (sessionIP != null) {
				ps.setString(2, sessionIP);
			}
			ps.execute();
			ps.close();
			return 0;
		} catch (SQLException e) {
			System.err.println("Error while unbanning" + e);
			return -2;
		}
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean m) {
		this.monitored = m;
	}

	public boolean isReceiving() {
		return receiving;
	}

	public void setReceiving(boolean m) {
		this.receiving = m;
	}

	public boolean canClickNPC() {
		return lastNpcClick + 500 < System.currentTimeMillis();
	}

	public void setClickedNPC() {
		lastNpcClick = System.currentTimeMillis();
	}

	public void removeClickedNPC() {
		lastNpcClick = 0;
	}

	public final Timestamp getCreated() {
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT createdat FROM accounts WHERE id = ?");
			ps.setInt(1, getAccID());
			Timestamp ret;
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					rs.close();
					ps.close();
					return null;
				}
				ret = rs.getTimestamp("createdat");
			}
			ps.close();
			return ret;
		} catch (SQLException e) {
			throw new DatabaseException("error getting create", e);
		}
	}

	public String getTempIP() {
		return tempIP;
	}

	public void setTempIP(String s) {
		this.tempIP = s;
	}

	public void setUsername(String what) {
		this.accountName = what;
	}

	public int getNextClientIncrenement() {
		int result = client_increnement;
		client_increnement++;
		return result;
	}

	public void sendPing() {
        final long then = System.currentTimeMillis();
        getSession().write(LoginPacket.getPing());
        
        PingTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    if (lastPong - then < 0) {
                        if (getSession().isConnected()) {
                        	System.out.println("Attempting to close the connection. :(");
                            // 	getSession().close();
                        }
                    }
                } catch (NullPointerException e) {
                    // client already gone
                }
            }
        }, 10000); // note: idletime gets added to this too)
     
    }

	//0 = "You must create PIC before proceeding"
	//1 = Enter your PIC
	//2/3 = No PIC asked
	//4 = "You haven't changed your PIC in a while"
	public byte getPicStatus() {
		if (secondPassword == null){
			return 0;
		}
		if (secondPassword.length() > 0){
			return 1;
		}
		if (secondPassword.length() <= 0){
			return 2;
		}
		return 0;
	}
}

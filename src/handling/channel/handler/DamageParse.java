package handling.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import server.MapleStatEffect;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import tools.AttackPair;
import tools.Pair;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class DamageParse {

	@SuppressWarnings("empty-statement")
	public static void applyAttack(AttackInfo attack, Skill theSkill, MapleCharacter player, int attackCount,
			double maxDamagePerMonster, MapleStatEffect effect, AttackType attack_type) {
		if (attack.skill != 0) {
			if (effect == null) {
				player.getClient().getSession().write(CWvsContext.enableActions());
				return;
			}
			if (GameConstants.isMulungSkill(attack.skill)) {
				if (player.getMapId() / 10000 != 92502) {
					return;
				}
				if (player.getMulungEnergy() < 10000) {
					return;
				}
				player.mulung_EnergyModify(false);
			} else if (GameConstants.isPyramidSkill(attack.skill)) {
				if (player.getMapId() / 1000000 != 926) {
					return;
				}

				if ((player.getPyramidSubway() != null) && (player.getPyramidSubway().onSkillUse(player)))
					;
			} else if (GameConstants.isInflationSkill(attack.skill)) {
				if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) != null)
					;
			}
		}
		/*
		 * if (player.getClient().getChannelServer().isAdminOnly()) {
		 * player.dropMessage(-1, new StringBuilder().append("Animation: "
		 * ).append(Integer.toHexString((attack.display & 0x8000) != 0 ?
		 * attack.display - 32768 : attack.display)).toString()); }
		 */
		boolean useAttackCount = (attack.skill != 4211006) && (attack.skill != 3221007) && (attack.skill != 23121003)
				&& ((attack.skill != 1311001) || (player.getJob() != 132)) && (attack.skill != 3211006);

		if ((attack.hits > 0) && (attack.targets > 0)) {
			if (!player.getStat().checkEquipDurabilitys(player, -1)) {
				player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
				return;
			}
		}
		int totDamage = 0;
		MapleMap map = player.getMap();
		if (attack.skill == 4211006) {
			for (AttackPair oned : attack.allDamage) {
				if (oned.attack == null) {
					MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);

					if (mapobject != null) {
						MapleMapItem mapitem = (MapleMapItem) mapobject;
						mapitem.getLock().lock();
						try {
							if (mapitem.getMeso() > 0) {
								if (mapitem.isPickedUp()) {
									return;
								}
								map.removeMapObject(mapitem);
								map.broadcastMessage(CField.explodeDrop(mapitem.getObjectId()));
								mapitem.setPickedUp(true);
							}
						} finally {
							mapitem.getLock().unlock();
						}
					}
				}
			}
		}
		int totDamageToOneMonster = 0;
		long hpMob = 0L;
		PlayerStats stats = player.getStat();

		int CriticalDamage = stats.passive_sharpeye_percent();
		int ShdowPartnerAttackPercentage = 0;
		if ((attack_type == AttackType.RANGED_WITH_SHADOWPARTNER)
				|| (attack_type == AttackType.NON_RANGED_WITH_MIRROR)) {
			MapleStatEffect shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.SHADOWPARTNER);
			if (shadowPartnerEffect != null) {
				ShdowPartnerAttackPercentage += shadowPartnerEffect.getX();
			}
			attackCount /= 2;
		}
		ShdowPartnerAttackPercentage *= (CriticalDamage + 100) / 100;
		if (attack.skill == 4221001) {
			ShdowPartnerAttackPercentage *= 10;
		}

		double maxDamagePerHit = 0.0D;

		int antiKS = 0;
		for (AttackPair oned : attack.allDamage) {
			MapleMonster monster = map.getMonsterByOid(oned.objectid);

			if (ServerConstants.AntiKS) {
				MapleQuestStatus statt = player.getQuestNoAdd(MapleQuest.getInstance(732648172));
				boolean antiks;
				if (statt == null || statt.getCustomData() == null) {
					antiks = false;
					statt.setCustomData(antiks + ";" + 0);
				} else {
					String[] statss = statt.getCustomData().split(";");
					try {
						antiks = Boolean.parseBoolean(statss[0]);
					} catch (Exception ex) {
						antiks = false;
					}
					if (monster.getBelongsToSomeone() && monster.getBelongsTo() != player.getId()
							&& (player.getParty() == null
									|| player.getParty().getMemberById(monster.getBelongsTo()) == null)
							&& !player.isGM()) {
						monster.setBelongsTo(player);
						antiKS++;
						statt.setCustomData(antiks + ";" + (Integer.parseInt(statss[1]) + 1));
					}
				}
				if (monster.getBelongsToSomeone()) {
					if (monster.getBelongsTo() != player.getId() && !player.isGM()) {
						player.dropMessage(5, "You cannot hit this monster because it belongs to someone else.");
						continue;
					}
					if (player.isGM()) {
						player.dropMessage(5, "You are attacking a monster who was marked by another player.");
					}
				}
			}

			if ((monster != null) && (monster.getLinkCID() <= 0)) {
				totDamageToOneMonster = 0;
				hpMob = monster.getMobMaxHp();
				MapleMonsterStats monsterstats = monster.getStats();
				int fixeddmg = monsterstats.getFixedDamage();
				boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006)
						|| (attack.skill == 21120006) || (attack.skill == 1221011);

				if ((!Tempest) && (!player.isGM())) {
					if (((player.getJob() >= 3200) && (player.getJob() <= 3212)
							&& (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY))
							&& (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY))
							&& (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) || (attack.skill == 3221007)
							|| (attack.skill == 23121003)
							|| (((player.getJob() < 3200) || (player.getJob() > 3212))
									&& (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY))
									&& (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY))
									&& (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)))) {
						maxDamagePerHit = CalculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect,
								maxDamagePerMonster, Integer.valueOf(CriticalDamage));
					} else {
						maxDamagePerHit = 1.0D;
					}
				}
				byte overallAttackCount = 0;

				int criticals = 0;
				for (Pair eachde : oned.attack) {
					Integer eachd = (Integer) eachde.left;
					overallAttackCount = (byte) (overallAttackCount + 1);
					if (((Boolean) eachde.right).booleanValue()) {
						criticals++;
					}
					if ((useAttackCount) && (overallAttackCount - 1 == attackCount)) {
						maxDamagePerHit = maxDamagePerHit / 100.0D * (ShdowPartnerAttackPercentage
								* (monsterstats.isBoss() ? stats.bossdam_r : stats.dam_r) / 100.0D);
					}

					if (fixeddmg != -1) {
						if (monsterstats.getOnlyNoramlAttack()) {
							eachd = Integer.valueOf(attack.skill != 0 ? 0 : fixeddmg);
						} else {
							eachd = Integer.valueOf(fixeddmg);
						}
					} else if (monsterstats.getOnlyNoramlAttack()) {
						eachd = Integer
								.valueOf(attack.skill != 0 ? 0 : Math.min(eachd.intValue(), (int) maxDamagePerHit));
					} else if (!player.isGM()) {
						if (Tempest) {
							if (eachd.intValue() > monster.getMobMaxHp()) {
								eachd = Integer.valueOf((int) Math.min(monster.getMobMaxHp(), 2147483647L));
							}
						} else if (((player.getJob() >= 3200) && (player.getJob() <= 3212)
								&& (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY))
								&& (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY))
								&& (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)))
								|| (attack.skill == 23121003)
								|| (((player.getJob() < 3200) || (player.getJob() > 3212))
										&& (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY))
										&& (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY))
										&& (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)))) {
							if (eachd.intValue() > maxDamagePerHit) {

								if (eachd.intValue() > maxDamagePerHit * 2.0D) {
									eachd = Integer.valueOf((int) (maxDamagePerHit * 2.0D));
									if (eachd.intValue() >= 2499999) {
										player.getClient().getSession().close();
									}
								}
							}

						} else if (eachd.intValue() > maxDamagePerHit) {
							eachd = Integer.valueOf((int) maxDamagePerHit);
						}

					}

					if (player == null) {
						return;
					}
					totDamageToOneMonster += eachd.intValue();

					if (((eachd.intValue() == 0) || (monster.getId() == 9700021))
							&& (player.getPyramidSubway() != null)) {
						player.getPyramidSubway().onMiss(player);
					}
				}
				totDamage += totDamageToOneMonster;
				player.checkMonsterAggro(monster);

				if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)
						&& (!GameConstants.isNoDelaySkill(attack.skill)) && (attack.skill != 3101005)
						&& (!monster.getStats().isBoss())
						&& (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants
								.getAttackRange(effect, player.getStat().defRange))) {
				}

				if (player.getSkillLevel(36110005) > 0) {
					Skill skill = SkillFactory.getSkill(36110005);
					MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));
					if (player.getLastCombo() + 5000 < System.currentTimeMillis()) {
						monster.setTriangulation(0);
						// player.clearDamageMeters();
					}
					if (eff.makeChanceResult()) {
						player.setLastCombo(System.currentTimeMillis());
						if (monster.getTriangulation() < 3) {
							monster.setTriangulation(monster.getTriangulation() + 1);
						}
						monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.DARKNESS, eff.getX(),
								eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
						monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.TRIANGULATION,
								monster.getTriangulation(), eff.getSourceId(), null, false), false, eff.getY() * 1000,
								true, eff);
					}
				}

				if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
					switch (attack.skill) {
					case 0:
					case 4001334:
					case 4201005:
					case 4211002:
					case 4211004:
					case 4221003:
					case 4221007:
						handlePickPocket(player, monster, oned);
					}

				}

				if ((totDamageToOneMonster > 0) || (attack.skill == 1221011) || (attack.skill == 21120006)) {
					if (GameConstants.isDemonSlayer(player.getJob())) {
						player.handleForceGain(monster.getObjectId(), attack.skill);
					}
					if ((GameConstants.isPhantom(player.getJob())) && (attack.skill != 24120002)
							&& (attack.skill != 24100003)) {
						player.handleCardStack();
					}

					if (GameConstants.isKaiser(player.getJob())) {
						player.handleKaiserCombo();
					}
					if (attack.skill != 1221011) {
						monster.damage(player, totDamageToOneMonster, true, attack.skill);
					} else {
						monster.damage(player, monster.getStats().isBoss() ? 500000L : monster.getHp() - 1L, true,
								attack.skill);
					}

					if (monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
						player.addHP(-(7000 + Randomizer.nextInt(8000)));
					}
					player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skill, monster.getObjectId(),
							totDamage, 0);
					switch (attack.skill) {
					case 4001002:
					case 4001334:
					case 4001344:
					case 4111005:
					case 4121007:
					case 4201005:
					case 4211002:
					case 4221001:
					case 4221007:
					case 4301001:
					case 4311002:
					case 4311003:
					case 4331000:
					case 4331004:
					case 4331005:
					case 4331006:
					case 4341002:
					case 4341004:
					case 4341005:
					case 4341009:
					case 14001004:
					case 14111002:
					case 14111005:
						int[] skills = { 4120005, 4220005, 4340001, 14110004 };
						for (int i : skills) {
							Skill skill = SkillFactory.getSkill(i);
							if (player.getTotalSkillLevel(skill) > 0) {
								MapleStatEffect venomEffect = skill.getEffect(player.getTotalSkillLevel(skill));
								if (!venomEffect.makeChanceResult()) {
									break;
								}
								monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.POISON,
										Integer.valueOf(1), i, null, false), true, venomEffect.getDuration(), true,
										venomEffect);
								break;
							}

						}

						break;
					case 4201004:
						monster.handleSteal(player);
						break;
					case 21000002:
					case 21100001:
					case 21100002:
					case 21100004:
					case 21110002:
					case 21110003:
					case 21110004:
					case 21110006:
					case 21110007:
					case 21110008:
					case 21120002:
					case 21120005:
					case 21120006:
					case 21120009:
					case 21120010:
						if ((player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null)
								&& (!monster.getStats().isBoss())) {
							MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
							if (eff != null) {
								monster.applyStatus(
										player, new MonsterStatusEffect(MonsterStatus.SPEED,
												Integer.valueOf(eff.getX()), eff.getSourceId(), null, false),
										false, eff.getY() * 1000, true, eff);
							}
						}
						if ((player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null)
								&& (!monster.getStats().isBoss())) {
							MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);

							if ((eff != null) && (eff.makeChanceResult())
									&& (!monster.isBuffed(MonsterStatus.NEUTRALISE))) {
								monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE,
										Integer.valueOf(1), eff.getSourceId(), null, false), false, eff.getX() * 1000,
										true, eff);
							}
						}
						break;
					}
					// int randomDMG = Randomizer.nextInt(player.getDamage2() -
					// player.getReborns() + 1) + player.getReborns();
					// monster.damage(player, randomDMG, true, attack.skill);
					// if (player.getshowdamage() == 1)
					// player.dropMessage(5, new StringBuilder().append("You
					// have done ").append(randomDMG).append(" extra RB damage!
					// (disable/enable this with @dmgnotice)").toString());
					// }
					// else {
					// if (player.getDamage() > 2147483647L) {
					// long randomDMG = player.getDamage();
					// monster.damage(player, monster.getMobMaxHp(), true,
					// attack.skill);
					// if (player.getshowdamage() == 1) {
					// player.dropMessage(5, new StringBuilder().append("You
					// have done ").append(randomDMG).append(" extra RB damage!
					// (disable/enable this with @dmgnotice)").toString());
					// }
					// }
					if (totDamageToOneMonster > 0) {
						Item weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
						if (weapon_ != null) {
							MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId());
							if ((stat != null) && (Randomizer.nextInt(100) < GameConstants.getStatChance())) {
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat,
										Integer.valueOf(GameConstants.getXForStat(stat)),
										GameConstants.getSkillForStat(stat), null, false);
								monster.applyStatus(player, monsterStatusEffect, false, 10000L, false, null);
							}
						}
						if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
							MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BLIND);

							if ((eff != null) && (eff.makeChanceResult())) {
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC,
										Integer.valueOf(eff.getX()), eff.getSourceId(), null, false);
								monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
							}
						}

						if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
							MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.HAMSTRING);

							if ((eff != null) && (eff.makeChanceResult())) {
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.SPEED,
										Integer.valueOf(eff.getX()), 3121007, null, false);
								monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
							}
						}
						if ((player.getJob() == 121) || (player.getJob() == 122)) {
							Skill skill = SkillFactory.getSkill(1211006);
							if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
								MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
								MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE,
										Integer.valueOf(1), skill.getId(), null, false);
								monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 2000, true, eff);
							}
						}
					}
					if ((effect != null) && (effect.getMonsterStati().size() > 0) && (effect.makeChanceResult())) {
						for (Map.Entry z : effect.getMonsterStati().entrySet()) {
							monster.applyStatus(player,
									new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(),
											theSkill.getId(), null, false),
									effect.isPoison(), effect.getDuration(), true, effect);
						}
					}
				}
			}
		}

		if (GameConstants.isLuminous(player.getJob())) {
			if (player.getBuffedValue(MapleBuffStat.DARK_CRESCENDO) != 1)
				;
			{
				MapleStatEffect crescendo = SkillFactory.getSkill(27121005).getEffect(player.getSkillLevel(27121005));
				if (crescendo != null) {

					if (crescendo.makeChanceResult()) {
						player.setLastCombo(System.currentTimeMillis());
						if (player.acaneAim <= 29) {
							player.acaneAim++;
							crescendo.applyTo(player);
						}
					}
				}
			}
		}

		if (player.getJob() >= 1500 && player.getJob() <= 1512) {
			MapleStatEffect crescendo = SkillFactory.getSkill(15001022).getEffect(player.getSkillLevel(15001022));
			if (crescendo != null) {

				if (crescendo.makeChanceResult()) {
					player.setLastCombo(System.currentTimeMillis());
					if (player.acaneAim <= 3) {
						player.acaneAim++;
						crescendo.applyTo(player);
					}
				}
			}
		}

		if (player.getJob() >= 420 && player.getJob() <= 422) {
			MapleStatEffect crescendo = SkillFactory.getSkill(4200013).getEffect(player.getSkillLevel(4200013));
			if (crescendo != null) {

				if (crescendo.makeChanceResult()) {
					player.setLastCombo(System.currentTimeMillis());
					if (player.acaneAim <= 30) {
						player.acaneAim++;
						crescendo.applyTo(player);
					}
				}
			}
		}

		if ((attack.skill == 4331003) && ((hpMob <= 0L) || (totDamageToOneMonster < hpMob))) {
			return;
		}
		if ((hpMob > 0L) && (totDamageToOneMonster > 0)) {
			player.afterAttack(attack.targets, attack.hits, attack.skill);
		}
		if ((attack.skill != 0) && ((attack.targets > 0) || ((attack.skill != 4331003) && (attack.skill != 4341002)))
				&& (!GameConstants.isNoDelaySkill(attack.skill))) {
			boolean applyTo = effect.applyTo(player, attack.position);
		}
		if (player.getSkillLevel(4100011) > 0) { // ë§ˆí�¬ ì˜¤ë¸Œ ì–´ìŒ�?ì‹`
			MapleStatEffect eff = SkillFactory.getSkill(4100011).getEffect(player.getSkillLevel(4100011));
			if (eff.makeChanceResult()) {
				for (Map.Entry z : effect.getMonsterStati().entrySet()) {
					for (AttackPair ap : attack.allDamage) {
						final MapleMonster monster = player.getMap().getMonsterByOid(ap.objectid);
						// monster.applyStatus(player, new
						// MonsterStatusEffect((MonsterStatus)z.getKey(),
						// (Integer)z.getValue(), theSkill.getId(), null,
						// false), effect.isPoison(), effect.getDuration(),
						// true, effect);
						// }

						// MonsterStatusEffect monsterStatusEffect = new
						// MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON,
						// eff.getSkillStats().getStats("dot")),
						// SkillFactory.getSkill(4100011), null, false);
						// monsterStatusEffect.setOwnerId(player.getId());
						// //cidê°€ ë§žì•„ì•¼ ë³´ì�´ë¯€ë¡œ
						// monster.applyStatus(player, new
						// MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON,
						// eff.getourceId().getStats("dot")),
						// SkillFactory.getSkill(4100011), null, false), true,
						// eff.getDuration(), false);
						// monster.applyStatus(player, new
						// MonsterStatusEffect(MonsterStatus.POISON,
						// Integer.valueOf(eff.getX()), eff.getSourceId(), null,
						// false), false, eff.getY() * 1000, true, eff);
						monster.applyStatus(player,
								new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(),
										theSkill.getId(), null, false),
								effect.isPoison(), effect.getDuration(), true, effect);
					}
				}
			}

			int bulletCount = eff.getBulletCount();
			for (AttackPair ap : attack.allDamage) {
				final MapleMonster source = player.getMap().getMonsterByOid(ap.objectid);

				final MonsterStatusEffect check = source.getBuff(MonsterStatus.POISON);

				if (check != null && check.getSkill() == 4100011 && check.getOwnerId() == player.getId()) { // :3
					final List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getPosition(), 500000,
							Arrays.asList(MapleMapObjectType.MONSTER));
					final List<MapleMonster> monsters = new ArrayList<>();
					for (int i = 0; i < bulletCount; i++) {
						int rand = Randomizer.rand(0, objs.size() - 1);
						if (objs.size() < bulletCount) {
							if (i < objs.size()) {
								monsters.add((MapleMonster) objs.get(i));
							}
						} else {
							monsters.add((MapleMonster) objs.get(rand));
							objs.remove(rand);
						}
					}
					if (monsters.size() <= 0) {
						CWvsContext.enableActions();
						return;
					}
					final List<Point> points = new ArrayList<>();
					for (MapleMonster mob : monsters) {
						points.add(mob.getPosition());
					}
					// player.dropMessage(monsters.size());
					// player.dropMessage("ì‹œìž‘" + monsters.size());
					player.getMap().broadcastMessage(CWvsContext.giveMarkOfTheif(player.getId(), source.getObjectId(),
							4100012, monsters, player.getPosition(), monsters.get(0).getPosition(), 2070005));
					// player.message("ì¢…ë£Œ");
				}
			}
		}
	}

	@SuppressWarnings("empty-statement")
	public static final void applyAttackMagic(AttackInfo attack, Skill theSkill, MapleCharacter player,
			MapleStatEffect effect, double maxDamagePerHit) {

		if ((attack.hits > 0) && (attack.targets > 0) && (!player.getStat().checkEquipDurabilitys(player, -1))) {
			player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
			return;
		}

		if (GameConstants.isMulungSkill(attack.skill)) {
			if (player.getMapId() / 10000 != 92502) {
				return;
			}
			if (player.getMulungEnergy() < 10000) {
				return;
			}
			player.mulung_EnergyModify(false);
		} else if (GameConstants.isPyramidSkill(attack.skill)) {
			if (player.getMapId() / 1000000 != 926) {
				return;
			}

			if ((player.getPyramidSubway() != null) && (player.getPyramidSubway().onSkillUse(player)))
				;
		} else if ((GameConstants.isInflationSkill(attack.skill))
				&& (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null)) {
			return;
		}

		/*
		 * if (player.getClient().getChannelServer().isAdminOnly()) {
		 * player.dropMessage(-1, new StringBuilder().append("Animation: "
		 * ).append(Integer.toHexString((attack.display & 0x8000) != 0 ?
		 * attack.display - 32768 : attack.display)).toString()); }
		 */

		PlayerStats stats = player.getStat();
		// Element element = player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null ? Element.NEUTRAL : theSkill.getElement();

		double MaxDamagePerHit = 0.0D;
		int totDamage = 0;

		int CriticalDamage = stats.passive_sharpeye_percent();
		Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
		int eaterLevel = player.getTotalSkillLevel(eaterSkill);

		MapleMap map = player.getMap();

		int antiKS = 0;
		for (AttackPair oned : attack.allDamage) {
			MapleMonster monster = map.getMonsterByOid(oned.objectid);

			if (ServerConstants.AntiKS) {
				MapleQuestStatus statt = player.getQuestNoAdd(MapleQuest.getInstance(732648172));
				boolean antiks;
				if (statt == null || statt.getCustomData() == null) {
					antiks = false;
					statt.setCustomData(antiks + ";" + 0);
				} else {
					String[] statss = statt.getCustomData().split(";");
					try {
						antiks = Boolean.parseBoolean(statss[0]);
					} catch (Exception ex) {
						antiks = false;
					}
					if (monster.getBelongsToSomeone() && monster.getBelongsTo() != player.getId()
							&& (player.getParty() == null
									|| player.getParty().getMemberById(monster.getBelongsTo()) == null)
							&& !player.isGM()) {
						monster.setBelongsTo(player);
						antiKS++;
						statt.setCustomData(antiks + ";" + (Integer.parseInt(statss[1]) + 1));
					}
				}
				if (monster.getBelongsToSomeone()) {
					if (monster.getBelongsTo() != player.getId() && !player.isGM()) {
						player.dropMessage(5, "You cannot hit this monster because it belongs to someone else.");
						continue;
					}
					if (player.isGM()) {
						player.dropMessage(5, "You are attacking a monster who was marked by another player.");
					}
				}
			}

			if ((monster != null) && (monster.getLinkCID() <= 0)) {
				boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006)
						&& (!monster.getStats().isBoss());
				int totDamageToOneMonster = 0;
				MapleMonsterStats monsterstats = monster.getStats();
				int fixeddmg = monsterstats.getFixedDamage();
				if ((!Tempest) && (!player.isGM())) {
					if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY))
							&& (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) {
						MaxDamagePerHit = CalculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats,
								null, Integer.valueOf(CriticalDamage), maxDamagePerHit, effect);
					} else {
						MaxDamagePerHit = 1.0D;
					}
				}
				byte overallAttackCount = 0;

				for (Pair eachde : oned.attack) {
					Integer eachd = (Integer) eachde.left;
					overallAttackCount = (byte) (overallAttackCount + 1);
					if (fixeddmg != -1) {
						eachd = Integer.valueOf(monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg);
					} else if (monsterstats.getOnlyNoramlAttack()) {
						eachd = Integer.valueOf(0);
					} else if (!player.isGM()) {
						if (Tempest) {
							if (eachd.intValue() > monster.getMobMaxHp()) {
								eachd = Integer.valueOf((int) Math.min(monster.getMobMaxHp(), 2147483647L));
							}
						} else if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY))
								&& (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) {
							if (eachd.intValue() > MaxDamagePerHit) {
								if (eachd.intValue() > MaxDamagePerHit * 2.0D) {
									eachd = Integer.valueOf((int) (MaxDamagePerHit * 2.0D));

									if (eachd.intValue() >= 2499999) {
										player.getClient().getSession().close();
									}
								}
							}

						} else if (eachd.intValue() > MaxDamagePerHit) {
							eachd = Integer.valueOf((int) MaxDamagePerHit);
						}

					}

					totDamageToOneMonster += eachd.intValue();
				}

				totDamage += totDamageToOneMonster;
				player.checkMonsterAggro(monster);

				if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)
						&& (!GameConstants.isNoDelaySkill(attack.skill)) && (!monster.getStats().isBoss())
						&& (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants
								.getAttackRange(effect, player.getStat().defRange))) {
				}
				if (GameConstants.isLuminous(player.getJob())) {
					player.handleLuminous(attack.skill);
				}

				monster.damage(player, totDamage, true, attack.skill);
			}
			// else if (player.getDamage() > 2147483647L) {
			// long randomDMG = player.getDamage();
			// monster.damage(player, monster.getMobMaxHp(), true,
			// attack.skill);
			// if (player.getshowdamage() == 1) {
			// player.dropMessage(5, new StringBuilder().append("You have done
			// ").append(randomDMG).append(" extra damage! (disable/enable this
			// with @dmgnotice)").toString());
			// }
			// int totDamageToOneMonster1 = 0;
			// if (totDamageToOneMonster1 > 0) {
			// monster.damage(player, totDamageToOneMonster1, true,
			// attack.skill);
			// if (monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
			// player.addHP(-(7000 + Randomizer.nextInt(8000)));
			// }
			// if (player.getBuffedValue(MapleBuffStat.SLOW) != null) {
			// MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.SLOW);
			//
			// if ((eff != null) && (eff.makeChanceResult()) &&
			// (!monster.isBuffed(MonsterStatus.SPEED))) {
			// monster.applyStatus(player, new
			// MonsterStatusEffect(MonsterStatus.SPEED,
			// Integer.valueOf(eff.getX()), eff.getSourceId(), null, false),
			// false, eff.getY() * 1000, true, eff);
			// }
			//
			// }
			//
			// player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(),
			// attack.skill, monster.getObjectId(), totDamage, 0);
			//
			// switch (attack.skill) {
			// case 2221003:
			// monster.setTempEffectiveness(Element.ICE, effect.getDuration());
			// break;
			// case 2121003:
			// monster.setTempEffectiveness(Element.FIRE, effect.getDuration());
			// }
			//
			// if ((effect != null) && (effect.getMonsterStati().size() > 0) &&
			// (effect.makeChanceResult()))
			// {
			// for (Map.Entry z : effect.getMonsterStati().entrySet()) {
			// monster.applyStatus(player, new
			// MonsterStatusEffect((MonsterStatus)z.getKey(),
			// (Integer)z.getValue(), theSkill.getId(), null, false),
			// effect.isPoison(), effect.getDuration(), true, effect);
			// }
			// }
			//
			// if (eaterLevel > 0) {
			// eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
			// }
			// }
			// }

			if (attack.skill != 2301002) {
				effect.applyTo(player);
			}
		}
	}

	private static double CalculateMaxMagicDamagePerHit(MapleCharacter chr, Skill skill, MapleMonster monster,
			MapleMonsterStats mobstats, PlayerStats stats, Element elem, Integer sharpEye, double maxDamagePerMonster,
			MapleStatEffect attackEffect) {
		int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0) * 2;
		int HitRate = Math.min(
				(int) Math.floor(Math.sqrt(stats.getAccuracy())) - (int) Math.floor(Math.sqrt(mobstats.getEva())) + 100,
				100);
		if (dLevel > HitRate) {
			HitRate = dLevel;
		}
		HitRate -= dLevel;
		if ((HitRate <= 0)
				&& ((!GameConstants.isBeginnerJob(skill.getId() / 10000)) || (skill.getId() % 10000 != 1000))) {
			return 0.0D;
		}

		int CritPercent = sharpEye.intValue();
		ElementalEffectiveness ee = monster.getEffectiveness(elem);
		double elemMaxDamagePerMob;
		switch (ee) {
		case IMMUNE:
			elemMaxDamagePerMob = 1.0D;
			break;
		default:
			elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * ee.getValue(), stats);
		}

		int MDRate = monster.getStats().getMDRate();
		MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.MDEF);
		if (pdr != null) {
			MDRate += pdr.getX().intValue();
		}
		elemMaxDamagePerMob -= elemMaxDamagePerMob
				* (Math.max(MDRate - stats.ignoreTargetDEF - attackEffect.getIgnoreMob(), 0) / 100.0D);

		elemMaxDamagePerMob += elemMaxDamagePerMob / 100.0D * CritPercent;

		elemMaxDamagePerMob *= (monster.getStats().isBoss() ? chr.getStat().bossdam_r : chr.getStat().dam_r) / 100.0D;
		MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
		if (imprint != null) {
			elemMaxDamagePerMob += elemMaxDamagePerMob * imprint.getX().intValue() / 100.0D;
		}
		elemMaxDamagePerMob += elemMaxDamagePerMob * chr.getDamageIncrease(monster.getObjectId()) / 100.0D;
		if (GameConstants.isBeginnerJob(skill.getId() / 10000)) {
			switch (skill.getId() % 10000) {
			case 1000:
				elemMaxDamagePerMob = 40.0D;
				break;
			case 1020:
				elemMaxDamagePerMob = 1.0D;
				break;
			case 1009:
				elemMaxDamagePerMob = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L
						: monster.getMobMaxHp();
			}
		}

		switch (skill.getId()) {
		case 32001000:
		case 32101000:
		case 32111002:
		case 32121002:
			elemMaxDamagePerMob *= 1.5D;
		}

		if (elemMaxDamagePerMob > 999999.0D) {
			elemMaxDamagePerMob = 999999.0D;
		} else if (elemMaxDamagePerMob <= 0.0D) {
			elemMaxDamagePerMob = 1.0D;
		}

		return elemMaxDamagePerMob;
	}

	private static double ElementalStaffAttackBonus(Element elem, double elemMaxDamagePerMob, PlayerStats stats) {
		switch (elem) {
		case FIRE:
			return elemMaxDamagePerMob / 100.0D * (stats.element_fire + stats.getElementBoost(elem));
		case ICE:
			return elemMaxDamagePerMob / 100.0D * (stats.element_ice + stats.getElementBoost(elem));
		case LIGHTING:
			return elemMaxDamagePerMob / 100.0D * (stats.element_light + stats.getElementBoost(elem));
		case POISON:
			return elemMaxDamagePerMob / 100.0D * (stats.element_psn + stats.getElementBoost(elem));
		}
		return elemMaxDamagePerMob / 100.0D * (stats.def + stats.getElementBoost(elem));
	}

	private static void handlePickPocket(MapleCharacter player, MapleMonster mob, AttackPair oned) {
		int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();

		for (Pair eachde : oned.attack) {
			Integer eachd = (Integer) eachde.left;
			if ((player.getStat().pickRate >= 100) || (Randomizer.nextInt(99) < player.getStat().pickRate)) {
				player.getMap().spawnMesoDrop(
						Math.min((int) Math.max(eachd.intValue() / 20000.0D * maxmeso, 1.0D), maxmeso),
						new Point((int) (mob.getTruePosition().getX() + Randomizer.nextInt(100) - 50.0D),
								(int) mob.getTruePosition().getY()),
						mob, player, false, (byte) 0);
			}
		}
	}

	private static double CalculateMaxWeaponDamagePerHit(MapleCharacter player, MapleMonster monster, AttackInfo attack,
			Skill theSkill, MapleStatEffect attackEffect, double maximumDamageToMonster,
			Integer CriticalDamagePercent) {
		int dLevel = Math.max(monster.getStats().getLevel() - player.getLevel(), 0) * 2;
		int HitRate = Math.min((int) Math.floor(Math.sqrt(player.getStat().getAccuracy()))
				- (int) Math.floor(Math.sqrt(monster.getStats().getEva())) + 100, 100);
		if (dLevel > HitRate) {
			HitRate = dLevel;
		}
		HitRate -= dLevel;
		if ((HitRate <= 0) && ((!GameConstants.isBeginnerJob(attack.skill / 10000)) || (attack.skill % 10000 != 1000))
				&& (!GameConstants.isPyramidSkill(attack.skill)) && (!GameConstants.isMulungSkill(attack.skill))
				&& (!GameConstants.isInflationSkill(attack.skill))) {
			return 0.0D;
		}
		if ((player.getMapId() / 1000000 == 914) || (player.getMapId() / 1000000 == 927)) {
			return 999999.0D;
		}

		List<Element> elements = new ArrayList();
		boolean defined = false;
		int CritPercent = CriticalDamagePercent.intValue();
		int PDRate = monster.getStats().getPDRate();
		MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.WDEF);
		if (pdr != null) {
			PDRate += pdr.getX().intValue();
		}
		if (theSkill != null) {
			elements.add(theSkill.getElement());
			if (GameConstants.isBeginnerJob(theSkill.getId() / 10000)) {
				switch (theSkill.getId() % 10000) {
				case 1000:
					maximumDamageToMonster = 40.0D;
					defined = true;
					break;
				case 1020:
					maximumDamageToMonster = 1.0D;
					defined = true;
					break;
				case 1009:
					maximumDamageToMonster = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L
							: monster.getMobMaxHp();
					defined = true;
				}
			}

			switch (theSkill.getId()) {
			case 1311005:
				PDRate = monster.getStats().isBoss() ? PDRate : 0;
				break;
			case 3221001:
			case 33101001:
				maximumDamageToMonster *= attackEffect.getMobCount();
				defined = true;
				break;
			case 3101005:
				defined = true;
				break;
			case 32001000:
			case 32101000:
			case 32111002:
			case 32121002:
				maximumDamageToMonster *= 1.5D;
				break;
			case 1221009:
			case 3221007:
			case 4331003:
			case 23121003:
				if (!monster.getStats().isBoss()) {
					maximumDamageToMonster = monster.getMobMaxHp();
					defined = true;
				}
				break;
			case 1221011:
			case 21120006:
				maximumDamageToMonster = monster.getStats().isBoss() ? 500000.0D : monster.getHp() - 1L;
				defined = true;
				break;
			case 3211006:
				if (monster.getStatusSourceID(MonsterStatus.FREEZE) == 3211003) {
					defined = true;
					maximumDamageToMonster = 999999.0D;
				}
				break;
			}
		}
		double elementalMaxDamagePerMonster = maximumDamageToMonster;
		if ((player.getJob() == 311) || (player.getJob() == 312) || (player.getJob() == 321)
				|| (player.getJob() == 322)) {
			Skill mortal = SkillFactory
					.getSkill((player.getJob() == 311) || (player.getJob() == 312) ? 3110001 : 3210001);
			if (player.getTotalSkillLevel(mortal) > 0) {
				MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
				if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
					elementalMaxDamagePerMonster = 999999.0D;
					defined = true;
					if (mort.getZ() > 0) {
						player.addHP(player.getStat().getMaxHp() * mort.getZ() / 100);
					}
				}
			}
		} else if ((player.getJob() == 221) || (player.getJob() == 222)) {
			Skill mortal = SkillFactory.getSkill(2210000);
			if (player.getTotalSkillLevel(mortal) > 0) {
				MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
				if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
					elementalMaxDamagePerMonster = 999999.0D;
					defined = true;
				}
			}
		}
		if ((!defined) || ((theSkill != null) && ((theSkill.getId() == 33101001) || (theSkill.getId() == 3221001)))) {
			if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
				int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);

				switch (chargeSkillId) {
				case 1211003:
				case 1211004:
					elements.add(Element.FIRE);
					break;
				case 1211005:
				case 1211006:
				case 21111005:
					elements.add(Element.ICE);
					break;
				case 1211007:
				case 1211008:
				case 15101006:
					elements.add(Element.LIGHTING);
					break;
				case 1221003:
				case 1221004:
				case 11111007:
					elements.add(Element.HOLY);
					break;
				case 12101005:
				}

			}

			if (player.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE) != null) {
				elements.add(Element.LIGHTING);
			}
			if (player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null) {
				elements.clear();
			}
			double elementalEffect;
			if (elements.size() > 0) {
				switch (attack.skill) {
				case 3111003:
				case 3211003:
					elementalEffect = attackEffect.getX() / 100.0D;
					break;
				default:
					elementalEffect = 0.5D / elements.size();
				}

				for (Element element : elements) {
					switch (monster.getEffectiveness(element)) {
					case IMMUNE:
						elementalMaxDamagePerMonster = 1.0D;
						break;
					case WEAK:
						elementalMaxDamagePerMonster *= (1.0D + elementalEffect
								+ player.getStat().getElementBoost(element));
						break;
					case STRONG:
						elementalMaxDamagePerMonster *= (1.0D - elementalEffect
								- player.getStat().getElementBoost(element));
					}

				}

			}

			elementalMaxDamagePerMonster -= elementalMaxDamagePerMonster
					* (Math.max(PDRate - Math.max(player.getStat().ignoreTargetDEF, 0)
							- Math.max(attackEffect == null ? 0 : attackEffect.getIgnoreMob(), 0), 0) / 100.0D);

			elementalMaxDamagePerMonster += elementalMaxDamagePerMonster / 100.0D * CritPercent;

			MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
			if (imprint != null) {
				elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * imprint.getX().intValue() / 100.0D;
			}

			elementalMaxDamagePerMonster += elementalMaxDamagePerMonster
					* player.getDamageIncrease(monster.getObjectId()) / 100.0D;
			elementalMaxDamagePerMonster *= ((monster.getStats().isBoss()) && (attackEffect != null)
					? player.getStat().bossdam_r + attackEffect.getBossDamage() : player.getStat().dam_r) / 100.0D;
		}
		if (elementalMaxDamagePerMonster > 999999.0D) {
			if (!defined) {
				elementalMaxDamagePerMonster = 999999.0D;
			}
		} else if (elementalMaxDamagePerMonster <= 0.0D) {
			elementalMaxDamagePerMonster = 1.0D;
		}
		return elementalMaxDamagePerMonster;
	}

	public static final AttackInfo DivideAttack(final AttackInfo attack, final int rate) {
		attack.real = false;
		if (rate <= 1) {
			return attack; // lol
		}
		for (AttackPair p : attack.allDamage) {
			if (p.attack != null) {
				for (Pair<Integer, Boolean> eachd : p.attack) {
					eachd.left /= rate; // too ex.
				}
			}
		}
		return attack;
	}

	public static final AttackInfo Modify_AttackCrit(AttackInfo attack, MapleCharacter chr, int type,
			MapleStatEffect effect) {
		int CriticalRate;
		boolean shadow;
		List damages;
		List damage;
		if ((attack.skill != 4211006) && (attack.skill != 3211003) && (attack.skill != 4111004)) {
			CriticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCr());
			shadow = (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) && ((type == 1) || (type == 2));
			damages = new ArrayList();
			damage = new ArrayList();

			for (AttackPair p : attack.allDamage) {
				if (p.attack != null) {
					int hit = 0;
					int mid_att = shadow ? p.attack.size() / 2 : p.attack.size();

					int toCrit = (attack.skill == 4221001) || (attack.skill == 3221007) || (attack.skill == 23121003)
							|| (attack.skill == 4341005) || (attack.skill == 4331006) || (attack.skill == 21120005)
									? mid_att : 0;
					if (toCrit == 0) {
						for (Pair eachd : p.attack) {
							if ((!((Boolean) eachd.right).booleanValue()) && (hit < mid_att)) {
								if ((((Integer) eachd.left).intValue() > 999999)
										|| (Randomizer.nextInt(100) < CriticalRate)) {
									toCrit++;
								}
								damage.add(eachd.left);
							}
							hit++;
						}
						if (toCrit == 0) {
							damage.clear();
						} else {
							Collections.sort(damage);
							for (int i = damage.size(); i > damage.size() - toCrit; i--) {
								damages.add(damage.get(i - 1));
							}
							damage.clear();
						}
					} else {
						hit = 0;
						for (Pair eachd : p.attack) {
							if (!((Boolean) eachd.right).booleanValue()) {
								if (attack.skill == 4221001) {
									eachd.right = Boolean.valueOf(hit == 3);
								} else if ((attack.skill == 3221007) || (attack.skill == 23121003)
										|| (attack.skill == 21120005) || (attack.skill == 4341005)
										|| (attack.skill == 4331006) || (((Integer) eachd.left).intValue() > 999999)) {
									eachd.right = Boolean.valueOf(true);
								} else if (hit >= mid_att) {
									eachd.right = ((Pair) p.attack.get(hit - mid_att)).right;
								} else {
									eachd.right = Boolean.valueOf(damages.contains(eachd.left));
								}
							}
							hit++;
						}
						damages.clear();
					}
				}
			}
		}
		return attack;
	}

	public static AttackInfo parseDmgMa(LittleEndianAccessor lea, MapleCharacter chr) // magic
	{
		try {
			AttackInfo ret = new AttackInfo();

			lea.skip(1);
			ret.tbyte = lea.readByte();

			ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
			ret.hits = ((byte) (ret.tbyte & 0xF));
			ret.skill = lea.readInt();
			if (ret.skill >= 91000000 && ret.skill < 100000000) {
				return null;
			}
			lea.skip(11);
			if (GameConstants.isMagicChargeSkill(ret.skill)) {
				ret.charge = lea.readInt();
			} else {
				ret.charge = -1;
			}
			ret.unk = lea.readByte();
			ret.display = lea.readUShort();

			lea.skip(4);
			ret.speed = lea.readByte();
			ret.lastAttackTickCount = lea.readInt();
			lea.skip(4);
			ret.allDamage = new ArrayList();

			for (int i = 0; i < ret.targets; i++) {
				int oid = lea.readInt();
				lea.skip(20);// v140 = 19
				List allDamageNumbers = new ArrayList();

				for (int j = 0; j < ret.hits; j++) {
					int damage = lea.readInt();
					allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
				}
				lea.skip(8);
				ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
			}
			if (lea.available() >= 4L) {
				ret.position = lea.readPos();
			}
			// if (ret.skill == 2321054){//Asura - Mixtamal6
			// lea.skip(3); //new
			// }
			return ret;
		} catch (Exception e) {
		}
		return null;
	}

	public static AttackInfo parseDmgM(MapleCharacter chr, LittleEndianAccessor lea)// reg att
	{
		System.out.println("[Damage] " + lea);

		AttackInfo ret = new AttackInfo();
		ret.tbyte = lea.readByte();

		ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
		ret.hits = ((byte) (ret.tbyte & 0xF));
		ret.skill = lea.readInt();
		if (GameConstants.isZero(chr.getJob()) && ret.skill != 0) {
			lea.skip(1); // zero has byte
		}
		if (ret.skill == 2221012 || ret.skill == 36101001 || ret.skill == 42120003) {
			lea.skip(1);
		}
		lea.skip(1);
		lea.readInt();
		lea.readInt(); // same as above
		lea.readShort();
		switch (ret.skill) {
			case 1311011:// La Mancha Spear
			case 2221012:
			case 4341002:
			case 4341003:
			case 4221052:
			case 5201002:
			case 5300007:
			case 5301001:
			case 11121052:// Styx Crossing
			case 11121055:// Styx Crossing charged
			case 31201001:
			case 31211001:
			case 14111006:
			case 24121000:
			case 24121005:
			case 27101202:
			case 27111100:
			case 27120211:
			case 27121201:
			case 31001000:
			case 31101000:
			case 31111005:
			case 36121000:
			case 36101001:
			case 42120003: // Monkey Spirits
			case 61111100:
			case 61111111:
			case 61111113:
			case 65121003:
			case 65121052:// Supreme Supernova
			case 101110101:
			case 101110102:
			case 101110104:
			case 101120200:
			case 101120203:
			case 101120205:
			case 32121003: // Tornado Spin
			// case 36121001:
			ret.charge = lea.readInt();
			break;
		default:
			ret.charge = 0;
		}

		ret.unk = lea.readByte();
		ret.display = lea.readUShort();
		if (ret.skill == 2221012 || ret.skill == 36101001 || ret.skill == 42120003) {
			lea.skip(4);
		} else {
			lea.skip(5);
		}
		if ((ret.skill == 5300007) || (ret.skill == 5101012) || (ret.skill == 5081001) || (ret.skill == 15101010)) {
			lea.readInt();
		}
		ret.speed = lea.readByte();
		ret.lastAttackTickCount = lea.readInt();
		if (GameConstants.isEnergyBuff(ret.skill)) {
			lea.skip(4);
		} else if (ret.skill == 4341052) {// Asura - Mixtamal6
			lea.skip(3); // new
		} else {
			lea.skip(8);
		}

		ret.allDamage = new ArrayList();

		for (int i = 0; i < ret.targets; i++) {
			int oid = lea.readInt();

			lea.skip(20);// was 19

			List allDamageNumbers = new ArrayList();

			for (int j = 0; j < ret.hits; j++) {
				int damage = lea.readInt();

				allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
			}
			lea.skip(8);
			ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
		}
		// ret.position = lea.readPos();

		return ret;
	}

	public static AttackInfo parseDmgR(LittleEndianAccessor lea, MapleCharacter chr)// ranged
																					// att
	{
		AttackInfo ret = new AttackInfo();
		lea.skip(1);
		ret.tbyte = lea.readByte();

		ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
		ret.hits = ((byte) (ret.tbyte & 0xF));
		ret.skill = lea.readInt();
		if (GameConstants.isZero(chr.getJob())) {
			lea.skip(1); // zero has byte
		}
		lea.skip(1);
		lea.readInt();
		lea.readInt(); // same as above
		lea.readShort();
		switch (ret.skill) {
		case 3121004:
		case 3221001:
		case 5321052:
		case 5221004:
		case 5311002:
		case 5711002:
		case 5721001:
		case 13111002:// Hurricane
		case 13111020:// Sentient Arrow
		case 13121001:// Song of Heaven
		case 23121000:
		case 24121000:
		case 33121009:
		case 35001001:
		case 35101009:
		case 60011216:// Soul Buster
		case 3101008:
		case 3111009:// Hurricane
		case 3121013:// Arrow Blaster
		case 5221022:
			lea.skip(4);
		}

		ret.charge = -1;
		ret.unk = lea.readByte();
		ret.display = lea.readUShort();
		lea.skip(5);
		if (ret.skill == 23111001 || ret.skill == 36111010) {
			lea.skip(12);
		} else if (ret.skill == 3121013) {// Arrow Blaster
			lea.skip(8);
		}
		if ((ret.skill == 5221022) || (ret.skill == 5220023) || (ret.skill == 95001000)) {// Corsair
																							// BoarSide
																							// IDSkills
			lea.readInt();// newv144
			lea.readInt();// newv144
		}
		ret.speed = lea.readByte();
		ret.lastAttackTickCount = lea.readInt();
		lea.skip(4);
		ret.slot = ((byte) lea.readShort());
		ret.csstar = ((byte) lea.readShort());
		ret.AOE = lea.readByte();
		ret.allDamage = new ArrayList();

		for (int i = 0; i < ret.targets; i++) {
			int oid = lea.readInt();
			lea.skip(20);// v140 = 19
			List allDamageNumbers = new ArrayList();
			for (int j = 0; j < ret.hits; j++) {
				int damage = lea.readInt();
				allDamageNumbers.add(new Pair(Integer.valueOf(damage), Boolean.valueOf(false)));
			}
			lea.skip(8);
			ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
		}
		ret.position = lea.readPos();
		if (lea.available() >= 4) {
			lea.skip(4);// moved after pos in v141
		}
		return ret;
	}

	public static AttackInfo parseExplosionAttack(LittleEndianAccessor lea, AttackInfo ret, MapleCharacter chr) {
		if (ret.hits == 0) {
			lea.skip(4);
			byte bullets = lea.readByte();
			for (int j = 0; j < bullets; j++) {
				ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()).intValue(), null));
				lea.skip(1);
			}
			lea.skip(2);
			return ret;
		}

		for (int i = 0; i < ret.targets; i++) {
			int oid = lea.readInt();

			lea.skip(12);
			byte bullets = lea.readByte();
			List allDamageNumbers = new ArrayList();
			for (int j = 0; j < bullets; j++) {
				allDamageNumbers.add(new Pair(Integer.valueOf(lea.readInt()), Boolean.valueOf(false)));
			}
			ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
			lea.skip(4);
		}
		lea.skip(4);
		byte bullets = lea.readByte();

		for (int j = 0; j < bullets; j++) {
			ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()).intValue(), null));
			lea.skip(2);
		}

		return ret;
	}
}

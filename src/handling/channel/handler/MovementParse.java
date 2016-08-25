/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package handling.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import server.maps.AnimatedMapleMapObject;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.StaticLifeMovement;
import tools.data.LittleEndianAccessor;

public class MovementParse {

    //1 = player, 2 = mob, 3 = pet, 4 = summon, 5 = dragon
    public static List<LifeMovementFragment> parseMovement(final LittleEndianAccessor lea, final int kind) {
        return parseMovement(lea, kind, null);
    }

    public static List<LifeMovementFragment> parseMovement(final LittleEndianAccessor lea, final int kind, MapleCharacter chr) {
        final List<LifeMovementFragment> res = new ArrayList<>();
        final byte numCommands = lea.readByte();

        for (byte i = 0; i < numCommands; i++) {
            final byte command = lea.readByte();
            switch (command) {
                case 0:
                case 8:
                case 15:
                case 17:
                case 58:
                case 59:
                case 60: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    short fh = 0;
                    short xoffset = 0;
                    short yoffset = 0;
                    if (command != 58) {
                        xoffset = lea.readShort();
                        yoffset = lea.readShort();
                    }
                    if (command == 15) {
                    	fh = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();

                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    mov.setUnk(unk);
                    mov.setFh(fh);
                    mov.setPixelsPerSecond(new Point(xwobble, ywobble));
                    mov.setOffset(new Point(xoffset, yoffset));

                    res.add(mov);
                    break;
                }
                case 1:
                case 2:
                case 16:
                case 19:
                case 20:
                case 22:
                case 54:
                case 55:
                case 56:
                case 57: {
                    final short xmod = lea.readShort();
                    final short ymod = lea.readShort();
                    short unk = 0;
                    if (command == 19 || command == 20) {
                        unk = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xmod, ymod), duration, newstate);
                    mov.setUnk(unk);
                    res.add(mov);
                    break;
                }
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 52: {
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(0, 0), duration, newstate);
                    res.add(mov);
                    break;
                }
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 9:
                case 10:
                case 11:
                case 13:
                case 24:
                case 25:
                case 49:
                case 50:
                case 51:
                case 53: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    mov.setFh(fh);

                    res.add(mov);
                    break;
                }
                case 14: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    mov.setUnk(unk);

                    res.add(mov);
                    break;
                }
                case 21: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    mov.setPixelsPerSecond(new Point(xwobble, ywobble));

                    res.add(mov);
                    break;
                }
                case 12: {
                    final byte newstate = 0;
                    final short duration = 0;
                    final int wui = lea.readByte();
                    lea.readByte();
                    
                    final StaticLifeMovement mov = new StaticLifeMovement(command, new Point(0, 0), duration, newstate);
                    mov.setWui(wui);
                    res.add(mov);
                    break;
                }
                default:
                   // if (chr.isGM()) {
                        //chr.showInfo("Movement", false, "Failed to read movement type " + command);
                    //}
//                    System.out.println("Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    //FileoutputUtil.log(FileoutputUtil.Movement_Log, "Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true) + "\r\n");
                    //return null;
            }
        }
        if (numCommands != res.size()) {
            return null; // Probably hack
        }
        return res;
    }

    public static void updatePosition(final List<LifeMovementFragment> movement, final AnimatedMapleMapObject target, final int yoffset) {
        if (movement == null) {
            return;
        }
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof StaticLifeMovement) {
                    final Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}

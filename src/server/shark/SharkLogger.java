/*
 * This file was designed for Titanium.
 * Do not redistribute without explicit permission from the
 * developer(s).
 */
package server.shark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import constants.ServerConstants;
import tools.data.MaplePacketLittleEndianWriter;

public class SharkLogger {

    private List<SharkPacket> stored = new ArrayList<>();
    private static int sessionID = 1337;
    private final static int SEVENBITS = 0x0000007f;
    private final static int SIGNBIT = 0x00000080;

    public SharkLogger() {
    }

    /**
     * Writes a 7-bit integer. As if I actually know what that is. Credits to
     * Arnold Lamkamp
     *
     * @ CWI Eclipse
     * @param value
     * @param mplew
     */
    private void write7BitInt(int value, MaplePacketLittleEndianWriter mplew) {
        int intValue = value;

        if ((intValue & 0xffffff80) == 0) {
            mplew.write((byte) (intValue & SEVENBITS));
            return;
        }
        mplew.write((byte) ((intValue & SEVENBITS) | SIGNBIT));

        if ((intValue & 0xffffc000) == 0) {
            mplew.write((byte) ((intValue >>> 7) & SEVENBITS));
            return;
        }
        mplew.write((byte) (((intValue >>> 7) & SEVENBITS) | SIGNBIT));

        if ((intValue & 0xffe00000) == 0) {
            mplew.write((byte) ((intValue >>> 14) & SEVENBITS));
            return;
        }
        mplew.write((byte) (((intValue >>> 14) & SEVENBITS) | SIGNBIT));

        if ((intValue & 0xf0000000) == 0) {
            mplew.write((byte) ((intValue >>> 21) & SEVENBITS));
            return;
        }
        mplew.write((byte) (((intValue >>> 21) & SEVENBITS) | SIGNBIT));

        mplew.write((byte) ((intValue >>> 28) & SEVENBITS));
    }

    public void dump() {
        if (!ServerConstants.LOG_SHARK) {
            return;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(); // because fuck you
        String localend = "127.0.0.1";
        String remoteend = "8.31.99.140";
        mplew.writeShort(0x2015);
        write7BitInt(localend.length(), mplew);
        mplew.writeAsciiString(localend); // mLocalEndpoint
        mplew.writeShort(7575); // mLocalPort
        write7BitInt(remoteend.length(), mplew);
        mplew.writeAsciiString(remoteend); // mRemoteEndpoint
        mplew.writeShort(6969); // mRemotePort
        mplew.write(8); // mLocale
        mplew.writeShort(140); // mBuild

        for (SharkPacket b : stored) {
            b.dump(mplew);
        }

        File toWrite = new File("./MShark" + sessionID++ + ".msb");
        try {
            toWrite.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(toWrite)) {
                fos.write(mplew.getPacket());
                fos.flush();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        stored = null;
    }

    public void log(SharkPacket sp) {
        this.stored.add(sp);
    }
}

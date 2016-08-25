/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import constants.GameConstants;
import tools.HexTool;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author: Maxcloud
 *
 */
public class CharacterInformation {
	
	private static int jobid = 0;

    public static void main(String[] args) {
        System.out.println("LOADING :: Please wait...");
        // String out = args[0];
        // File dir = new File(out);
        File text = new File("Done.txt");
        // dir.mkdir();

        BufferedReader b = null;
        try {
            String s;

            text.createNewFile();
            b = new BufferedReader(new FileReader("character.txt"));

            StringBuilder sb = new StringBuilder();
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(text))) {
                while ((s = b.readLine()) != null) {
                    byte[] bArray = HexTool.getByteArrayFromHexString(s);
                    SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(bArray));
                    
                    addCharacterInfo(slea);
                    
                    System.out.printf("There are %s bytes remaining.%n", slea.available());
                    
                    System.out.println(slea.toString());
                    // sb.append(slea.toString());
                    // writer.println(sb.toString());
                    sb.delete(0, sb.length());
                }
                writer.flush();
            }
            System.out.println("Success! The task has been completed.");
        } catch (IOException e) {
        } finally {
            try {
                if (b != null) {
                    b.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    
    private static final void addCharacterInfo(SeekableLittleEndianAccessor slea) {
    	short shrt;
    	
    	// mask
        slea.skip(8);
        
        // combat orders
        slea.skip(1);
        
        // pet active skill cool time
        slea.skip(12);
        
        slea.skip(1);
        
        slea.skip(1);
        
        slea.skip(4);
        
        slea.skip(1);
        
        addCharStats(slea);
        
        /// buddy capacity
        slea.skip(1);
        
        // blessing of the fairy
        slea.skip(1);
        
        // blessing of the empress origin
        slea.skip(1);
        
        // ultimate explorer
        slea.skip(1);
        
        // meso amount
        slea.skip(8);
        
        addInventoryInfo(slea);
        
        
        // addSkillInfo
        slea.skip(1);
        
        shrt = slea.readShort();
        for(int i = 0; i < shrt ; i++) {
        	slea.skip(4);
        	slea.skip(4);
        	slea.skip(8);
        }
        slea.skip(2);
        
        
        
        // AddCoolDownInfo
        shrt = slea.readShort();
        for(int i = 0; i < shrt ; i++) {
        	slea.skip(4);
        	slea.skip(4);
        }
        
        // AddStartedQuestInfo
        slea.skip(1);
        shrt = slea.readShort();
        for(int i = 0; i < shrt; i++) {
        	slea.skip(4);
        	slea.readMapleAsciiString();
        }

        shrt = slea.readShort();
        for(int i = 0; i < shrt; i++) {
        	slea.readMapleAsciiString();
        	slea.readMapleAsciiString();
        }
        
        // AddCompletedQuestInfo
        slea.skip(1);
        shrt = slea.readShort();
        for(int i = 0; i < shrt; i++) {
        	slea.skip(4);
        	slea.skip(4);
        }
        
        // 0x400
        slea.skip(2);
        
        // AddRingInfo
        slea.skip(2);
        slea.skip(2);
        slea.skip(2);
        
        // AddRocksInfo
        for(int i = 0; i < 41; i++) {
        	slea.skip(4);
        }
        
        // 0x20000
        slea.skip(4);
        
        // addMonsterBookInfo
        slea.skip(1);
        slea.skip(2);
        slea.skip(4);

        slea.skip(2);
        
        slea.skip(4);
        
        // 0x80000
        slea.readShort();
    	
        // 0x40000 (QuestInfoPacket)
        shrt = slea.readShort();
        for(int i = 0; i < shrt; i++) {
        	slea.readInt();
        	slea.readMapleAsciiString();
        }
        
        // 0x2000
        slea.skip(2);
        
        // 0x1000
        slea.skip(4);
        
        if ((jobid >= 3300) && (jobid <= 3312)) {
        	slea.skip(1);
            for(int i = 0; i < 5; i++) {
            	slea.skip(4);
            }
        }
        
        // 0x800
        if (GameConstants.isZero(jobid)) {
        	// TODO: Add Zero Information
        }
        
        // 0x4000000
        slea.skip(2);
        
        // 0x10000000
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        // 0x80000000 (AddAbilityInfo)
        slea.skip(2);
        
        // 0x10000
        slea.skip(2);
        
        // ?
        slea.skip(4);
        
        // ?
        slea.skip(1);
        
        // 0x1
        slea.skip(4);
        slea.skip(4);
        
        // 0x2
        slea.skip(1);
        slea.skip(2);
        
        // 0x4
        slea.skip(1);
        
        // 0x08
        if (GameConstants.isAngelicBuster(jobid)) {
        	// TODO: Angelic Buster
        } else {
        	slea.skip(4);
        	slea.skip(4);
        	slea.skip(4);
        	slea.skip(1);
        	slea.skip(4);
        	slea.skip(4);
        	slea.skip(4);
        }
        
        // 0x40000
        slea.skip(4);
        slea.skip(4);
        slea.skip(8);
        slea.readMapleAsciiString();
        slea.skip(4);
        
        // 0x10
        slea.skip(2);
        slea.skip(2);
        
        // 0x20
        slea.skip(4);
        
        // 0x40
        slea.readMapleAsciiString();
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(1);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        
        // 0x80
        slea.skip(1);
        
        // 0x400
        slea.skip(4);
        slea.skip(8);
        slea.skip(4);
        
        // 0x20000
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(8);
        slea.skip(4);
        
        // ..
        slea.skip(2);
        slea.skip(2);
        
        // ..
        slea.skip(1);
        
        // DecodeTextEquipInfo
        slea.skip(4);
        
        // 0x8000000
        slea.skip(1);
        slea.skip(1);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(8);
        slea.skip(2);
        slea.skip(2);
        
        // 0x10000000
        slea.skip(1);
        
        // 0x20000000
        slea.skip(4);
        slea.skip(4);
        
        // 0x2000
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        
        slea.skip(8);
        slea.skip(1);
        
        slea.skip(1);
        
        // 0x100000
        slea.skip(2);
        
        // red leaf information
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(4);
        slea.skip(32);
        System.out.println(slea.toString());
    }
    
    private static final void addCharStats(SeekableLittleEndianAccessor slea) {
    	
    	// character id
    	slea.skip(4);
    	
    	// dwCharacterIDForLog
    	slea.skip(4);
    	
    	// dwWorldIDForLog
    	slea.skip(4);
    	
    	// character name
    	slea.skip(13);
    	
    	// gender
    	slea.skip(1);
    	
    	// skin color
    	slea.skip(1);
    	
    	// face
    	slea.skip(4);
    	
    	// hair
    	slea.skip(4);
    	
    	// mix hair base, etc
    	slea.skip(1);
    	slea.skip(1);
    	slea.skip(1);
    	
    	// level
    	slea.skip(1);
    	
    	// job
    	jobid = slea.readShort();
    	
    	// connect data
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	
    	// remaining sp
    	slea.skip(2);
    	if(GameConstants.isSeparatedSp(jobid)) {
    		slea.skip(1);
    	} else {
    		// slea.skip(2);
    		// System.err.println("Does this job have seperated skill points?");
    	}
    	
    	// exp
    	slea.skip(8);
    	
    	// fame
    	slea.skip(4);
    	
    	// waru points
    	slea.skip(4);
    	
    	// gach exp
    	slea.skip(4);
    	
    	// map id
    	slea.skip(4);
    	
    	// spawn point
    	slea.skip(1);
    	
    	// ?
    	slea.skip(4);
    	
    	// sub category
    	slea.skip(2);
    	
    	// face marking
    	if (GameConstants.isDemonSlayer(jobid) || GameConstants.isXenon(jobid) || GameConstants.isDemonAvenger(jobid)) {
    		slea.skip(4);
    	}
    	
    	// fatigue
    	slea.skip(1);
    	
    	// current date
    	slea.skip(4);
    	
    	// trait information
    	slea.skip(24);
    	
    	// ?
    	slea.skip(21);
    	
    	// pvp exp
    	slea.skip(4);
    	
    	// pvp rak
    	slea.skip(1);
    	
    	// battle points
    	slea.skip(4);
    	
    	// pvp mode level
    	slea.skip(1);
    	
    	// pvp mode type
    	slea.skip(1);
    	
    	// event points
    	slea.skip(4);
    	
    	// add part time job information
    	slea.skip(1);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(1);
    	
    	// character card
    	for (int i = 0; i < 9; i++) {
    		slea.skip(4);
    		slea.skip(1);
    		slea.skip(4);
    	}
    	
    	// reversed long
    	slea.skip(8);
    	
    	// character burning
    	slea.skip(1);
    }
    
    private static final void addInventoryInfo(SeekableLittleEndianAccessor slea) {
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	slea.skip(4);
    	
    	slea.skip(4);
    	
    	slea.skip(1);
    	slea.skip(1);
    	slea.skip(1);
    	
    	// inventory slots
    	slea.skip(1);
    	slea.skip(1);
    	slea.skip(1);
    	slea.skip(1);
    	slea.skip(1);
    	
    	slea.skip(8);
    	
    	slea.skip(1);
    	
    	// monster book
    	slea.skip(125);
    	
    	
    	slea.skip(2);
    	
    	slea.skip(2);
    	
    	slea.skip(2);
    	
    	slea.skip(2);
    	
    	slea.skip(2);
    	slea.skip(2);
    	
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	slea.skip(2);
    	
    	slea.skip(1);
    	
    	slea.skip(1);
    	
    	slea.skip(1);
    	
    	slea.skip(1);
    	
    	slea.skip(21);
    }
}
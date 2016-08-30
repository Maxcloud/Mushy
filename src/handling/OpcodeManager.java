package handling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import client.MapleClient;
import handling.handlers.*;
import handling.handlers.login.*;
import tools.HexTool;
import tools.data.LittleEndianAccessor;


class OpcodeManager {
	
	private static Map<Short, Method> handlers = new HashMap<Short, Method>();
	
	private static Class<?>[] packethandlers = new Class<?>[] {
		
		PongHandler.class,
		
		// Login
		ServerStatusRequest.class,
		ServerListRequest.class,
		AuthServerHandler.class,
		PlayerLoggedInHandler.class,
		CheckCharacterName.class,
		DeleteCharHandler.class,
		HeartbeatRequest.class,
		ClientStartHandler.class,
		LoginPasswordHandler.class,
		CharacterListRequest.class,
		CreateNewCharacter.class,
		ClientErrorHandler.class,
		CharacterWithSecondPassword.class,
		CreateWithoutSecondPassword.class,

		CharacterWithoutSecondPassword.class,
		CharSelectHandler.class,
		
		// Channel
		ChangeMapHandler.class,
		EnterCashShopHandler.class,
		MovePlayerHandler.class,
		CloseRangeAttackHandler.class,
		RangedAttackHandler.class,
		MagicAttackHandler.class,
		GeneralChatHandler.class,
		
		MesoDropHandler.class,
		QuestActionHandler.class,
		
		MoveLifeHandler.class,
		NpcTalkHandler.class,
		NpcTalkMoreHandler.class

    };
    
	static {
		try {
			for (Class<?> c : packethandlers) {
		        for (Method method : c.getMethods()) {
		            PacketHandler annotation = method.getAnnotation(PacketHandler.class);
		            if (annotation != null) {
		                if (isValidMethod(method)) {
		                    if (handlers.containsKey(annotation.opcode())) {
		                        System.out.println("Duplicate handler for opcode: " + annotation.opcode());
		                    } else {
		                        handlers.put(annotation.opcode().getValue(), method);
		                    }
		                } else {
		                    System.out.println("Failed to add handler with method name of: " + method.getName() + " in " + c.getName());
		                }
		            }
		        }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("A total of %s handlers have been loaded.\r\n", handlers.size());
	}
    
    private static boolean isValidMethod(Method method) {
		Class[] types = method.getParameterTypes();
        
        return types.length == 2 && types[0].equals(MapleClient.class) && types[1].equals(LittleEndianAccessor.class);
    }

    public static void handle(MapleClient client, short opcode, LittleEndianAccessor lea) {
        Method method = handlers.get(opcode);
        try {
            if (method != null) { 
                method.invoke(null, client, lea);
            } else {
            	System.out.println("[Unhandled] [Recv] (" + HexTool.getOpcodeToString(opcode) + ") " + lea);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}

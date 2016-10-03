package handling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import client.MapleClient;
import handling.handlers.*;
import handling.handlers.cashshop.*;
import handling.handlers.login.*;
import handling.handlers.npc.*;
import tools.HexTool;
import tools.data.LittleEndianAccessor;


public class OpcodeManager {
	
	private static Map<Integer, Method> handlers = new HashMap<Integer, Method>();
	
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
		CancelChairHandler.class,
		UseChairHandler.class,
		ChangeChannelHandler.class,
		ChangeFmMapHandler.class,

		CloseRangeAttackHandler.class,
		RangedAttackHandler.class,
		PassiveEnergyAttackHandler.class,
		MagicAttackHandler.class,
		TakeDamageHandler.class,
		GeneralChatHandler.class,

		DistributeApHandler.class,
		DistributeSpHandler.class,

		ItemMoveHandler.class,
		UseItemHandler.class,
		UsePotentialScrollHandler.class,
		UseMagnifyGlassHandler.class,
		UseCashItemHandler.class,
		UseBonusPotentialScrollHandler.class,
		BlackCubeResultHandler.class,
		UseEnhancementHandler.class,

		MesoDropHandler.class,
		QuestActionHandler.class,
		
		MoveLifeHandler.class,
		NpcActionHandler.class,
		ItemPickupHandler.class,
		NpcTalkHandler.class,
		NpcTalkMoreHandler.class,
		NpcShopHandler.class,
		
		ChangeMapSpecialHandler.class,
		UseInnerPortalHandler.class,
		ChangeKeymapHandler.class

    };
    
	public static void load() {
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

    public static boolean handle(MapleClient client, int opcode, LittleEndianAccessor lea) {
        Method method = handlers.get(opcode);
        try {
            if (method != null) { 
                method.invoke(null, client, lea);
                return true;
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
        return false;
    }

}

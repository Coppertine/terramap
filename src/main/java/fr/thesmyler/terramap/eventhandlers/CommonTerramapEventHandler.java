package fr.thesmyler.terramap.eventhandlers;

import java.io.File;

import fr.thesmyler.terramap.TerramapMod;
import fr.thesmyler.terramap.TerramapUtils;
import fr.thesmyler.terramap.config.TerramapConfig;
import fr.thesmyler.terramap.config.TerramapServerPreferences;
import fr.thesmyler.terramap.network.S2CTerramapHelloPacket;
import fr.thesmyler.terramap.network.S2CTpCommandSyncPacket;
import fr.thesmyler.terramap.network.TerramapNetworkManager;
import fr.thesmyler.terramap.network.mapsync.RemoteSynchronizer;
import io.github.terra121.EarthGeneratorSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CommonTerramapEventHandler {

	private long tickCounter = 0;

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event){
		//Send world data to the client
		EntityPlayerMP player = (EntityPlayerMP)event.player;
		World world = player.getEntityWorld();
		if(!TerramapUtils.isEarthWorld(world)) return;
		EarthGeneratorSettings settings = S2CTerramapHelloPacket.getEarthGeneratorSettingsFromWorld(world);
		if(settings == null) return;
		IMessage data = new S2CTerramapHelloPacket(TerramapMod.getVersion(), settings, TerramapConfig.synchronizePlayers, TerramapConfig.syncSpectators, false);
		TerramapNetworkManager.CHANNEL.sendTo(data, player);
		if(TerramapConfig.forceClientTpCmd) TerramapNetworkManager.CHANNEL.sendTo(new S2CTpCommandSyncPacket(TerramapConfig.tpllcmd), player);
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		RemoteSynchronizer.playersToUpdate.remove(event.player.getPersistentID());
	}


	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.phase.equals(TickEvent.Phase.END)) return;
		World world = event.world.getMinecraftServer().worlds[0]; //event.world has no entity or players
		if(TerramapConfig.synchronizePlayers && TerramapUtils.isEarthWorld(world) && this.tickCounter == 0) {
			RemoteSynchronizer.syncPlayers(world);
		}
		this.tickCounter = (this.tickCounter+1) % TerramapConfig.syncInterval;
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		TerramapServerPreferences.save();
	}
	
	@SubscribeEvent
	public void onWorldLoads(WorldEvent.Load event) {
		if(!event.getWorld().isRemote) {
			WorldServer world = ((WorldServer)event.getWorld());
	    	File serverPrefs = new File(world.getSaveHandler().getWorldDirectory().getAbsoluteFile() + "/" + TerramapServerPreferences.FILENAME);
	    	TerramapServerPreferences.setFile(serverPrefs);
	    	TerramapServerPreferences.load();
		}
	}

}

package yesman.epicfight.api.animation;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.animation.types.datapack.ClipHoldingAnimation;
import yesman.epicfight.api.client.animation.ClientAnimationDataReader;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.api.utils.InstantiateInvoker;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPCheckAnimationRegistrySync;
import yesman.epicfight.network.server.SPDatapackSync;

public class AnimationManager extends SimpleJsonResourceReloadListener {
	private static final AnimationManager INSTANCE = new AnimationManager();
	private static ResourceManager serverResourceManager = null;
	
	public static AnimationManager getInstance() {
		return INSTANCE;
	}
	
	private final Map<StaticAnimation, AnimationClip> animationClips = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> animationRegistry = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> userAnimations = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> swappedOriginalAnimations = Maps.newHashMap();
	
	private final Map<ResourceLocation, String> userAnimationInvocationCommands = Maps.newHashMap();
	private final Map<Integer, StaticAnimation> animationIdMap = Maps.newHashMap();
	private String currentWorkingModid;
	
	public AnimationManager() {
		super(new GsonBuilder().create(), "animmodels/animations");
	}
	
	public StaticAnimation byId(int animationId) {
		if (!this.animationIdMap.containsKey(animationId)) {
			throw new NoSuchElementException("No animation id " + animationId);
		}
		
		return this.animationIdMap.get(animationId);
	}
	
	public StaticAnimation byKeyOrThrow(String resourceLocation) {
		return this.byKeyOrThrow(new ResourceLocation(resourceLocation));
	}
	
	public StaticAnimation byKeyOrThrow(ResourceLocation rl) {
		if (!this.animationRegistry.containsKey(rl)) {
			throw new NoSuchElementException("No animation with registry name " + rl);
		}
		
		return this.byKey(rl);
	}
	
	public StaticAnimation byKey(ResourceLocation rl) {
		return this.animationRegistry.get(rl);
	}
	
	public AnimationClip getStaticAnimationClip(StaticAnimation animation) {
		if (!this.animationClips.containsKey(animation)) {
			animation.loadAnimation(getAnimationResourceManager());
		}
		
		return this.animationClips.get(animation);
	}
	
	public Map<ResourceLocation, StaticAnimation> getAnimations(Predicate<StaticAnimation> filter) {
		Map<ResourceLocation, StaticAnimation> filteredItems = this.animationRegistry.entrySet().stream().filter((entry) -> !this.userAnimations.containsKey(entry.getKey()) && filter.test(entry.getValue())).reduce(Maps.newHashMap(), (map, entry) -> {
			map.put(entry.getKey(), entry.getValue());
			return map;
		}, (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		});
		
		return ImmutableMap.copyOf(filteredItems);
	}
	
	public int registerAnimation(StaticAnimation staticAnimation) {
		if (this.currentWorkingModid != null) {
			if (this.animationRegistry.containsKey(staticAnimation.getRegistryName())) {
				EpicFightMod.LOGGER.error("Animation registration failed.");
				new IllegalStateException("[EpicFightMod] Animation with registry name " + staticAnimation.getRegistryName() + " already exists!").printStackTrace();
				return -1;
			}
			
			this.animationRegistry.put(staticAnimation.getRegistryName(), staticAnimation);
			int id = this.animationRegistry.size();
			this.animationIdMap.put(id, staticAnimation);
			
			return id;
		}
		
		return -1;
	}
	
	/**
	 * Registers animations created by datapack edit screen
	 */
	public void registerUserAnimation(ClipHoldingAnimation animation) {
		ResourceLocation rl = animation.getCreator().getRegistryName();
		
		if (this.animationRegistry.containsKey(rl)) {
			this.swappedOriginalAnimations.put(rl, this.animationRegistry.get(rl));
		}
		
		this.animationRegistry.put(animation.getCreator().getRegistryName(), animation.cast());
	}
	
	/**
	 * Remove user animations created by datapack edit screen
	 */
	public void removeUserAnimation(ClipHoldingAnimation animation) {
		ResourceLocation rl = animation.getCreator().getRegistryName();
		this.animationRegistry.remove(rl);
		
		if (this.swappedOriginalAnimations.containsKey(rl)) {
			this.animationRegistry.put(rl, this.swappedOriginalAnimations.get(rl));
			this.swappedOriginalAnimations.remove(rl);
		}
	}
	
	public StaticAnimation refreshAnimation(StaticAnimation staticAnimation) {
		if (!this.animationRegistry.containsKey(staticAnimation.getRegistryName())) {
			throw new IllegalStateException("Animation refresh exception: No animation named " + staticAnimation.getRegistryName());
		}
		
		return this.animationRegistry.get(staticAnimation.getRegistryName());
	}
	
	public void loadAnimationClip(StaticAnimation animation, Function<StaticAnimation, AnimationClip> clipProvider) {
		if (!this.animationClips.containsKey(animation)) {
			AnimationClip animationClip = clipProvider.apply(animation);
			this.animationClips.put(animation, animationClip);
		}
	}
	
	public void onFailed(StaticAnimation animation) {
		if (!this.animationClips.containsKey(animation)) {
			this.animationClips.put(animation, AnimationClip.EMPTY_CLIP);
		}
	}
	
	public String workingModId() {
		return this.currentWorkingModid;
	}
	
	public static void readAnimationProperties(StaticAnimation animation) {
		ResourceLocation dataLocation = getAnimationDataFileLocation(animation.getLocation());
		
		getAnimationResourceManager().getResource(dataLocation).ifPresent((rs) -> {
			ClientAnimationDataReader.readAndApply(animation, rs);
		});
	}
	
	@Override
	protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerIn) {
		if (!EpicFightMod.isPhysicalClient() && serverResourceManager == null) {
			serverResourceManager = resourceManager;
		}
		
		Armatures.build(resourceManager);
		
		this.animationIdMap.clear();
		this.animationRegistry.clear();
		this.userAnimations.clear();
		this.userAnimationInvocationCommands.clear();
		
		Map<String, Runnable> registryMap = Maps.newLinkedHashMap();
		ModLoader.get().postEvent(new AnimationRegistryEvent(registryMap));
		this.animationClips.clear();
		
		registryMap.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEach((entry) -> {
			EpicFightMod.LOGGER.info("Register animations from " + entry.getKey());
			this.currentWorkingModid = entry.getKey();
			entry.getValue().run();
			
			this.currentWorkingModid = null;
		});
		
		return super.prepare(resourceManager, profilerIn);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profilerIn) {
		final Map<ResourceLocation, StaticAnimation> registeredAnimation = Maps.newHashMap();
		this.animationRegistry.values().forEach(a1 -> a1.getClipHolders().forEach((a2) -> registeredAnimation.put(a2.getRegistryName(), a2)));
		
		/**
		 * Load animations that are not registered from {@link AnimationRegistryEvent}
		 * Reads from Resource Pack in physical client, Datapack in physical server.
		 */
		objectIn.entrySet().stream().filter((entry) -> !registeredAnimation.containsKey(entry.getKey()) && !entry.getKey().getPath().contains("/data/"))
									.sorted((e1, e2) -> e1.getKey().toString().compareTo(e2.getKey().toString()))
									.forEach((entry) -> {
										if (!entry.getKey().getNamespace().equals(this.currentWorkingModid)) {
											this.currentWorkingModid = entry.getKey().getNamespace();
										}
										
										try {
											this.readAnimationFromJson(entry.getKey(), entry.getValue().getAsJsonObject());
										} catch (Exception e) {
											EpicFightMod.LOGGER.error("Failed to load User animation " + entry.getKey() + " because of " + e + ". Skipped.");
											e.printStackTrace();
										}
									});
		
		SkillManager.reloadAllSkillsAnimations();
		
		this.animationRegistry.values().stream().reduce(Lists.<StaticAnimation>newArrayList(), (list, anim) -> {
			list.addAll(anim.getClipHolders());
			return list;
		}, (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		}).forEach((animation) -> {
			animation.postInit();
			
			if (EpicFightMod.isPhysicalClient()) {
				AnimationManager.readAnimationProperties(animation);
			}
		});
	}
	
	public static ResourceLocation getAnimationDataFileLocation(ResourceLocation location) {
		int splitIdx = location.getPath().lastIndexOf('/');
		
		if (splitIdx < 0) {
			splitIdx = 0;
		}
		
		return new ResourceLocation(location.getNamespace(), String.format("%s/data%s", location.getPath().substring(0, splitIdx), location.getPath().substring(splitIdx)));
	}
	
	public static void setServerResourceManager(ResourceManager pResourceManager) {
		serverResourceManager = pResourceManager;
	}
	
	public static ResourceManager getAnimationResourceManager() {
		return EpicFightMod.isPhysicalClient() ? Minecraft.getInstance().getResourceManager() : serverResourceManager;
	}
	
	public int getUserAnimationsCount() {
		return this.userAnimations.size();
	}
	
	public Stream<CompoundTag> getUserAnimationStream() {
		return this.userAnimations.values().stream().sorted((a1, a2) -> a1.getRegistryName().toString().compareTo(a2.getRegistryName().toString())).map((animation) -> {
			CompoundTag compTag = new CompoundTag();
			
			compTag.putString("registry_name", animation.getRegistryName().toString());
			compTag.putString("invoke_command", this.userAnimationInvocationCommands.get(animation.getRegistryName()));
			
			return compTag;
		});
	}
	
	/**
	 * @param createDummyAnimations : creates dummy animations for server side animations without animation clips when the server has mandatory resource pack.
	 *                                custom weapon types & mob capabilities won't be created because they won't be able to find the animations from the server
	 *                                dummy animations will be automatically removed right after reloading resourced as the server forces using resource pack
	 */
	@OnlyIn(Dist.CLIENT)
	public void processServerPacket(SPDatapackSync packet, boolean createDummyAnimations) {
		if (createDummyAnimations) {
			for (CompoundTag tag : packet.getTags()) {
				String invocationCommand = tag.getString("invoke_command");
				ResourceLocation registryName = new ResourceLocation(tag.getString("registry_name"));
				
				if (this.animationRegistry.containsKey(registryName)) {
					continue;
				}
				
				try {
					this.currentWorkingModid = registryName.getNamespace();
					StaticAnimation animation = InstantiateInvoker.invoke(invocationCommand, StaticAnimation.class).getResult();
					
					this.userAnimations.put(registryName, animation);
					this.currentWorkingModid = null;
				} catch (Exception e) {
					EpicFightMod.LOGGER.warn("Failed at creating animation from server resource pack");
					e.printStackTrace();
				}
			}
		}
		
		this.sendAnimationRegistrySyncCheck();
	}
	
	@OnlyIn(Dist.CLIENT)
	private void sendAnimationRegistrySyncCheck() {
		int animationCount = this.animationRegistry.size();
		String[] registryNames = new String[animationCount];
		
		for (int i = 0; i < animationCount; i++) {
			String registryName = this.animationIdMap.get(i + 1).getRegistryName().toString();
			registryNames[i] = registryName;
		}
		
		CPCheckAnimationRegistrySync packet = new CPCheckAnimationRegistrySync(animationCount, registryNames);
		EpicFightNetworkManager.sendToServer(packet);
	}
	
	public void validateClientAnimationRegistry(CPCheckAnimationRegistrySync msg, ServerGamePacketListenerImpl connection) {
		StringBuilder builder = new StringBuilder();
		Set<String> clientAnimationRegistry = new HashSet<> (Set.of(msg.registryNames));
		
		for (String registryName : this.animationRegistry.keySet().stream().map((rl) -> rl.toString()).toList()) {
			if (!clientAnimationRegistry.contains(registryName)) {
				// Animations that don't exist in client
				builder.append(registryName);
				builder.append("\n");
			} else {
				clientAnimationRegistry.remove(registryName);
			}
		}
		
		// Animations that don't exist in server
		for (String registryName : clientAnimationRegistry) {
			if (registryName.equals("empty")) {
				continue;
			}
			
			builder.append(registryName);
			builder.append("\n");
		}
		
		if (!builder.isEmpty()) {
			connection.disconnect(Component.translatable("gui.epicfight.warn.animation_unsync", builder.toString()));
		}
	}
	
	/**************************************************
	 * User-animation loader
	 **************************************************/
	@SuppressWarnings({ "deprecation" })
	private void readAnimationFromJson(ResourceLocation rl, JsonObject json) throws Exception {
		JsonElement constructorElement = json.get("constructor");
		
		if (constructorElement == null) {
			throw new IllegalStateException("No constructor information has provided in User animation " + rl);
		}
		
		JsonObject constructorObject = constructorElement.getAsJsonObject();
		String invocationCommand = constructorObject.get("invocation_command").getAsString();
		StaticAnimation animation = InstantiateInvoker.invoke(invocationCommand, StaticAnimation.class).getResult();
		this.userAnimations.put(animation.getRegistryName(), animation);
		this.userAnimationInvocationCommands.put(animation.getRegistryName(), invocationCommand);
		
		JsonElement propertiesElement = json.get("properties");
		
		if (propertiesElement != null) {
			JsonObject propertiesObject = propertiesElement.getAsJsonObject();
			
			for (Map.Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
				AnimationProperty<?> propertyKey = AnimationProperty.getSerializableProperty(entry.getKey());
				Object value = propertyKey.parseFrom(entry.getValue());
				animation.addPropertyUnsafe(propertyKey, value);
			}
		}
	}
}

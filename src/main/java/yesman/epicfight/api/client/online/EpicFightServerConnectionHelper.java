package yesman.epicfight.api.client.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.function.BiConsumer;

import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightServerConnectionHelper {
	public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(60000)).build();
	private static final String LIB_FILE = "ServerCommunicationHelper";
	private static boolean SUPPORTED;
	
	public static boolean supported() {
		return SUPPORTED;
	}
	
	public static boolean init(String configPath) {
		SupportedOS os = SupportedOS.getOS();
		boolean supported = false;
		
		if (os != null) {
			String libpath = MessageFormat.format("/assets/epicfight/nativelib/{0}/{1}{2}", os.telemetryName(), LIB_FILE, os.libExtension());
			InputStream inputstream = EpicFightMod.class.getResourceAsStream(libpath);
			
			if (inputstream != null) {
				File file = new File(configPath + "/epicfight/native/" + LIB_FILE + os.libExtension());
				boolean shouldCreate;
				
				if (file.exists()) {
					try {
						String sha256 = ParseUtil.getBytesSHA256Hash(new FileInputStream(file).readAllBytes());
						shouldCreate = !sha256.equals(os.SHA256());
					} catch (IOException e) {
						shouldCreate = true;
					}
				} else {
					shouldCreate = true;
				}
				
				if (shouldCreate) {
					try {
						EpicFightMod.LOGGER.info("Created temporary lib file at: " + file.getPath());
						file.delete();
						
						if (!file.getParentFile().isDirectory()) {
							file.getParentFile().mkdirs();
						}
						
						file.createNewFile();
						FileOutputStream fos = new FileOutputStream(file);
						byte[] bytes = inputstream.readAllBytes();
						fos.write(bytes, 0, bytes.length);
						fos.flush();
						fos.close();
					} catch (IOException e) {
						EpicFightMod.LOGGER.info("Can't read library file: " + e.getMessage());
					}
				}
				
				boolean exceptionOccurred = false;
				
				try {
					System.load(file.toString());
				} catch (UnsatisfiedLinkError e) {
					//e.printStackTrace();
					exceptionOccurred = true;
				}
				
				supported = !exceptionOccurred;
			} else {
				supported = false;
				EpicFightMod.LOGGER.info("Can't read library file: " + libpath);
			}
		}
		
		SUPPORTED = supported;
		
		return supported;
	}
	
	public static native void autoLogin(String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void signIn(String minecraftUuid, String authenticationCode, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void signOut(String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void getAvailableCosmetics(String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void saveConfiguration(String postBody, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void getPlayerSkinInfo(String minecraftUuid, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void loadRemoteMesh(String path, BiConsumer<Mesh, Exception> onResponse);
	
	private enum SupportedOS {
		//LINUX("linux", ".so", ""),
		//SOLARIS("solaris", ".so", ""),
		WINDOWS("windows", ".dll", "2f1f38f1aff1fb405408865a22478ad464a6786fe636e13adf67891b9eb8e6e5"),
		//OSX("mac", ".dylib", "")
		;
		
		public static SupportedOS getOS() {
			try {
				return SupportedOS.valueOf(Util.getPlatform().name());
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
		
		private final String telemetryName;
		private final String libExtension;
		private final String SHA256;
		
		SupportedOS(String telemetryName, String libExtension, String SHA256) {
			this.telemetryName = telemetryName;
			this.libExtension = libExtension;
			this.SHA256 = SHA256;
		}
		
		String telemetryName() {
			return this.telemetryName;
		}
		
		String libExtension() {
			return this.libExtension;
		}
		
		String SHA256() {
			return this.SHA256;
		}
	}
}

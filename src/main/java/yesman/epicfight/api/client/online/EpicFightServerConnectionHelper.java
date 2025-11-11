package yesman.epicfight.api.client.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.function.BiConsumer;

import javax.net.ssl.SSLContext;

import net.minecraft.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightServerConnectionHelper {
	public static HttpClient HTTP_CLIENT;
	private static final String LIB_FILE = "ServerCommunicationHelper";
	private static boolean SUPPORTED;
	
	public static boolean supported() {
		return SUPPORTED;
	}
	
	public static boolean init(String configPath) {
		EpicFightMod.LOGGER.info("Epic Fight web server connection helper: Initialize");
		
		SupportedOS os = SupportedOS.getOS();
		boolean supported = false;
		
		try {
			SSLContext ssl = SSLContext.getInstance("TLSv1.3");
			ssl.init(null, null, null);
			
			HTTP_CLIENT = HttpClient.newBuilder().sslContext(ssl).connectTimeout(Duration.ofMillis(60000)).build();
		} catch (NoSuchAlgorithmException e) {
			EpicFightMod.LOGGER.warn("TLS 1.3 not found, we do not support TLS communication lower than 1.3");
			HTTP_CLIENT = null;
			SUPPORTED = false;
			return false;
		} catch (KeyManagementException e) {
			EpicFightMod.LOGGER.warn("Failed at initializing SSL context");
			HTTP_CLIENT = null;
			SUPPORTED = false;
			return false;
		} catch (Exception e) {
			EpicFightMod.LOGGER.warn("Failed at initializing " + e);
			HTTP_CLIENT = null;
			SUPPORTED = false;
			return false;
		}
		
		if (os != null) {
			String libpath = MessageFormat.format("/assets/epicfight/nativelib/{0}/{1}{2}", os.telemetryName(), LIB_FILE, os.libExtension());
			InputStream inputStream = EpicFightMod.class.getResourceAsStream(libpath);
			
			if (inputStream != null) {
				File configNativeFile = new File(configPath + "/epicfight/native/" + LIB_FILE + os.libExtension());
				byte[] resourceBytes = null;
				boolean shouldCreate;
				
				if (configNativeFile.exists()) {
					try {
						String configFileSHA256 = ParseUtil.getBytesSHA256Hash(new FileInputStream(configNativeFile).readAllBytes());
						resourceBytes = inputStream.readAllBytes();
						String resourceFileSHA256 = ParseUtil.getBytesSHA256Hash(resourceBytes);
						shouldCreate = !configFileSHA256.equals(resourceFileSHA256);
					} catch (IOException e) {
						e.printStackTrace();
						shouldCreate = true;
					} finally {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}
				} else {
					shouldCreate = true;
				}
				
				if (shouldCreate) {
					try {
						EpicFightMod.LOGGER.info("Created temporary lib file at: " + configNativeFile.getPath());
						configNativeFile.delete();
						
						if (!configNativeFile.getParentFile().isDirectory()) {
							configNativeFile.getParentFile().mkdirs();
						}
						
						configNativeFile.createNewFile();
						FileOutputStream fos = new FileOutputStream(configNativeFile);
						if (resourceBytes == null) resourceBytes = inputStream.readAllBytes();
						fos.write(resourceBytes, 0, resourceBytes.length);
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
						EpicFightMod.LOGGER.info("Can't read library file: " + e);
					}
				}
				
				boolean exceptionOccurred = false;
				
				try {
					System.load(configNativeFile.toString());
				} catch (UnsatisfiedLinkError e) {
					exceptionOccurred = true;
					EpicFightMod.LOGGER.warn("Failed at loading library file");
				}
				
				supported = !exceptionOccurred;
			} else {
				supported = false;
				EpicFightMod.LOGGER.info("Can't read library file: " + libpath);
			}
		} else {
			EpicFightMod.LOGGER.info("Epic Fight web server connection helper: Unsupported OS: " + Util.getPlatform().name());
		}
		
		SUPPORTED = supported;
		
		return supported;
	}
	
	public static native void autoLogin(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void signIn(String domain, String minecraftUuid, String authenticationCode, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void signOut(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void getAvailableCosmetics(String domain, String minecraftUuid, String accessToken, String refreshToken, String provider, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void saveConfiguration(String domain, String postBody, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void getPlayerSkinInfo(String domain, String minecraftUuid, BiConsumer<HttpResponse<String>, Exception> onResponse);
	
	public static native void loadRemoteMesh(String domain, String path, BiConsumer<Mesh, Exception> onResponse);
	
	private enum SupportedOS {
		//LINUX("linux", ".so"),
		//SOLARIS("solaris", ".so"),
		WINDOWS("windows", ".dll"),
		//OSX("mac", ".dylib")
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
		
		SupportedOS(String telemetryName, String libExtension) {
			this.telemetryName = telemetryName;
			this.libExtension = libExtension;
		}
		
		String telemetryName() {
			return this.telemetryName;
		}
		
		String libExtension() {
			return this.libExtension;
		}
	}
}

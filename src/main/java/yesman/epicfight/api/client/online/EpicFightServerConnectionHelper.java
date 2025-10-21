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
			InputStream inputstream = EpicFightMod.class.getResourceAsStream(libpath);
			
			if (inputstream != null) {
				File file = new File(configPath + "/epicfight/native/" + LIB_FILE + os.libExtension());
				boolean shouldCreate;
				
				if (file.exists()) {
					InputStream inputStream = null;
					
					try {
						inputStream = new FileInputStream(file);
						String sha256 = ParseUtil.getBytesSHA256Hash(inputStream.readAllBytes());
						shouldCreate = !sha256.equals(os.SHA256());
					} catch (IOException e) {
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

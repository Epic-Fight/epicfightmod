package yesman.epicfight.client.online.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class RemoteTexture extends SimpleTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private final String urlString;
	@Nullable
	private CompletableFuture<?> future;
	
	public RemoteTexture(String pUrlString, ResourceLocation defaultLocation) {
		super(defaultLocation);
		
		this.urlString = pUrlString;
	}
	
	private void loadCallback(NativeImage pImage) {
		Minecraft.getInstance().execute(() -> {
			if (!RenderSystem.isOnRenderThread()) {
				RenderSystem.recordRenderCall(() -> {
					this.upload(pImage);
				});
			} else {
				this.upload(pImage);
			}
		});
	}
	
	private void upload(NativeImage pImage) {
		TextureUtil.prepareImage(this.getId(), pImage.getWidth(), pImage.getHeight());
		pImage.upload(0, 0, 0, true);
	}
	
	@Override
	public void load(ResourceManager pResourceManager) throws IOException {
		if (this.future == null) {
			this.future = CompletableFuture.runAsync(() -> {
				LOGGER.debug("Downloading http texture from {}", this.urlString);
				
				try {
					HttpURLConnection httpurlconnection = (HttpURLConnection)(URI.create(this.urlString).toURL()).openConnection(Minecraft.getInstance().getProxy());
					httpurlconnection.setDoInput(true);
					httpurlconnection.setDoOutput(false);
					httpurlconnection.connect();
					
					if (httpurlconnection.getResponseCode() / 100 == 2) {
						InputStream inputstream = httpurlconnection.getInputStream();
						
						Minecraft.getInstance().execute(() -> {
							NativeImage nativeimage1 = this.load(inputstream);
							
							if (nativeimage1 != null) {
								this.loadCallback(nativeimage1);
							}
							
							if (httpurlconnection != null) {
								httpurlconnection.disconnect();
							}
						});
						
						return;
					}
				} catch (Exception exception) {
					LOGGER.error("Couldn't download http texture", (Throwable) exception);
					return;
				}
			}, Util.backgroundExecutor());
		}
	}
	
	@Nullable
	private NativeImage load(InputStream pStream) {
		NativeImage nativeimage = null;
		
		try {
			nativeimage = NativeImage.read(pStream);
		} catch (Exception exception) {
			LOGGER.warn("Error while loading the skin texture", (Throwable)exception);
		}
		
		return nativeimage;
	}
}
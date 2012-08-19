package org.mariotaku.twidere.extension.uploader.imgur;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.IImageUploader;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;

/**
 * Image uploader example
 * 
 * @author mariotaku
 */
public class ImgurUploaderService extends Service implements Constants {

	private final ImageUploaderStub mBinder = new ImageUploaderStub(this);

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * @param file_uri Image file uri to send
	 * @param message Tweet to send, you can use this to describe image you
	 *            upload.
	 * @return Image uri server returned.
	 */
	public Uri upload(Uri file_uri, String message) {
		if (file_uri == null) return null;
		final File file = new File(file_uri.getPath());
		if (!file.exists()) return null;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERRENCES_NAME, MODE_PRIVATE);
		final String url = "http://api.imgur.com/2/upload.json";
		final String user_api_key = preferences.getString(PREFERENCE_KEY_API_KEY, null);
		final String api_key = user_api_key != null && user_api_key.length() >= 0 ? user_api_key : IMGUR_API_KEY;
		final String link_type = preferences.getString(PREFERENCE_KEY_LINK_TYPE, DEFAULT_LINK_TYPE);
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpContext localContext = new BasicHttpContext();
		final HttpPost httpPost = new HttpPost(url);

		try {
			final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("image", new FileBody(file));
			entity.addPart("key", new StringBody(api_key));
			httpPost.setEntity(entity);
			final HttpResponse response = httpClient.execute(httpPost, localContext);
			final String response_string = EntityUtils.toString(response.getEntity());
			final JSONObject json = new JSONObject(response_string);
			final JSONObject upload = json.getJSONObject("upload");
			final JSONObject links =upload.getJSONObject("links");
			final String original = links.getString(link_type);
			return Uri.parse(original);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	private static final class ImageUploaderStub extends IImageUploader.Stub {

		final WeakReference<ImgurUploaderService> mService;

		public ImageUploaderStub(ImgurUploaderService service) {

			mService = new WeakReference<ImgurUploaderService>(service);
		}

		@Override
		public Uri upload(Uri file_uri, String message) {
			return mService.get().upload(file_uri, message);
		}

	}

}

package org.jellyfin.sdk.api.okhttp

import kotlinx.coroutines.CoroutineScope
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.ApiClientFactory
import org.jellyfin.sdk.api.client.HttpClientOptions
import org.jellyfin.sdk.api.sockets.SocketConnection
import org.jellyfin.sdk.api.sockets.SocketConnectionFactory
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo
import java.security.KeyStore
import java.security.cert.Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.time.toJavaDuration

/**
 * Create a new factory that creates OkHttp ApiClient instances and SocketConnections on a shared connection pool. All
 * instances will be built on top of the [base] okHttpClient.
 */
public class OkHttpFactory(
	public val base: okhttp3.OkHttpClient = okhttp3.OkHttpClient.Builder().build(),
) : ApiClientFactory, SocketConnectionFactory {
	override fun create(
		baseUrl: String?,
		accessToken: String?,
		clientInfo: ClientInfo,
		deviceInfo: DeviceInfo,
		httpClientOptions: HttpClientOptions,
		socketConnectionFactory: SocketConnectionFactory,
		caCertificate: Certificate?,
		allowSystemCas: Boolean,
	): ApiClient = OkHttpClient(
		client = createClient(httpClientOptions, caCertificate, allowSystemCas),
		initialBaseUrl = baseUrl,
		initialAccessToken = accessToken,
		initialClientInfo = clientInfo,
		initialDeviceInfo = deviceInfo,
		httpClientOptions = httpClientOptions,
		socketConnectionFactory = socketConnectionFactory,
	)

	override fun create(
		clientOptions: HttpClientOptions,
		scope: CoroutineScope,
		caCertificate: Certificate?,
		allowSystemCas: Boolean,
	): SocketConnection = OkHttpSocketConnection(
		client = createClient(clientOptions, caCertificate, allowSystemCas),
		scope = scope,
	)

	/**
	 * Create a new [okhttp3.OkHttpClient] instance with the [HttpClientOptions] applied to it.
	 */
	public fun createClient(
		httpClientOptions: HttpClientOptions, caCertificate: Certificate? = null, allowSystemCas: Boolean = true
	): okhttp3.OkHttpClient {
		val (sslSocketFactory, trustManager) = getSSLSocketFactoryArgs(caCertificate, allowSystemCas)
		return base.newBuilder()
			.followRedirects(httpClientOptions.followRedirects)
			.connectTimeout(httpClientOptions.connectTimeout.toJavaDuration())
			.callTimeout(httpClientOptions.requestTimeout.toJavaDuration())
			.readTimeout(httpClientOptions.socketTimeout.toJavaDuration())
			.writeTimeout(httpClientOptions.socketTimeout.toJavaDuration())
			.sslSocketFactory(sslSocketFactory, trustManager)
			.build()
	}

	public companion object {
		/**
		 * Returns arguments for [okhttp3.OkHttpClient.Builder.sslSocketFactory].
		 */
		@JvmStatic
		internal fun getSSLSocketFactoryArgs(
			caCertificate: Certificate?,
			allowSystemCas: Boolean
		): Pair<SSLSocketFactory, X509TrustManager> {
			val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
			keyStore.load(null)
			if (caCertificate != null) {
				keyStore.setCertificateEntry("ca", caCertificate)
			}
			if (allowSystemCas) {
				val defaultTrustManagerFactory =
					TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
				defaultTrustManagerFactory.init(null as KeyStore?)
				val defaultTrustManager = defaultTrustManagerFactory.trustManagers.first() as X509TrustManager
				defaultTrustManager.acceptedIssuers.forEachIndexed { index, certificate ->
					keyStore.setCertificateEntry(
						"system_ca$index",
						certificate
					)
				}
			}
			val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
			trustManagerFactory.init(keyStore)
			val trustManager: X509TrustManager = trustManagerFactory.trustManagers.first() as X509TrustManager
			val sslContext: SSLContext = SSLContext.getInstance("TLS")
			sslContext.init(null, arrayOf(trustManager), null,)
			return Pair(sslContext.socketFactory, trustManager)
		}
	}
}

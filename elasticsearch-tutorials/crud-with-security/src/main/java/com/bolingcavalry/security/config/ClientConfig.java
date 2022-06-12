package com.bolingcavalry.security.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

@ConfigurationProperties(prefix = "elasticsearch") //配置的前缀
@Configuration
@Slf4j
public class ClientConfig {

    @Setter
    private String hosts;

    @Setter
    private String username;

    @Setter
    private String passwd;

    @Setter
    private String apikey;

    /**
     * 解析配置的字符串，转为HttpHost对象数组
     * @return
     */
    private HttpHost[] toHttpHost() {
        if (!StringUtils.hasLength(hosts)) {
            throw new RuntimeException("invalid elasticsearch configuration");
        }

        String[] hostArray = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];
        HttpHost httpHost;
        for (int i = 0; i < hostArray.length; i++) {
            String[] strings = hostArray[i].split(":");
            httpHost = new HttpHost(strings[0], Integer.parseInt(strings[1]), "https");
            httpHosts[i] = httpHost;
        }

        return httpHosts;
    }

    @Bean
    public ElasticsearchClient clientByPasswd() throws Exception {
        ElasticsearchTransport transport = getElasticsearchTransport(username, passwd, toHttpHost());
        return new ElasticsearchClient(transport);
    }

    private static SSLContext buildSSLContext() {
        ClassPathResource resource = new ClassPathResource("es01.crt");
        SSLContext sslContext = null;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa;
            try (InputStream is = resource.getInputStream()) {
                trustedCa = factory.generateCertificate(is);
            }
            KeyStore trustStore = KeyStore.getInstance("pkcs12");
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", trustedCa);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, null);
            sslContext = sslContextBuilder.build();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            log.error("ES连接认证失败", e);
        }

        return sslContext;
    }

    private static ElasticsearchTransport getElasticsearchTransport(String username, String passwd, HttpHost...hosts) {
        // 账号密码的配置
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, passwd));

        // 自签证书的设置，并且还包含了账号密码
        HttpClientConfigCallback callback = httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setSSLContext(buildSSLContext())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setDefaultCredentialsProvider(credentialsProvider);

        // 用builder创建RestClient对象
        RestClient client = RestClient
                           .builder(hosts)
                           .setHttpClientConfigCallback(callback)
                           .build();

        return new RestClientTransport(client, new JacksonJsonpMapper());
    }

    private static ElasticsearchTransport getElasticsearchTransport(String apiKey, HttpHost...hosts) {
        // 将ApiKey放入header中
        Header[] headers = new Header[] {new BasicHeader("Authorization", "ApiKey " + apiKey)};

        // es自签证书的设置
        HttpClientConfigCallback callback = httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setSSLContext(buildSSLContext())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        // 用builder创建RestClient对象
        RestClient client = RestClient
                           .builder(hosts)
                           .setHttpClientConfigCallback(callback)
                           .setDefaultHeaders(headers)
                           .build();

        return new RestClientTransport(client, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient clientByApiKey() throws Exception {
        ElasticsearchTransport transport = getElasticsearchTransport(apikey, toHttpHost());
        return new ElasticsearchClient(transport);
    }

}

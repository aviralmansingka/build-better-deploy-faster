package com.aviral.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}

@Component
class NettyWebServerFactorySslCustomizer
		implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
	@Override
	public void customize(NettyReactiveWebServerFactory serverFactory) {
		Ssl ssl = new Ssl();
		ssl.setEnabled(true);
		ssl.setKeyStore("classpath:gateway.p12");
		ssl.setKeyAlias("alias");
		ssl.setKeyPassword("password");
		ssl.setKeyStorePassword("secret");
		Http2 http2 = new Http2();
		http2.setEnabled(false);
		serverFactory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
		serverFactory.setPort(8443);
	}
}
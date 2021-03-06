package com.entfrm.auth.config;

import com.entfrm.auth.handler.EntfrmWebResponseExceptionTranslator;
import com.entfrm.auth.service.EntfrmUserDetailService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yong
 * @date 2020/3/10
 * @description TokenStore
 */
@Configuration
@AllArgsConstructor
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final EntfrmUserDetailService userDetailService;
    private final TokenStore jwtTokenStore;
    private final JwtAccessTokenConverter jwtAccessTokenConverter;
    private final TokenEnhancer tokenEnhancer;
    private final DataSource dataSource;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> enhancers = new ArrayList<>();
        enhancers.add(tokenEnhancer);
        enhancers.add(jwtAccessTokenConverter);
        enhancerChain.setTokenEnhancers(enhancers);

        endpoints.authenticationManager(authenticationManager)
                .tokenStore(jwtTokenStore)
                .tokenEnhancer(enhancerChain)
                .accessTokenConverter(jwtAccessTokenConverter)
                .userDetailsService(userDetailService)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);//允许 GET、POST 请求获取 token，即访问端点：oauth/token

        endpoints.reuseRefreshTokens(true);//oauth2登录异常处理
        endpoints.exceptionTranslator(new EntfrmWebResponseExceptionTranslator());//oauth2登录异常处理
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();//允许表单认证
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);//客户端配置 使用jdbc数据库存储
    }
}

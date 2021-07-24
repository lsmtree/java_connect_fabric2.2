package com.example.java_to_fabric.fabric;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;

@Component
public class fabricConfig {

    private X509Identity identity;
    private Network network;
    private HFClient client;

    private File getConfigDir(String target) {
        return new File(FileSystems.getDefault().getPath("").toFile().getAbsoluteFile(), target);
    }
    private String readStrFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
    //获取当前实体
    @Bean
    @Lazy
    public X509Identity getIdentity() throws IOException, CertificateException, InvalidKeyException {
        File configDir = new File(this.getConfigDir("config"), "peerOrganizations");
        String certPem = this.readStrFile(new File(configDir, "org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem"));
        String keyPem = this.readStrFile(new File(configDir, "org1.example.com/users/User1@org1.example.com/msp/keystore/priv_sk"));
        this.identity = Identities.newX509Identity("Org1MSP", Identities.readX509Certificate(certPem), Identities.readPrivateKey(keyPem));
        return this.identity;
    }
    //获取fabric通道
    @Bean
    @Lazy
    @DependsOn(value = "getIdentity")
    public Channel getChannel(){
        File configDir = this.getConfigDir("config");
        try {
            this.network = Gateway.createBuilder()
                    .identity(this.identity)
                    .networkConfig(new File(configDir,"connection_profile.yaml").toPath())
                    .connect()
                    .getNetwork("mychannel");
        } catch (Exception e) {
            throw new RuntimeException("通道初始化失败",e);
        }
        return this.network.getChannel();
    }
    //获取HFClient
    @Bean
    @Lazy
    @DependsOn(value = "getIdentity")
    public HFClient getHFClient() throws IOException {
        File configDir = new File(this.getConfigDir("config"), "peerOrganizations");
        String certPem = this.readStrFile(new File(configDir, "org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem"));
        GatewayUser user = new GatewayUser("User1@Org1.example.com","Org1MSP", new X509Enrollment(this.identity.getPrivateKey(),certPem));
        this.client = HFClient.createNewInstance();
        try {
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(user);
        } catch (Exception e) {
            throw new RuntimeException("客户端初始化失败", e);
        }
        return this.client;
    }


}

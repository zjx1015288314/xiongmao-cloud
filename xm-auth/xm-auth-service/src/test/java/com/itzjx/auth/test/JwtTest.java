package com.itzjx.auth.test;

import com.itzjx.auth.entity.UserInfo;
import com.itzjx.auth.utils.JwtUtils;
import com.itzjx.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
public class JwtTest {
    private static final String pubKeyPath = "/home/zhaojiexiong/rsa/rsa.pub";

    private static final String priKeyPath = "/home/zhaojiexiong/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    //测试生成公钥和私钥
//    @Test
//    public void testRsa() throws Exception {
//        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
//    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    /**
     * 测试生成token
     * @throws Exception
     */
    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    /**
     * 测试解析token
     * @throws Exception
     */
    @Test
    public void testParseToken() throws Exception {

        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU5MTAwNTMwMX0.TXKqarqb6aMtIoJnpuQejJWi430gtlwMkrqKH45NYZZqzqaYSl4jQg8VX60uvhhbYzY0FQnD6MeNvO_EfV4TARMO2quhH7m_x_8Z1XhIkBg1GAUrhGhxdmxjjcOTtcLHPX3DxWqPjWrRBDn5FIDXb2y17w0uWDLDWGIWFfVQ-oI";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}

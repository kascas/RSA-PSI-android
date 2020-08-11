package server;

import bloom.Bloom;
import bloom.BloomIO;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import utils.FileIO;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: kascas
 * @date: 2020/8/10-19:32
 * @description:
 */
public class RSAPSI {
    
    private BigInteger e, n, d;
    private String username;
    private String rootDir;
    private int keySize;
    
    public RSAPSI(String rootDir, int keySize) {
        this.rootDir = rootDir;
        this.keySize = keySize;
    }
    
    // 设定此次查询的用户名
    public void setUsername(String username) {
        this.username = username;
    }
    
    // 建立布隆过滤器并写入bloom文件
    public void setBloom() {
        List<String> strList = FileIO.fileReader(rootDir + "\\x.txt");
        Bloom bloom = new Bloom();
        int bloomSize = Math.max(strList.size(), 1000);
        bloom.bloom_init(bloomSize, (double) 1 / strList.size());
        for (String str : strList) {
            bloom.bloom_add(new BigInteger(str, 16).modPow(d, n).toString(16));
        }
        BloomIO.bloomWriter(bloom, rootDir + "\\bloom");
    }
    
    
    // 获取新的RSA密钥
    public void newKey() {
        RSAKeyPairGenerator keyGen = new RSAKeyPairGenerator();
        keyGen.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(), keySize, 80));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();
        RSAKeyParameters pub = (RSAKeyParameters) keyPair.getPublic();
        e = pub.getExponent();
        n = pub.getModulus();
        RSAPrivateCrtKeyParameters pra = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
        d = pra.getExponent();
        pubKeyWriter();
        priKeyWriter();
    }
    
    // 将公钥写入pub_key文件
    public void pubKeyWriter() {
        List<String> strList = new ArrayList<>();
        strList.add(e.toString(16));
        strList.add(n.toString(16));
        FileIO.fileWriter(rootDir + "\\pub_key_" + username, strList);
    }
    
    // 将私钥写入pri_key文件
    public void priKeyWriter() {
        List<String> strList = new ArrayList<>();
        strList.add(d.toString(16));
        strList.add(n.toString(16));
        FileIO.fileWriter(rootDir + "\\pri_key_" + username, strList);
    }
    
    
    // 将私钥写入pri_key文件
    public void priKeyReader() {
        List<String> userList = FileIO.fileReader(rootDir + "\\pri_key_" + username);
        d = new BigInteger(userList.get(0), 16);
        n = new BigInteger(userList.get(1), 16);
    }
    
    // 盲签名
    public void sign() {
        List<String> aList = FileIO.fileReader(rootDir + username);
        List<String> bList = new ArrayList<>();
        BigInteger a, b;
        for (String str : aList) {
            a = new BigInteger(str, 16);
            b = a.modPow(d, n);
            bList.add(b.toString(16));
        }
        FileIO.fileWriter(rootDir + username + "_sign", bList);
    }
}

package com.example.psi.client;


import com.example.psi.bloom.Bloom;
import com.example.psi.bloom.BloomIO;
import com.example.psi.util.FileIO;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: kascas
 * @date: 2020/8/10-21:18
 * @description:
 */
public class RSAPSI {

    private BigInteger e;
    private BigInteger n;

    public void setKey(String file) {
        List<String> strList = FileIO.fileReader(file);
        e = new BigInteger(strList.get(0), 16);
        n = new BigInteger(strList.get(1), 16);
    }


    public static Bloom setBloom(String file) {
        return BloomIO.bloomReader(file);
    }

    public void blindFactorGenerator(String rFile) {
        BigInteger r, r_inv, r_e, gcd, ONE = BigInteger.ONE, ZERO = BigInteger.ZERO;
        List<String> rList = new ArrayList<>();
        int nLen = n.bitLength();
        for (int i = 0; i < 1000; i++) {
            do {
                r = new BigInteger(nLen - 1, new SecureRandom());
                gcd = r.gcd(n);
            } while (r.compareTo(ZERO) == 0 || r.compareTo(ONE) == 0 || gcd.compareTo(ONE) != 0);
            r_inv = r.modInverse(n);
            r_e = r.modPow(e, n);
            rList.add(r.toString(16) + "," + r_inv.toString(16) + "," + r_e.toString(16));
        }
        FileIO.fileWriter(rFile, rList);
    }

    public void blind(String yFile, String aFile, String rFile) {
        List<String> yList = FileIO.fileReader(yFile);
        List<String> rList = FileIO.fileReader(rFile);
        List<String> aList = new ArrayList<>();
        BigInteger y, r_e;
        for (int i = 0; i < yList.size(); i++) {
            y = new BigInteger(yList.get(i), 16);
            r_e = new BigInteger(rList.get(i).split(",")[2], 16);
            aList.add(y.multiply(r_e).mod(n).toString(16));
        }
        FileIO.fileWriter(aFile, aList);
    }

    public void unblindAndCheck(String bFile, String sFile, String rFile, String yFile, Bloom bloom) {
        List<String> bList = FileIO.fileReader(bFile);
        List<String> rList = FileIO.fileReader(rFile);
        List<Integer> indexList = new ArrayList<>();
        BigInteger b, r_inv, s;
        for (int i = 0; i < bList.size(); i++) {
            b = new BigInteger(bList.get(i), 16);
            r_inv = new BigInteger(rList.get(i).split(",")[1], 16);
            s = b.multiply(r_inv).mod(n);
            if (bloom.bloom_check(s.toString(16)))
                indexList.add(i);
        }
        List<String> yList = FileIO.fileReader(yFile);
        List<String> sList = new ArrayList<>();
        for (int i = 0; i < indexList.size(); i++) {
            sList.add(yList.get(indexList.get(i)));
        }
        FileIO.fileWriter(sFile, sList);
    }

}

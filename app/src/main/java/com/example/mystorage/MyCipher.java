package com.example.mystorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.room.Room;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class MyCipher {
    public Context context;
    private static final int SALT_BYTES = 8;
    private static final int IV_BYTES = 16;
    private static final int PBK_ITERATIONS = 1000;
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PBE_ALGORITHM = "PBEwithSHA256and128BITAES-CBC-BC";


    public String EncryptString(String inputString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        tempEncryptedData encData = new tempEncryptedData();
        SecureRandom rnd = new SecureRandom();
        encData.salt = new byte[SALT_BYTES];
        encData.iv = new byte[IV_BYTES]; // AES block size
        rnd.nextBytes(encData.salt);
        rnd.nextBytes(encData.iv);
        String password = getCode(context);

        byte[] data = inputString.getBytes("UTF-8");

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), encData.salt, PBK_ITERATIONS);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        SecretKey key = secretKeyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(encData.iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        encData.encryptedData = cipher.doFinal(data);

        //String result = byteArrayToHexString(joinByteArray(encData.salt, encData.iv, encData.encryptedData));
        String result = Base64.encodeToString(joinByteArray(encData.salt, encData.iv, encData.encryptedData), Base64.DEFAULT);
        return result;
    }

    public String DecryptString(String inputString) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        String password = getCode(context);

        tempEncryptedData encData = splitStringToBytes(inputString);

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), encData.salt, PBK_ITERATIONS);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        Key key = secretKeyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(encData.iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedData = cipher.doFinal(encData.encryptedData);
        String decDataAsString = new String(decryptedData, "UTF-8");
        return decDataAsString;
    }

    public ResourceData EncryptResourceData(ResourceData inputResourceData){
        ResourceData resourceData = null;
        try {
            resourceData = new ResourceData(
                    EncryptString(inputResourceData.getResource()),
                    EncryptString(inputResourceData.getLogin()),
                    EncryptString(inputResourceData.getPassword()),
                    EncryptString(inputResourceData.getDescription())
            );
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", "EncryptResourceData  CATCH");
        };
        return resourceData;
    };

    public ResourceData DecryptResourceData(ResourceData inputResourceData){
        ResourceData resourceData = null;
        try {
            resourceData = new ResourceData(
                    DecryptString(inputResourceData.getResource()),
                    DecryptString(inputResourceData.getLogin()),
                    DecryptString(inputResourceData.getPassword()),
                    DecryptString(inputResourceData.getDescription())
            );
            resourceData.setId(inputResourceData.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", "DecryptResourceData  CATCH");
        };
        return resourceData;
    }

    private static class tempEncryptedData {
        public byte[] salt;
        public byte[] iv;
        public byte[] encryptedData;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    private static byte[] joinByteArray(byte[] salt, byte[] iv, byte[] data) {
        return ByteBuffer.allocate(salt.length + iv.length + data.length)
                .put(salt)
                .put(iv)
                .put(data)
                .array();
    }


    private tempEncryptedData splitStringToBytes(String source) throws UnsupportedEncodingException {
        tempEncryptedData encData = new tempEncryptedData();

        //byte[] byteArray = source.getBytes("UTF-8");
        byte[] byteArray = Base64.decode(source, Base64.DEFAULT);

        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        byte[] salt = new byte[8];
        byte[] iv = new byte[16];
        byte[] data = new byte[byteArray.length - 24];
        bb.get(salt, 0, salt.length);
        bb.get(iv, 0, iv.length);
        bb.get(data, 0, data.length);

        encData.salt = salt;
        encData.iv = iv;
        encData.encryptedData = data;

        return encData;
    }


    private static final String PREF_FILE = "settings_pref";
    static void saveToPref(Context context, String str) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("code", str);
        editor.apply();
    }
    static String getCode(Context context) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final String defaultValue = "";
        return sharedPref.getString("code", defaultValue);
    }

    static boolean isPinExist(Context context){
        return !(getCode(context).equals(""));
    }

}

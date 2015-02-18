package edu.pitt.sis.paws.kt2.setNewPwd;

import java.util.UUID;

public class UUIDGenerator { 
    public UUIDGenerator() { 
    } 
    /** 
     * get a UUID 
     * @return String UUID 
     */ 
    public static String getUUID(){ 
        String s = UUID.randomUUID().toString(); 
        //去掉“-”符号 
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
    } 
    /** 
     * get specific number of UUID
     * @param number int UUID's number 
     * @return String[] UUID array
     */ 
    public static String[] getUUID(int number){ 
        if(number < 1){ 
            return null; 
        } 
        String[] ss = new String[number]; 
        for(int i=0;i<number;i++){ 
            ss[i] = getUUID(); 
        } 
        return ss; 
    } 
}   
package com.aldrin.ensarium.security;

//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//public final class Session {
//    
//    public static Session session;
//
//    public static Session demo() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//    private static int userId;
//    private static String username;
//    private static String fullName;
//    private Set<String> permissions;
//
//    public Session(int userId, String username, String fullName, Set<String> permissions) {
//        this.userId = userId;
//        this.username = username;
//        this.fullName = fullName;
//        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
//    }
//
//    public int userId() {
//        return getUserId();
//    }
//
//    public String username() {
//        return getUsername();
//    }
//
//    public String fullName() {
//        return getFullName();
//    }
//
//    public Set<String> permissions() {
//        return getPermissions();
//    }
//
//    public boolean has(String code) {
//        return getPermissions().contains(code);
//    }
//
//    private static int storeId;
//    private static String storeCode;
//    private static String storeName;
//    private static int terminalId;
//    private static String terminalCode;
//    private static String terminalName;
//
//    /**
//     * @return the userId
//     */
//    public int getUserId() {
//        return userId;
//    }
//
//    /**
//     * @param userId the userId to set
//     */
//    public void setUserId(int userId) {
//        this.userId = userId;
//    }
//
//    /**
//     * @return the username
//     */
//    public String getUsername() {
//        return username;
//    }
//
//    /**
//     * @param username the username to set
//     */
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    /**
//     * @return the fullName
//     */
//    public String getFullName() {
//        return fullName;
//    }
//
//    /**
//     * @param fullName the fullName to set
//     */
//    public void setFullName(String fullName) {
//        this.fullName = fullName;
//    }
//
//    /**
//     * @return the permissions
//     */
//    public Set<String> getPermissions() {
//        return permissions;
//    }
//
//    /**
//     * @param permissions the permissions to set
//     */
//    public void setPermissions(Set<String> permissions) {
//        this.permissions = permissions;
//    }
//
//    /**
//     * @return the storeId
//     */
//    public static int getStoreId() {
//        return storeId;
//    }
//
//    /**
//     * @param aStoreId the storeId to set
//     */
//    public static void setStoreId(int aStoreId) {
//        storeId = aStoreId;
//    }
//
//    /**
//     * @return the storeCode
//     */
//    public static String getStoreCode() {
//        return storeCode;
//    }
//
//    /**
//     * @param aStoreCode the storeCode to set
//     */
//    public static void setStoreCode(String aStoreCode) {
//        storeCode = aStoreCode;
//    }
//
//    /**
//     * @return the storeName
//     */
//    public static String getStoreName() {
//        return storeName;
//    }
//
//    /**
//     * @param aStoreName the storeName to set
//     */
//    public static void setStoreName(String aStoreName) {
//        storeName = aStoreName;
//    }
//
//    /**
//     * @return the terminalId
//     */
//    public static int getTerminalId() {
//        return terminalId;
//    }
//
//    /**
//     * @param aTerminalId the terminalId to set
//     */
//    public static void setTerminalId(int aTerminalId) {
//        terminalId = aTerminalId;
//    }
//
//    /**
//     * @return the terminalCode
//     */
//    public static String getTerminalCode() {
//        return terminalCode;
//    }
//
//    /**
//     * @param aTerminalCode the terminalCode to set
//     */
//    public static void setTerminalCode(String aTerminalCode) {
//        terminalCode = aTerminalCode;
//    }
//
//    /**
//     * @return the terminalName
//     */
//    public static String getTerminalName() {
//        return terminalName;
//    }
//
//    /**
//     * @param aTerminalName the terminalName to set
//     */
//    public static void setTerminalName(String aTerminalName) {
//        terminalName = aTerminalName;
//    }
//}



import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Session {

    // optional legacy global pointer for old code paths
    public static volatile Session session;

    private int userId;
    private String username;
    private String fullName;
    private Set<String> permissions = Collections.emptySet();

    private int storeId;
    private String storeCode;
    private String storeName;
    private int terminalId;
    private String terminalCode;
    private String terminalName;

    public Session(int userId, String username, String fullName, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        setPermissions(permissions);
    }

    public static Session getSession() {
        return session;
    }

    public int userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public String fullName() {
        return fullName;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public boolean has(String code) {
        return permissions.contains(code);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        if (permissions == null) {
            this.permissions = Collections.emptySet();
        } else {
            this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
        }
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(int terminalId) {
        this.terminalId = terminalId;
    }

    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
    }
}
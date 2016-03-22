package io.ap1.backendlesschattest;

import com.backendless.BackendlessUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 17/03/16.
 */
public class DataStore {
    public static ArrayList<MyBackendlessUser> userList = new ArrayList<>();
    public static ArrayList<String> blockList = new ArrayList<>();
    public static HashMap<String, String> duplicateCheck = new HashMap<>();
}

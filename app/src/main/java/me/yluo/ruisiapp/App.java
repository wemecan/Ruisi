package me.yluo.ruisiapp;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;

import me.yluo.ruisiapp.checknet.NetworkReceiver;
import me.yluo.ruisiapp.database.MyDB;
import me.yluo.ruisiapp.database.SQLiteHelper;

/**
 * Created by free2 on 16-3-11.
 * 共享的全局数据
 */
public class App extends Application {

    private Context context;
    private NetworkReceiver receiver = new NetworkReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();

        //注册网络变化广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);

        //清空消息数据库
        MyDB myDB = new MyDB(context);
        //最多缓存2000条历史纪录
        myDB.deleteOldHistory(2000);

        boolean enableDarkMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("setting_dark_mode", false);
        boolean auto = false;
        int cur = AppCompatDelegate.getDefaultNightMode();
        int to = cur;
        if (enableDarkMode) {//允许夜间模式
            if (auto = App.isAutoDarkMode(this)) {//自动夜间模式
                int[] time = App.getDarkModeTime(this);
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if ((hour >= time[0] || hour < time[1])) {
                    to = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    to = AppCompatDelegate.MODE_NIGHT_NO;
                }
            } else {
                to = AppCompatDelegate.MODE_NIGHT_YES;
            }
        } else {//不允许夜间模式
            to = AppCompatDelegate.MODE_NIGHT_NO;
        }

        if (cur != to) {
            AppCompatDelegate.setDefaultNightMode(to);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        //注册网络变化广播
        if (receiver != null) {
            Log.d("application onTerminate", "取消注册广播");
            unregisterReceiver(receiver);
        }

        //关闭数据库
        new SQLiteHelper(context).close();

    }

    public Context getContext() {
        return context;
    }

    //发布地址tid
    public static final String POST_TID = "805203";
    //启动时设定
    //论坛基地址
    private static final String BASE_URL_ME = "http://bbs.rs.xidian.me/";
    private static final String BASE_URL_RS = "http://rs.xidian.edu.cn/";
    //是否为校园网
    public static boolean IS_SCHOOL_NET = false;


    public static String getBaseUrl() {
        if (IS_SCHOOL_NET) {
            return BASE_URL_RS;
        } else {
            return BASE_URL_ME;
        }
    }

    public static boolean ISLOGIN(Context context) {
        return !TextUtils.isEmpty(App.getUid(context));
    }

    public static String getUid(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_UID_KEY, "");
    }

    public static void setHash(Context context, String hash) {
        if (TextUtils.isEmpty(hash)) {
            return;
        }
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(HASH_KEY, hash);
        editor.apply();
    }

    public static String getName(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_NAME_KEY, "");
    }

    public static String getGrade(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_GRADE_KEY, "");
    }

    public static boolean isAutoDarkMode(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getBoolean(AUTO_DARK_MODE_KEY, true);
    }

    public static void setAutoDarkMode(Context context, boolean value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(AUTO_DARK_MODE_KEY, value);
        editor.apply();
    }

    public static void setDarkModeTime(Context context, boolean isStart, int value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        if (isStart) {
            editor.putInt(START_DARK_TIME_KEY, value);
        } else {
            editor.putInt(END_DARK_TIME_KEY, value);
        }
        editor.apply();
    }

    public static int[] getDarkModeTime(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        int[] ret = new int[2];
        ret[0] = shp.getInt(START_DARK_TIME_KEY, 21);
        ret[1] = shp.getInt(END_DARK_TIME_KEY, 6);
        return ret;
    }


    /**
     * config
     * todo 把一些常量移到这儿来
     */

    //记录上次未读消息的id
    public static final String MY_SHP_NAME = "ruisi_shp";

    public static final String NOTICE_MESSAGE_REPLY_KEY = "message_notice_reply";
    public static final String NOTICE_MESSAGE_AT_KEY = "message_notice_at";

    public static final String AUTO_DARK_MODE_KEY = "auto_dark_mode";
    public static final String START_DARK_TIME_KEY = "start_dart_time";
    public static final String END_DARK_TIME_KEY = "end_dark_time";
    public static final String USER_UID_KEY = "user_uid";
    public static final String USER_NAME_KEY = "user_name";
    public static final String HASH_KEY = "forum_hash";
    public static final String USER_GRADE_KEY = "user_grade";
    public static final String IS_REMBER_PASS_USER = "login_rember_pass";
    public static final String LOGIN_NAME = "login_name";
    public static final String LOGIN_PASS = "login_pass";
    public static final String CHECK_UPDATE_KEY = "check_update_time";
    public static final String LOGIN_RS = "http://rs.xidian.edu.cn/member.php?mod=logging&action=login&mobile=2";
    public static final String LOGIN_ME = "http://bbs.rs.xidian.me/member.php?mod=logging&action=login&mobile=2";

    public static final String CHECK_UPDATE_URL = "forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2";
}

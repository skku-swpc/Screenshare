package com.screenshare;

import android.app.Notification;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.ForegroundService;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;
import com.googlecode.android_scripting.interpreter.html.HtmlActivityTask;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ScriptService extends ForegroundService {
	private final static int NOTIFICATION_ID = NotificationIdFactory.create();
	private final IBinder mBinder;
	public static Ipc ipc;
    private static AndroidProxy mProxy = null;
    private Timer timer = new Timer();
    
	public class LocalBinder extends Binder {
	}

	public ScriptService() {
		super(NOTIFICATION_ID);
		mBinder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ipc = new Ipc(getApplicationContext());
	}

	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
			if (mProxy != null)
				mProxy.shutdown();
			mProxy = new AndroidProxy(this, null, true);
			InetSocketAddress net = mProxy.startLocal();
			int port = net.getPort();
			String secret = mProxy.getSecret();
			ipc.comBinAuth("/set_sl4a"+port+"_"+secret);
			timer.schedule(new TimerTask() { 
				public void run(){ 
					stopSelf(); 
					if(mProxy != null) 
						mProxy.shutdown(); 
					}
				}, 3600000);
	}
	@Override
	protected Notification createNotification() {
		return null;
	}
}

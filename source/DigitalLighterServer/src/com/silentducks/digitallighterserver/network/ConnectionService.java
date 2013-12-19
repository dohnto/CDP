/**
  * Digital Ligter
  * Customer Driven Project - NTNU
  * 20th November  2013
  *
  * @author Jan Bednarik
  * @author Tomas Dohnalek
  * @author Milos Jovac
  * @author Agnethe Soraa
  */

package com.silentducks.digitallighterserver.network;

import java.net.Socket;
import java.util.ArrayList;

import com.silentducks.digitallighterserver.core.ServiceObserver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ConnectionService extends Service {

	// LOGCAT TAG
	public static final String TAG = "DLService";

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	// NSD
	NsdHelper mNsdHelper;
	private Connection mConnection;
	private Handler mUpdateHandler;

	public int userCount = 0;
	public String serviceName = "";

	private ServiceObserver observer;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * Class used for the client Binder. Because we know this service always runs in the same process as its
	 * clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public ConnectionService getService() {
			// Return this instance of ConnectionService so clients can call public methods
			return ConnectionService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(ConnectionService.this, "DL connection service started ", Toast.LENGTH_SHORT).show();

		// HENDELR GETS MESSAGES FROM BACKGROUND THREADS AND MAKE MODIFICATIONS TO UI

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				// BASIC PROTOCOL THIS WILL EVOLVE.

				switch (msg.getData().getInt(Protocol.MESSAGE_TYPE)) {
				case Protocol.MESSAGE_TYPE_USER_ADDED:
					userCount++;
					break;

				case Protocol.MESSAGE_TYPE_SERVER_STARTED:
					serviceName = msg.getData().getString(Protocol.NEW_SERVICE_NAME);
					Toast.makeText(ConnectionService.this,
							"Server " + serviceName + " started on port " + mConnection.getLocalPort(),
							Toast.LENGTH_SHORT).show();
					break;
				}

				// notify activity about the change
				observer.onServiceDataUpdate();
			}
		};

		// NSD
		mConnection = new Connection(mUpdateHandler);
		mNsdHelper = new NsdHelper(this, mUpdateHandler);
		mNsdHelper.initializeNsd();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	public void pingUsers() {
		// mConnection.pingUsers();
	}

	public void registerService(final String name) {
		if (mConnection.getLocalPort() > -1) {
			int port = mConnection.getLocalPort();
			mNsdHelper.registerService(name + ":" + port, mConnection.getLocalPort());
		} else {
			Log.d(TAG, "ServerSocket isn't bound.");
			Toast.makeText(this, "Server isn't bound", Toast.LENGTH_SHORT).show();
		}
	}

	public void broadcastCommandSignal(String signal) {
		mConnection.sendMessage(signal);
	}

	public void multicastCommandSignal(ArrayList<Socket> receivers, String msg) {
		mConnection.sendMessage(receivers, msg);
	}

	public void unicastCommandSignal(Socket receiver, String msg) {
		mConnection.sendMessage(receiver, msg);
	}

	public ArrayList<Socket> getConnectedDevices() {
		return mConnection.getSockets();
	}

	public void setObserver(ServiceObserver obs) {
		observer = obs;
	}

}

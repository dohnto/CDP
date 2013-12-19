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

package com.silentducks.timesyns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.silentducks.digitallighter.ClientPlayer;
import com.silentducks.digitallighter.DLApplication;

public class SNTPClient extends AsyncTask<String, Void, Integer> {
	private static final String DEFAULT_NTP_SERVER = "0.no.pool.ntp.org";
	private static final int SNTP_PORT = 38621; // 123 real address

	private double ntpTime = 0;
	String sharedKey;

	ArrayList<Long> offsets = new ArrayList<Long>();

	@Override
	protected Integer doInBackground(String... params) {
		try {
			long diffSum = 0;
			for (int i = 0; i < 30; i++) {
				ntpTime = retrieveSNTPTime(params);
				long now = System.currentTimeMillis();
				double utc = ntpTime - (2208988800.0);

				// milliseconds
				long ms = (long) (utc * 1000.0);

				Log.d("TimeSync", "" + (ms - now));

				// if (ClientPlayer.timeOffset < ms - now)
				// ClientPlayer.timeOffset = ms - now;
				offsets.add(ms - now);
				Log.d("TStamp", "" + (now - ms));
				
				
				diffSum += ms - now;
				ClientPlayer.timeOffset = diffSum/(i+1);
			}

			ClientPlayer.timeOffset = getMean(offsets);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Integer result) {

		Toast.makeText(DLApplication.getContext(), "Time diff: " + ClientPlayer.timeOffset,
				Toast.LENGTH_SHORT).show();

		double utc = ntpTime - (2208988800.0);

		// milliseconds
		long ms = (long) (utc * 1000.0);

		// date/time
		String date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(ms));

		// fraction
		double fraction = ntpTime - ((long) ntpTime);
		String fractionSting = new DecimalFormat(".000000").format(fraction);
		/*
		 * mToast.setText("System Time:\n" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.S").format(new Date())
		 * + "\n Server Time:\n" + date + fractionSting); mToast.show();
		 */

		// Log response
		Log.d("System Time: ", new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.S").format(new Date()));
		Log.d("NTP Time: ", date + fractionSting);

		super.onPostExecute(result);
	}

	private double retrieveSNTPTime(String... params) throws SocketException, UnknownHostException,
			IOException {
		String serverName = params[0];
		DatagramSocket socket = new DatagramSocket();
		InetAddress serverAddress = InetAddress.getByName(serverName);
		byte[] buffer = new NtpMessage().toByteArray();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SNTP_PORT);

		NtpMessage
				.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

		socket.send(packet);

		packet = new DatagramPacket(buffer, buffer.length);

		socket.receive(packet);

		// Process response
		NtpMessage message = new NtpMessage(packet.getData());

		// Display response
		Log.d("NTP server: ", serverName);
		Log.d("NTP message: ", message.toString());

		socket.close();

		return message.transmitTimestamp;
	}

	private static long getMean(final ArrayList<Long> list) {
		long mean = 0;
		for (long i : list) {
			mean += i;
		}
		return mean / list.size();
	}

	private static long getMedian(ArrayList<Long> list) {
		Collections.sort(list);
		return list.get(list.size() / 2);
	}

}

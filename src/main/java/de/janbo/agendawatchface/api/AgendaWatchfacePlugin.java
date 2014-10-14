package de.janbo.agendawatchface.api;

import java.util.ArrayList;
import java.util.List;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

/**
 * The main class for plugins to implement. 
 * 
 * In order to make your plugin discoverable, you must register this class as a BroadcastReceiver in the manifest and add an intent filter for de.janbo.agendawatchface.intent.action.provider
 * Then, you simply implement the abstract methods of this class in the inheriting class.
 * To send data to the service (which will update the watch if necessary), use publishData().
 * 
 * If you want this BroadcastReceiver to handle some Broadcasts other than the AgendaWatchface ones, just override onReceive(). Just make sure to call super.onReceive() as well
 */
public abstract class AgendaWatchfacePlugin extends BroadcastReceiver {
	/**
	 * The intent action that this receiver must be registered for
	 */
	public static final String INTENT_ACTION_AGENDA_PROVIDER = "de.janbo.agendawatchface.intent.action.provider";
	
	//Internal constants
	public static final String INTENT_EXTRA_PROTOCOL_VERSION = "de.janbo.agendawatchface.intent.extra.protversion";
	public static final String INTENT_EXTRA_REQUEST_TYPE = "de.janbo.agendawatchface.intent.extra.requesttype";
	public static final int REQUEST_TYPE_DISCOVER = 1;
	public static final int REQUEST_TYPE_REFRESH = 2;
	public static final int REQUEST_TYPE_SHOW_SETTINGS = 3;
	public static final String INTENT_EXTRA_REQUEST_PLUGIN_ID = "de.janbo.agendwatchface.intent.extra.requestpluginid";
	//Intent actions for the main service
	public static final String INTENT_ACTION_ACCEPT_DISCOVER = "de.janbo.agendawatchface.intent.action.acceptdiscovery";
	public static final String MAIN_SERVICE_INTENT_EXTRA_PLUGIN_ID = "de.janbo.agendawatchface.intent.extra.pluginid";
	public static final String MAIN_SERVICE_INTENT_EXTRA_PLUGIN_NAME = "de.janbo.agendawatchface.intent.extra.pluginname";
	public static final String MAIN_SERVICE_INTENT_EXTRA_PLUGIN_VERSION = "de.janbo.agendawatchface.intent.extra.pluginprotocolversion";
	public static final String MAIN_SERVICE_INTENT_ACTION_ACCEPT_DATA = "de.janbo.agendawatchface.intent.action.acceptdata";
	public static final String MAIN_SERVICE_INTENT_EXTRA_DATA = "de.janbo.agendawatchface.intent.extra.plugindata";
	public static final String MAIN_SERVICE_INTENT_EXTRA_VIBRATE = "de.janbo.agendawatchface.intent.extra.vibrateflag";

	
	/**
	 * Version of the plugin protocol implemented
	 */
	public static final int PLUGIN_PROTOCOL_VERSION = 1;
	
	public AgendaWatchfacePlugin() {
		super();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (INTENT_ACTION_AGENDA_PROVIDER.equals(intent.getAction())) {
			if (intent.getIntExtra(INTENT_EXTRA_PROTOCOL_VERSION, 0) != PLUGIN_PROTOCOL_VERSION) {
				Log.e("AgendaWatchfacePlugin", getPluginId()+" or the app seem to be outdated");
				return;
			}
			
			switch (intent.getIntExtra(INTENT_EXTRA_REQUEST_TYPE, -1)) {
			case REQUEST_TYPE_DISCOVER:
				Log.d("AgendaWatchfacePlugin", "Discovering "+getPluginId());
				Intent reply = new Intent(INTENT_ACTION_ACCEPT_DISCOVER);
				reply.putExtra(MAIN_SERVICE_INTENT_EXTRA_PLUGIN_ID, getPluginId());
				reply.putExtra(MAIN_SERVICE_INTENT_EXTRA_PLUGIN_NAME, getPluginDisplayName());
				reply.putExtra(MAIN_SERVICE_INTENT_EXTRA_PLUGIN_VERSION, PLUGIN_PROTOCOL_VERSION);
				context.sendBroadcast(reply);
				break;
				
			case REQUEST_TYPE_REFRESH:
				Log.d("AgendaWatchfacePlugin", "Calling onRefreshRequest() on "+getPluginId()+" - expecting plugin to answer via publishData()");
				onRefreshRequest(context);
				break;
			
			case REQUEST_TYPE_SHOW_SETTINGS:
				if (intent.getStringExtra(INTENT_EXTRA_REQUEST_PLUGIN_ID).equals(getPluginId()))
					onShowSettingsRequest(context);
				break;
				
			default:
				Log.e("AgendaWatchfacePlugin", "invalid request type "+intent.getIntExtra(INTENT_EXTRA_REQUEST_TYPE, -1));
			}
		}
	}
	
	/**
	 * Sends data to the AgendaWatchfaceService. The supplied list of items will replace the previous one.
	 * Will automatically push data to the watch if it makes sense. 
	 * You may call it as often as you like (service will only update the watch if data is actually different from previous data).
	 * You may call it as seldom as you like (service will cache your old data).
	 * However, plugins are expected to publishData() after onRefreshRequest() is called (may indicate that the service lost its data).
	 * You may supply as many items as you like (although more than 10 would not make sense, since the watch (as of this writing) only displays at most 10 items)
	 * @param context Some Context (e.g., as supplied in onRefreshRequest() or get one by getApplicationContext() in an Activity/Service)
	 * @param items the list of items you want to publish. Will replace any previously published data
	 * @param vibrate if set to true, the watch will vibrate after these items have been synchronized. Has no effect if the data didn't change (note that the AgendaWatchfaceService's data can sometimes be reset (e.g., every time the Android app is entered), so don't spam it
	 */
	public void publishData(Context context, List<AgendaItem> items, boolean vibrate) {
		if (items == null)
			items = new ArrayList<AgendaItem>();
		
		Log.d("AgendaWatchfacePlugin", "publishing "+items.size()+" items for "+getPluginId());
		Intent dataIntent = new Intent(MAIN_SERVICE_INTENT_ACTION_ACCEPT_DATA);
		dataIntent.setClassName("de.janbo.agendawatchface", "de.janbo.agendawatchface.AgendaWatchfaceService");
		dataIntent.putExtra(MAIN_SERVICE_INTENT_EXTRA_PLUGIN_ID, getPluginId());
		dataIntent.putExtra(MAIN_SERVICE_INTENT_EXTRA_PLUGIN_VERSION, PLUGIN_PROTOCOL_VERSION);
		
		Parcelable[] parcelItems = new Parcelable[items.size()];
		for (int i=0; i<items.size();i++)
			parcelItems[i] = items.get(i).toBundle();
		dataIntent.putExtra(MAIN_SERVICE_INTENT_EXTRA_DATA, parcelItems);
		dataIntent.putExtra(MAIN_SERVICE_INTENT_EXTRA_VIBRATE, vibrate);

		context.startService(dataIntent);
	}
	
	/**
	 * You plugin id (should be unique and qualified with you package name, e.g. com.example.myapp.agendawatchface.plugin)
	 * @return A string uniquely identifying your plugin
	 */
	public abstract String getPluginId();
	
	/**
	 * The name of the plugin to display in the AgendaWatchface settings (e.g., "Calendar")
	 */
	public abstract String getPluginDisplayName();
	
	/**
	 * Called whenever the AgendaWatchface service requests data from the plugin. 
	 * This happens regularly (every AgendaWatchfaceService.PLUGIN_SYNC_INTERVAL (as of this writing: 30 minutes)) and whenever the service is killed and loses its cached data.
	 * The plugin should follow up this call by publishData() with its most recent data in a timely fashion (2-3 seconds). If it doesn't, its data may not be in the (first) synchronization with the watch. 
	 * However, data is still accepted at any point in time after onRefreshRequest() as well, so not answering in time is okay if necessary.
	 * 
	 * One typical thing to do here is to start a service that gathers data and then calls (new YourPlugin).publishData(). 
	 * That service may then keep running and possibly use a ContentObserver to wait for new data, then call publishData() with the new data to push it to the watch.
	 * 
	 * If you don't need push updates, or you can get notified of changes via a broadcast, you don't need a service (in the latter case, just let this BroadcastReceiver react to the notification intent, collect data in onRefreshRequest() (if it doesn't take long!) and send it directly via publishData())
	 * 
	 * @param context Context you may use (e.g., to start a service).
	 */
	public abstract void onRefreshRequest(Context context);
	
	/**
	 * Called when the user presses the plugin's entry in the AgendaWatchface settings.
	 * You should start your own activity here that presents your plugin's settings.
	 * @param context
	 */
	public abstract void onShowSettingsRequest(Context context);
}

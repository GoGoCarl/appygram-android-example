package com.appygram.android.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class MainActivity extends Activity {

	private final static String APPYGRAM_API_KEY = "your-api-key-here";

	@SuppressWarnings("unused")
	private class Topic {
		private String id;
		private String name;

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	List<Topic> topics = new ArrayList<Topic>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new GetTopicsTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		
	private class GetTopicsTask extends AsyncTask<URL, Integer, Long> {

		protected GetTopicsTask(){
			super();
		}

		protected Long doInBackground(URL... urls) {
			try{
				URL url = new URL("https://arecibo.appygram.com/topics/" + APPYGRAM_API_KEY);
				Log.i("Appygram Example","Getting topics from "+url.toString());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		        topics.clear();
		        reader.beginArray();
		        while (reader.hasNext()) {
		            Topic topic = gson.fromJson(reader, Topic.class);
		            topics.add(topic);
		        }
		        reader.endArray();
		        reader.close();
			} catch (IOException x) {
				Log.e("Appygram Example","Error sending appygram", x);
			}
			return 0L;
		}

		protected void onPostExecute(Long result) {
			ArrayList<String> names = new ArrayList<String>();
			for(Topic topic: topics){
				names.add(topic.name);
			}
			Spinner spinner = (Spinner) findViewById(R.id.editTopic);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,names.toArray(new String[0]));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
		}

	}

	private class SendAppygramTask extends AsyncTask<URL, Integer, Long> {
		private Map<String,String> params;

		protected SendAppygramTask(Map<String,String> params){
			super();
			this.params = params;
			params.put("api_key", APPYGRAM_API_KEY);
		}

		protected Long doInBackground(URL... urls) {
			Long result = 0L;
			try{
				URL url = new URL("https://arecibo.appygram.com/appygrams");
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				String input = new Gson().toJson(params);
				Log.i("Appygram Example","Sending: "+input);
				OutputStream os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();	
				os.close();
				result = (long) conn.getResponseCode();
				Log.i("Appygram Example","Appygram sent with result "+result);
			} catch (IOException x) {
				Log.e("Appygram Example","Error sending appygram", x);
			}
			return result;
		}

		protected void onPostExecute(Long result) {
			// TODO: show that something happened
		}
	}

	private String getUIString(int id){
		return ((TextView) findViewById(id)).getText().toString();
	}

	public void send(View source) {
		// Collect parameters from the UI
		Map<String, String> params = new HashMap<String, String>();
		params.put("topic", topics.get(((Spinner)findViewById(R.id.editTopic)).getSelectedItemPosition()).getName());
		params.put("name", getUIString(R.id.editName));
		params.put("email", getUIString(R.id.editEmail));
		params.put("message", getUIString(R.id.editMessage));

		// Send them to Appygram
		new SendAppygramTask(params).execute();
	}

}

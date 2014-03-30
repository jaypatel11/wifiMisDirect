// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
@SuppressLint("SdCardPath")
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            
        	
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = null; 
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            OutputStream stream = null;
            InputStream is = null;

        

            for(int i = 0 ; i < 2; i++){
        		
            	String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
                if(i == 0)
                	fileUri = Uri.parse("file:///sdcard/xyz.vcf").toString();
            
                try {
                    Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");	
                    socket = new Socket();                    

                    socket.bind(null);
                    Log.d(WiFiDirectActivity.TAG, "Client socket - bind");

                    socket.setReuseAddress(true);
                    Log.d(WiFiDirectActivity.TAG, "Client socket - reusable");
            
                    socket.connect((new InetSocketAddress(host, port)), 10000);
                    Log.d(WiFiDirectActivity.TAG, "Client socket - connect" );

                    
                    Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                    stream = socket.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    try {
                        is = cr.openInputStream(Uri.parse(fileUri));
                    } catch (FileNotFoundException e) {
                        Log.d(WiFiDirectActivity.TAG, e.toString());
                    }
                    DeviceDetailFragment.copyFile(is, stream);
                    Log.d(WiFiDirectActivity.TAG, "Client: Data written");
                } catch (IOException e) {
                    Log.e(WiFiDirectActivity.TAG, e.getMessage());
                }
                finally{
                	
                	                	if (socket != null) {
                        if (socket.isConnected()) {
                        	
                        	try {
                        		
                        		if(stream != null){
                            		stream.flush();
                            		stream.close();
                            	}
                        		if(is != null ){
                        			
                        			is.close();
                        		}
                        		
                        		socket.close();
                        		try {
            						Thread.sleep(2000);
            					} catch (InterruptedException e1) {
            						// TODO Auto-generated catch block
            						e1.printStackTrace();
            					}

                        	} catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
       
                }
            

        	}
            
        }
    }
}

package com.example.ex4android;

import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is AsyncTask which provides with tools for using Tcp connection
 * between a current app(as a client) and a server in thread-safe way(as AsyncTask
 * requires).
 */
public class ConnectTask extends AsyncTask<String, String, TcpClient> {
    //TCp client which will be connected to a server in a thread safe way.
    private TcpClient mTcpClient;

    public ConnectTask(String ip, int port) {
        super();
        //we create a TCPClient object
        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                publishProgress(message);
            }
        }, ip, port);
    }

    public TcpClient getTcpClient() {
        return mTcpClient;
    }

    /**
     * This function connects a client(mTcpClient) to a server and starts
     * listening to messages from a server.
     * @param message - isn't used here.
     * @return - null, isn't used here.
     */
    @Override
    protected TcpClient doInBackground(String... message) {
        mTcpClient.run();
        return null;
    }

}

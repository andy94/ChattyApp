package idp.andrei.chatty.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import idp.andrei.chatty.ChatActivity;

/**
 * Created by Andrei on 3/27/2017.
 */

public class SocketService extends  Service {
    ServerSocket serverSocket;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        ServerSocketThread serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();


        return Service.START_STICKY;
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(8080);

                while (true) {
                    socket = serverSocket.accept();
                    FileTxThread fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {

            try {

                // Receive file path:
                InputStream is = socket.getInputStream();
                byte[] bytes = new byte[1024];
                int rb  = is.read(bytes, 0, bytes.length);
                byte[] rbytes = new byte[rb];
                for(int i = 0 ; i < rb; ++i){
                    rbytes[i] = bytes[i];
                }
                String path = new String(rbytes);
//                User.firebaseReference.child("mesg").child(User.id).child("client").setValue(path+" "+System.currentTimeMillis());
                File file = new File(path);
//                User.firebaseReference.child("mesg").child(User.id).child("file").setValue((int) file.length()+" "+System.currentTimeMillis());

                int fileLen = (int)file.length();

                // Send the file size
                OutputStream os = socket.getOutputStream();

                DataOutputStream dos = new DataOutputStream(os);

                dos.writeInt((int) file.length());
                dos.flush();

                if(fileLen == 0){
                    socket.close();
                    return;
                }

                BufferedInputStream bis;
                bis = new BufferedInputStream(new FileInputStream(file));

                int bytesRead = 0;
                os = socket.getOutputStream();
                byte[] fileBytes = new byte[1024];

                // Send file
                while(bytesRead< fileLen){
                    int val  = bis.read(fileBytes, 0, fileBytes.length);
                    os.write(fileBytes, 0, fileBytes.length);
                    os.flush();
                    bytesRead += val;
                }

                os.close();
                is.close();
                dos.close();
                bis.close();

                socket.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




}



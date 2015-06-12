package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    ArrayList<String> REMOTE_PORTS= new ArrayList<String>(){{
        add("11108");
        add("11112");
        add("11116");
        add("11120");
        add("11124");
    }};
    static final int SERVER_PORT = 10000;
    int count=0;
    int fifo_count=0;
    HashMap<String, Message> message_identifier_queue= new HashMap<String, Message>();
    //SortedMap hold_back_queue= Collections.synchronizedSortedMap( new TreeMap<Double, Message>());
    TreeMap<Double, Message>hold_back_queue =  new TreeMap<Double, Message>();
    HashMap<String, ArrayList<Double>> number_list= new HashMap<String, ArrayList<Double>>();
    HashMap<String, Integer> for_fifo= new HashMap<String, Integer>(){{put("11108",0);put("11112",0);put("11116",0);put("11120",0);put("11124",0);}};
    HashMap<String, ArrayList<Message>> fifo_hold= new HashMap<String, ArrayList<Message>>();
    ArrayList<String> timed_up= new ArrayList<String>();
    HashMap<String, Timer> timer_for1= new HashMap<String, Timer>();
    HashMap<String, Timer> timer_for2= new HashMap<String, Timer>();
    Timer timer;
    Random rand= new Random();
    int randNumber;
    ReentrantLock lock = new ReentrantLock();
    //private final ContentValues[] mContentValues;
    ContentResolver mContentResolver;
    TextView mTextView;
    private final Uri mUri= buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

    String myPort;


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){

                        String msg = editText.getText().toString() + "\n";
                        Message message_from_UI= new Message();
                        message_from_UI.setSender("1");
                        int id= rand.nextInt(100000) + 10000;
                        message_from_UI.setIdentifier(Integer.toString(id)+"-"+myPort);
                        message_from_UI.setMy_port(myPort);
                        message_from_UI.setMessage(msg);
                        message_from_UI.setProposal(0.0);
                        message_from_UI.setDeliverable(false);
                        message_from_UI.setMessage_number(fifo_count);
                        editText.setText(""); // This is one way to reset the input box.
                        TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                        localTextView.append("\t" + msg); // This is one way to display a string.
                        TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                        remoteTextView.append("\n");
                        fifo_count++;
                        // mContentResolver= getContentResolver();
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message_from_UI, null );


                    }});
    }

   /* private class hash{
        public TreeMap<Double, Message>hold_back_queue =  new TreeMap<Double, Message>();

        public synchronized TreeMap<Double, Message> getMap(){
            if(lock.isHeldByCurrentThread()){
                return hold_back_queue;
            }
            else{
               return null;
            }
        }
    }*/

   // hash hash_map= new hash();

   /* private class RemindTask extends TimerTask {

        Message m;
        String id;
        public RemindTask(Message m, String id){
            this.id= id;
            this.m= m;
        }
        public void run() {

                System.out.println("Time's up!");
                if(id== "1"){
                    timed_up.add(m.getIdentifier());
                    Calling calling= new Calling();

                    calling.message2(m);
                }
                else if(id=="2"){
                    if(hold_back_queue!=null){
                        for(Map.Entry<Double, Message> entry : hold_back_queue.entrySet()) {
                            if (entry.getValue().equals(m)) {
                                if (!entry.getValue().isDeliverable()) {
                                    hold_back_queue.remove(entry);
                                }
                            }

                        }

                    }

            }

            //timer.cancel(); //Not necessary because we call System.exi    t
           // System.exit(0); //Stops the AWT thread (and everything else)
        }
    }*/

    private class ServerTask extends AsyncTask<ServerSocket, Message, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            /* Below logic is taken from http:/ /docs.oracle.com/javase/tutorial/networking/sockets/index.html and
            http://developer.android.com/reference/android/os/AsyncTask.html */
            try {


                while(true) {
                 Socket socketS= serverSocket.accept();
                    ObjectInputStream inFromClient = new ObjectInputStream(socketS.getInputStream());
                    //BufferedReader brReader = new BufferedReader(new InputStreamReader(socketS.getInputStream()));

                            Message m= (Message) inFromClient.readObject();
                            String message;
                            Calling calling= new Calling();

                            ArrayList<Message> message_list= calling.calling_method(m);
                            if(message_list!=null){
                                for(int i=0; i<message_list.size();i++){
                                    publishProgress(message_list.get(i));
                                }

                            }




                  }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Message...msgs) {
            /*
             * The following code displays what is received in doInBackground().
             */



            String strReceived = msgs[0].getMessage();

            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");

            ContentValues[] cv = new ContentValues[1];
            cv[0] = new ContentValues();
            //double final_key= hold_back_queue.firstKey();
            cv[0].put("key", count++);
            cv[0].put("value", strReceived);
            getContentResolver().insert(mUri, cv[0]);

            return;
        }

    }
    private class Calling{

        public ArrayList<Message> calling_method(Message m) {
            ArrayList<Message> list= new ArrayList<Message>();


                if (m != null) {
                    try {
                        Log.e("Enter Sever Socket", m.getMy_port());
                        String message_sent = m.getMessage();

                        if (m.getSender().equals("2")) {

                            message2(m);
                        } else if (m.getSender().equals("1")) {

                            message1(m);

                        } else if (m.getSender().equals("3")) {
                            Message final_message = message_identifier_queue.get(m.getIdentifier());
                            final_message.setDeliverable(true);
                            /*if (timer_for2.get(m.getIdentifier()) != null)
                                timer_for2.get(m.getIdentifier()).cancel();*/

                                for (Map.Entry<Double, Message> entry : hold_back_queue.entrySet()) {
                                    if (entry.getValue() == final_message) {
                                        if (m.getProposal() == entry.getKey()) {
                                            hold_back_queue.put(entry.getKey(), final_message);
                                        } else {
                                            hold_back_queue.remove(entry.getKey());
                                            double final_proposal = m.getProposal();
                                            //double new_proposal= Double.parseDouble(final_proposal);
                                            hold_back_queue.put(final_proposal, final_message);
                                        }

                                    }

                                }
                                if(hold_back_queue.size()!=0){
                                    while (hold_back_queue.get(hold_back_queue.firstKey()).isDeliverable()) {
                                        list.add(hold_back_queue.get(hold_back_queue.firstKey()));
                                        hold_back_queue.remove(hold_back_queue.firstKey());
                                        if (hold_back_queue.size() == 0) {
                                            break;
                                        }
                                    }
                                }
                            }





                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


                return list;


        }

        public void message2 (Message m){

                try{
                    String identifier = m.getIdentifier();
                    double proposed_number = m.getProposal();
                    if (number_list.containsKey(identifier)) {
                        ArrayList<Double> proposal_numbers = number_list.get(identifier);
                        proposal_numbers.add(proposed_number);
                        if ((proposal_numbers.size() == 5)) {
                            /*Timer timer1= timer_for1.get(m.getIdentifier());
                            if(timer1 != null)
                                timer_for1.get(m.getIdentifier()).cancel();*/
                            Collections.sort(proposal_numbers);
                            Message m_for_final = new Message();
                            m_for_final.setIdentifier(identifier);
                            m_for_final.setSender("3");
                            m_for_final.setMy_port(myPort);
                            m_for_final.setMessage(null);
                            m_for_final.setProposal(proposal_numbers.get(proposal_numbers.size() - 1));
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m_for_final, null);
                            //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, final_number,null);
                        }/* else if (timed_up.contains(m.getIdentifier())) {
                            Collections.sort(proposal_numbers);
                            Message m_for_final = new Message();
                            m_for_final.setIdentifier(identifier);
                            m_for_final.setSender("3");
                            m_for_final.setMy_port(myPort);
                            m_for_final.setMessage(null);
                            m_for_final.setProposal(proposal_numbers.get(proposal_numbers.size() - 1));
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m_for_final, null);
                            //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, final_number,null)
                        }*/
                    } else {
                        ArrayList<Double> proposal_numbers = new ArrayList<Double>();
                        proposal_numbers.add(proposed_number);
                        number_list.put(identifier, proposal_numbers);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }

        }
        public void message1(Message m) {
                try{
                    int proposal = 0;
                    if (myPort == m.getMy_port()) {
                        if (hold_back_queue.size() != 0) {
                            double hbq_last = hold_back_queue.lastKey();
                            if (count > hbq_last) {
                                proposal = count + 1;
                            } else {
                                proposal = (int) hbq_last + 1;
                            }
                        } else {
                            proposal = proposal + 1;
                        }
                        message_identifier_queue.put(m.getIdentifier(), m);

                        //Socket newSocket1 = new Socket(socketS.getInetAddress(),socketS.getPort());
                        String message_for_proposal = Integer.toString(proposal) + "." + myPort;
                        Double object_value = (Double) Double.parseDouble(message_for_proposal);
                        double proposal_number = object_value.doubleValue();
                        hold_back_queue.put(proposal_number, m);
                        Message m_for_proposal = new Message();
                        m_for_proposal.setSender("2");
                        m_for_proposal.setProposal(proposal_number);
                        m_for_proposal.setMessage(m.getMessage());
                        m_for_proposal.setIdentifier(m.getIdentifier());
                        m_for_proposal.setMy_port(m.getMy_port());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m_for_proposal, null);
                    } else {
                        if (for_fifo.get(m.getMy_port()) < m.getMessage_number()) {
                            Log.e("for_fifo", "" + for_fifo.get(m.getMy_port()));
                            if (fifo_hold.containsKey(m.getMy_port())) {
                                ArrayList<Message> message_queue = fifo_hold.get(m.getMy_port());
                                message_queue.add(m);
                                fifo_hold.put(m.getMy_port(), message_queue);

                            } else {
                                ArrayList<Message> message_queue = new ArrayList<Message>();
                                message_queue.add(m);
                                fifo_hold.put(m.getMy_port(), message_queue);
                            }

                        } else if (for_fifo.get(m.getMy_port()) == m.getMessage_number()) {
                            int message_count = for_fifo.get(m.getMy_port());
                            for_fifo.put(m.getMy_port(), message_count + 1);
                            if (hold_back_queue.size() != 0) {
                                double hbq_last = hold_back_queue.lastKey();
                                if (count > hbq_last) {
                                    proposal = count + 1;
                                } else {
                                    proposal = (int) hbq_last + 1;
                                }
                            } else {
                                proposal = proposal + 1;
                            }
                            message_identifier_queue.put(m.getIdentifier(), m);

                            //Socket newSocket1 = new Socket(socketS.getInetAddress(),socketS.getPort());
                            String message_for_proposal = Integer.toString(proposal) + "." + myPort;
                            Double object_value = (Double) Double.parseDouble(message_for_proposal);
                            double proposal_number = object_value.doubleValue();
                            hold_back_queue.put(proposal_number, m);
                            Message m_for_proposal = new Message();
                            m_for_proposal.setSender("2");
                            m_for_proposal.setProposal(proposal_number);
                            m_for_proposal.setMessage(m.getMessage());
                            m_for_proposal.setIdentifier(m.getIdentifier());
                            m_for_proposal.setMy_port(m.getMy_port());
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m_for_proposal, null);
                            if (fifo_hold.containsKey(m.getMy_port())) {
                                ArrayList<Message> m_for_updating = fifo_hold.get(m.getMy_port());
                                if (m_for_updating != null || m_for_updating.size() != 0) {
                                    for (int i = 0; i < m_for_updating.size(); i++) {
                                        if (m_for_updating.get(i).getMessage_number() == for_fifo.get(m.getMy_port())) {
                                            message_count = for_fifo.get(m.getMy_port());
                                            for_fifo.put(m.getMy_port(), message_count + 1);
                                            Log.e("Inside While", for_fifo.get(m.getMy_port()) + "");
                                            if (hold_back_queue.size() != 0) {
                                                double hbq_last = hold_back_queue.lastKey();
                                                if (count > hbq_last) {
                                                    proposal = count + 1;
                                                } else {
                                                    proposal = (int) hbq_last + 1;
                                                }
                                            } else {
                                                proposal = proposal + 1;
                                            }
                                            message_identifier_queue.put(m.getIdentifier(), m);

                                            //Socket newSocket1 = new Socket(socketS.getInetAddress(),socketS.getPort());
                                            message_for_proposal = Integer.toString(proposal) + "." + myPort;
                                            object_value = (Double) Double.parseDouble(message_for_proposal);
                                            proposal_number = object_value.doubleValue();
                                            hold_back_queue.put(proposal_number, m);
                                            m_for_proposal = new Message();
                                            m_for_proposal.setSender("2");
                                            m_for_proposal.setProposal(proposal_number);
                                            m_for_proposal.setMessage(m.getMessage());
                                            m_for_proposal.setIdentifier(m.getIdentifier());
                                            m_for_proposal.setMy_port(m.getMy_port());
                                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m_for_proposal, null);
                                        }

                                    }
                                }

                                if (m_for_updating.size() == 0)
                                    fifo_hold.remove(m.getMy_port());
                            }

                        }

                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }

        }
    }
    private class ClientTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... msgs) {
            String failed="";

//                String remotePort = REMOTE_PORT0;
//                if (msgs[1].equals(REMOTE_PORT0))
//                    remotePort = REMOTE_PORT1;
                if(msgs[0].getSender().equals("2")){
                  try{
                      /*timer= new Timer(msgs[0].getIdentifier());
                      timer.schedule(new RemindTask(msgs[0], msgs[0].getSender()), 30000);
                      timer_for2.put(msgs[0].getIdentifier(), timer);*/
                      failed= msgs[0].getMy_port();
                      Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                              Integer.parseInt(msgs[0].getMy_port()));
                     // socket.setSoTimeout(500);

                      ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                      msgs[0].setMy_port(myPort);
                      outToServer.writeObject(msgs[0]);
                      outToServer.close();
                      socket.close();
                  }
                  catch (SocketTimeoutException e){
                      if(failed != ""){
                          if(REMOTE_PORTS.contains(failed))
                          REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                      }

                  }
                  catch (UnknownHostException e) {
                      Log.e(TAG, "ClientTask UnknownHostException");
                  } catch (IOException e) {
                      Log.e(TAG, "ClientTask socket IOException");
                      if(failed != ""){
                          if(REMOTE_PORTS.contains(failed))
                          REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                      }
                  }

                }
                else if(msgs[0].getSender().equals("1")){
                    /*timer= new Timer(msgs[0].getIdentifier());
                    timer.schedule(new RemindTask(msgs[0], msgs[0].getSender()),30000);
                    timer_for1.put(msgs[0].getIdentifier(), timer);*/
                   for(int i=0; i<REMOTE_PORTS.size(); i++){
                    try {

                        failed= REMOTE_PORTS.get(i);
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(REMOTE_PORTS.get(i)));
                        ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                        outToServer.writeObject(msgs[0]);
                        /*String msgToSend;
                        PrintWriter printOnServer= new PrintWriter(socket.getOutputStream());
                        printOnServer.write(msgToSend);
                        printOnServer.close();*/
                        outToServer.close();
                        socket.close();
                    }
                    catch (SocketTimeoutException e){
                        if(failed != ""){
                            if(REMOTE_PORTS.contains(failed))
                            REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                        }

                    }
                    catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                        if(failed != ""){
                            if(REMOTE_PORTS.contains(failed))
                            REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                        }
                    }
                }


            }
            else if((msgs[0].getSender().equals("3"))){
                    for(int i=0; i<REMOTE_PORTS.size(); i++){
                        try {
                            failed= REMOTE_PORTS.get(i);
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(REMOTE_PORTS.get(i)));
                            ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                            outToServer.writeObject(msgs[0]);
                        /*String msgToSend;
                        PrintWriter printOnServer= new PrintWriter(socket.getOutputStream());
                        printOnServer.write(msgToSend);
                        printOnServer.close();*/
                            outToServer.close();
                            socket.close();
                        }
                        catch (SocketTimeoutException e){
                            if(failed != ""){
                                if(REMOTE_PORTS.contains(failed))
                                REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                            }

                        }
                        catch (UnknownHostException e) {
                            Log.e(TAG, "ClientTask UnknownHostException");
                        } catch (IOException e) {
                            Log.e(TAG, "ClientTask socket IOException");
                            if(failed != ""){
                                if(REMOTE_PORTS.contains(failed))
                                REMOTE_PORTS.remove(REMOTE_PORTS.indexOf(failed));
                            }
                        }
                    }

                }

            return null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}

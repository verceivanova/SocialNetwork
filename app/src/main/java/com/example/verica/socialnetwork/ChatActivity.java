package com.example.verica.socialnetwork;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verica.socialnetwork.Models.UserModel;
import com.example.verica.socialnetwork.Utils.NotificationAsync;
import com.example.verica.socialnetwork.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private Toolbar ChattoolBar;
    private ImageButton SendMessageButton, SendImagefileButton;
    private EditText userMessageInput;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private String messageReceiverID,messageReceiverName, messageSenderID,saveCurrentDate,saveCurrentTime;
    private TextView receiverName;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;

    UserModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        RootRef=FirebaseDatabase.getInstance().getReference();
        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("userName").toString();
        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();



        getUserObjectFromDb();

        InitializeFields();
        DisplayReceiverInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();

            }
        });

        FetchMessages();
    }

    private void getUserObjectFromDb() {
        RootRef.child("Users").child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                     model=dataSnapshot.getValue(UserModel.class);
                    if(model!=null){

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchMessages()
    {
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        if(dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();

                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void SendMessage()
    {


        final String messageText=userMessageInput.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please type a message first...",Toast.LENGTH_SHORT).show();
        }
        else
            {
          String message_sender_ref="Messages/"+messageSenderID+"/"+messageReceiverID;
          String message_receiver_ref="Messages/"+messageReceiverID+"/"+messageSenderID;

          DatabaseReference user_message_key=RootRef.child("Messages").child(messageSenderID)
                  .child(messageReceiverID).push();

          String message_push_id=user_message_key.getKey();

                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate=currentDate.format(calFordDate.getTime());

                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                saveCurrentTime=currentTime.format(calFordDate.getTime());

                Map messageTextBody=new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);

                Map messageBodyDetails=new HashMap();
                messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
                messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            NotificationAsync notificationAsync = new NotificationAsync(ChatActivity.this);
                            String NotificationTitle = "New message from " + SharedPrefs.getFullName();
                            String NotificationMessage = "Message: "+messageText ;
                            notificationAsync.execute("ali", model.getFcmKey(),
                                    NotificationTitle, NotificationMessage, "Chat",messageSenderID,SharedPrefs.getFullName()+" ");
                          Toast.makeText(ChatActivity.this,"Message send Successfully",Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(ChatActivity.this,"Error: "+message,Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }


                    }
                });



            }
    }

    private void DisplayReceiverInfo()
    {
        receiverName.setText(messageReceiverName);
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists()){
                    final String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields()
    {
        ChattoolBar=(Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChattoolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        receiverName=(TextView)findViewById(R.id.custom_profile_name);
        receiverProfileImage=(CircleImageView)findViewById(R.id.custom_profile_image) ;


        SendMessageButton=(ImageButton)findViewById(R.id.send_message_button);
        SendImagefileButton=(ImageButton)findViewById(R.id.send_image_file_button);
        userMessageInput=(EditText)findViewById(R.id.input_message);

        messagesAdapter=new MessagesAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.messeges_list_users);
        
        linearLayoutManager= new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);


    }
}

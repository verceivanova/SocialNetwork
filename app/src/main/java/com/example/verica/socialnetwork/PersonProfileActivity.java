package com.example.verica.socialnetwork;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.verica.socialnetwork.Models.UserModel;
import com.example.verica.socialnetwork.Utils.NotificationAsync;
import com.example.verica.socialnetwork.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFirendRequestbutton, DeclineFriendRequestbutton;

    private DatabaseReference FriendRequestRef, UsersRef,FriendsRef;
    private FirebaseAuth mAuth;
    UserModel model;

    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");

        IntializeFields();
        getUserObjectFromDb();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String  myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String  myUserName = dataSnapshot.child("username").getValue().toString();
                    String  myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String  myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String  myDOB = dataSnapshot.child("dob").getValue().toString();
                    String  myCountry = dataSnapshot.child("country").getValue().toString();
                    String  myGender = dataSnapshot.child("gender").getValue().toString();
                    String  myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@"+myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB:"+myDOB);
                    userCountry.setText("Country:"+myCountry);
                    userGender.setText("Gender:"+myGender);
                    userRelation.setText("Relationship:"+myRelationStatus);


                    MaintananceofButtons();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestbutton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendFirendRequestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFirendRequestbutton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();

                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();

                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }



                }
            });

        }
        else
            {
                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                SendFirendRequestbutton.setVisibility(View.INVISIBLE);
            }
    }

    private void UnFriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        { NotificationAsync notificationAsync = new NotificationAsync(PersonProfileActivity.this);
                            String NotificationTitle = SharedPrefs.getFullName()+" unfriended you";
                            String NotificationMessage = "" ;
                            notificationAsync.execute("ali", model.getFcmKey(),
                                    NotificationTitle, NotificationMessage, "Request",senderUserId,SharedPrefs.getFullName()+" ");

                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFirendRequestbutton.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendFirendRequestbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });


    }
    private void getUserObjectFromDb() {
        UsersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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
    private void AcceptFriendRequest()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            NotificationAsync notificationAsync = new NotificationAsync(PersonProfileActivity.this);
                            String NotificationTitle = SharedPrefs.getFullName()+" accepted your friend request";
                            String NotificationMessage = "" ;
                            notificationAsync.execute("ali", model.getFcmKey(),
                                    NotificationTitle, NotificationMessage, "Request",senderUserId,SharedPrefs.getFullName()+" ");

                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {

                                                                                        SendFirendRequestbutton.setEnabled(true);
                                                                                        CURRENT_STATE="friends";
                                                                                        SendFirendRequestbutton.setText("Unfriend this Person");

                                                                                        DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestbutton.setEnabled(false);
                                                                                    }

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            NotificationAsync notificationAsync = new NotificationAsync(PersonProfileActivity.this);
                            String NotificationTitle = SharedPrefs.getFullName()+" did not accepted your friend request";
                            String NotificationMessage = "" ;
                            notificationAsync.execute("ali", model.getFcmKey(),
                                    NotificationTitle, NotificationMessage, "Request",senderUserId,SharedPrefs.getFullName()+" ");

                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFirendRequestbutton.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendFirendRequestbutton.setText("Send Friend Request");

                                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestbutton.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });

    }

    private void MaintananceofButtons()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE="request_sent";
                                SendFirendRequestbutton.setText("Cancel Friend request");

                                DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestbutton.setEnabled(false);
                            }
                            else if(request_type.equals("received"))
                            {
                               CURRENT_STATE="request_received" ;
                               SendFirendRequestbutton.setText("Accept friend Request");

                               DeclineFriendRequestbutton.setVisibility(View.VISIBLE);
                               DeclineFriendRequestbutton.setEnabled(true);

                               DeclineFriendRequestbutton.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v)
                                   {
                                      CancelFriendRequest();
                                   }
                               });
                            }
                        }
                        else {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserId))
                                            {
                                             CURRENT_STATE="friends" ;
                                             SendFirendRequestbutton.setText("Unfriend this Person");

                                             DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                             DeclineFriendRequestbutton.setEnabled(false);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToaPerson()
    {
     FriendRequestRef.child(senderUserId).child(receiverUserId)
             .child("request_type").setValue("sent")
             .addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task)
                 {
                   if(task.isSuccessful())
                   {
                       NotificationAsync notificationAsync = new NotificationAsync(PersonProfileActivity.this);
                       String NotificationTitle = "New Friend Request from " + SharedPrefs.getFullName();
                       String NotificationMessage = "Open app to accept" ;
                       notificationAsync.execute("ali", model.getFcmKey(),
                               NotificationTitle, NotificationMessage, "Request",senderUserId,SharedPrefs.getFullName()+" ");
                       FriendRequestRef.child(receiverUserId).child(senderUserId)
                               .child("request_type").setValue("received")
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task)
                                   {
                                       if(task.isSuccessful())
                                       {
                                          SendFirendRequestbutton.setEnabled(true);
                                          CURRENT_STATE="request_sent";
                                          SendFirendRequestbutton.setText("Cancel friend Request");

                                          DeclineFriendRequestbutton.setVisibility(View.INVISIBLE);
                                          DeclineFriendRequestbutton.setEnabled(false);
                                       }

                                   }
                               });
                   }
                 }
             });
    }

    private void IntializeFields()
    {
        userName =(TextView) findViewById(R.id.person_username);
        userProfName =(TextView)findViewById(R.id.person_full_name);
        userStatus =(TextView)findViewById(R.id.person_profile_status);
        userCountry =(TextView)findViewById(R.id.person_country);
        userGender =(TextView)findViewById(R.id.person_gender);
        userRelation =(TextView)findViewById(R.id.person_relationship_status);
        userDOB =(TextView)findViewById(R.id.person_dob);
        userProfileImage =(CircleImageView)findViewById(R.id.person_profile_pic);


        SendFirendRequestbutton=(Button)findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestbutton=(Button)findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE="not_friends";

    }
}

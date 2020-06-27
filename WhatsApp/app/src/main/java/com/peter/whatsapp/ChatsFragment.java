package com.peter.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.whatsapp.model.Friends;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{
    private View chatsView;
    private RecyclerView chatsUserList;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        chatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsUserList = chatsView.findViewById(R.id.chats_users_list);
        chatsUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsUserList.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return chatsView;
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView userProfileImage;
        TextView userName, userStatus;
        ImageView onlineStatus;


        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_username);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineStatus = itemView.findViewById(R.id.user_online_status);

        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(contactsRef, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, ContactsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Friends model)
                    {
                        final String usersIDS = getRef(position).getKey();
                        final String[] profileImage = {"default_image"};


                        usersRef.child(usersIDS).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {

                                if (dataSnapshot.hasChild("image"))
                                {
                                    profileImage[0] = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(profileImage[0]).placeholder(R.drawable.profile_image).into(holder.userProfileImage);


                                }

                                final String uName = dataSnapshot.child("name").getValue().toString();
                                String uStatus = dataSnapshot.child("status").getValue().toString();


                                holder.userName.setText(uName);


                                if (dataSnapshot.child("User State").hasChild("state"))
                                {
                                    String stateType = dataSnapshot.child("User State").child("state").getValue().toString();
                                    String date = dataSnapshot.child("User State").child("date").getValue().toString();
                                    String time = dataSnapshot.child("User State").child("time").getValue().toString();

                                    if (stateType.equals("online"))
                                    {
                                        holder.userStatus.setText("online");

                                        ImageView onlineStatusBtn = holder.itemView.findViewById(R.id.user_online_status);
                                        onlineStatusBtn.setVisibility(View.VISIBLE);

                                    }
                                    else if (stateType.equals("offline"))
                                    {
                                        ImageView onlineStatusBtn = holder.itemView.findViewById(R.id.user_online_status);
                                        onlineStatusBtn.setVisibility(View.INVISIBLE);


                                        holder.userStatus.setText("Last Seen: "+" "+ date+" " +time);
                                    }


                                }
                                else
                                {
                                    holder.userStatus.setText("offline");
                                }

                                    holder.itemView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("UserID", usersIDS);
                                            chatIntent.putExtra("UserName", uName);
                                            chatIntent.putExtra("ProfileImage", profileImage[0]);
                                            startActivity(chatIntent);

                                        }
                                    });

                                }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        chatsUserList.setAdapter(adapter);
        adapter.startListening();
    }
}

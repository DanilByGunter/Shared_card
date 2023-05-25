package com.project.shared_card.activity.main_screen.group;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.shared_card.R;
import com.project.shared_card.activity.converter.DateConverter;
import com.project.shared_card.activity.converter.DbBitmapUtility;
import com.project.shared_card.activity.converter.ModelConverter;
import com.project.shared_card.activity.main_screen.group.dialog.DialogEdit;
import com.project.shared_card.activity.main_screen.group.dialog.DialogGroupJoin;
import com.project.shared_card.database.ImplDB;
import com.project.shared_card.database.entity.group.GroupEntity;
import com.project.shared_card.database.entity.group_name.AllGroups;
import com.project.shared_card.database.entity.group_name.GroupNameEntity;
import com.project.shared_card.database.entity.user_name.UserNameEntity;
import com.project.shared_card.retrofit.RetrofitService;
import com.project.shared_card.retrofit.api.GroupIdApi;
import com.project.shared_card.retrofit.api.TheAllGroupApi;
import com.project.shared_card.retrofit.api.UserApi;
import com.project.shared_card.retrofit.model.TheAllGroup;
import com.project.shared_card.retrofit.model.TheGroupId;
import com.project.shared_card.retrofit.model.User;
import com.project.shared_card.retrofit.model.dto.TheAllGroupWithUser;
import com.project.shared_card.retrofit.model.dto.TheAllGroupWithUserId;
import com.project.shared_card.retrofit.model.dto.UserWithGroup;
import com.project.shared_card.retrofit.model.dto.UsersGroup;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFragment extends Fragment {
    DialogEdit dialogUser;
    DialogEdit dialogGroup;
    DialogEdit dialogCreateGroup;
    DialogGroupJoin dialogGroupJoin;
    ExpandableListView expandableListView;
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences settings;
    View mainToolBar;
    ImageView imageMe;
    AdapterForExpendList adapter;
    TextView textNameMe;
    Button editProfile;
    Button groupJoin;
    Button groupCreate;
    String USER_PATH;
    String GROUP_USER_PATH;
    String GROUP_CREATE_PATH;
    ImplDB db;
    AdapterForExpendList.updateExpandableListView updateExpandableListView;
    ActivityResultLauncher<String> getContentForEdit;
    ActivityResultLauncher<String> getContentForCreate;
    RetrofitService server;
    public GroupFragment() {
    }

    public GroupFragment(View viewById) {
        mainToolBar = viewById;
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        textNameMe.setText(settings.getString(getString(R.string.key_for_me_name),"no_name"));
        imageMe.setImageURI(Uri.parse(USER_PATH));

        groupJoin.setOnClickListener(this::clickOnJoinGroup);
        groupCreate.setOnClickListener(this::clickOnCreateGroup);
        editProfile.setOnClickListener(this::clickOnEditProfile);
        dialogUser.ready.setOnClickListener(this::clickOnSaveDialogEdit);
        dialogGroupJoin.ready.setOnClickListener(this::clickOnButtonJoinGroup);
        dialogCreateGroup.ready.setOnClickListener(this::clickOnButtonCreateGroup);
        updateAdapter();

    }
    void updateAdapter(){
        db.group_name().getAllGroups().observe((LifecycleOwner) getContext(), new Observer<List<AllGroups>>() {
            @Override
            public void onChanged(List<AllGroups> allGroups) {
                adapter = new AdapterForExpendList(getContext(),allGroups,dialogGroup,mainToolBar,updateExpandableListView);
                expandableListView.setAdapter(adapter);
            }
        });
    }


    void init(View view){
        expandableListView = view.findViewById(R.id.group_expand_list);
        settings = getContext().getSharedPreferences(getString(R.string.key_for_shared_preference), Context.MODE_PRIVATE);
        prefEditor = settings.edit();
        db = new ImplDB(getContext());
        server = new RetrofitService();
        textNameMe = view.findViewById(R.id.group_head_name);
        imageMe = view.findViewById(R.id.group_head_image);
        editProfile = view.findViewById(R.id.user_edit);
        groupJoin = view.findViewById(R.id.group_join);
        groupCreate = view.findViewById(R.id.group_create);

        USER_PATH = getContext().getFilesDir() + "/user/"  + getString(R.string.me_id);
        GROUP_USER_PATH = getContext().getFilesDir() + "/group/" +getString(R.string.me_id);

        getContentForEdit = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onActivityResultForEdit);
        getContentForCreate = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onActivityResultForCreate);
        dialogUser = new DialogEdit(getContext(), getContentForEdit);
        dialogGroup = new DialogEdit(getContext(), getContentForEdit);
        dialogCreateGroup = new DialogEdit(getContext(), getContentForCreate);
        dialogGroupJoin = new DialogGroupJoin(getContext());

        updateExpandableListView = new AdapterForExpendList.updateExpandableListView() {
            @Override
            public void update(String name, Drawable image) {
                adapter = (AdapterForExpendList) expandableListView.getExpandableListAdapter();
                adapter.groupName.setText(name);
                adapter.groupImage.setImageDrawable(image);
                expandableListView.setAdapter(adapter);
            }
        };
    }

    private void onActivityResultForEdit(Uri result) {
        dialogUser.image.setImageURI(result);
        dialogGroup.image.setImageURI(result);
    }
    private void onActivityResultForCreate(Uri result) {
        dialogCreateGroup.image.setImageURI(result);
    }

    private void clickOnJoinGroup(View v){
        dialogGroupJoin.dialog.show();
    }
    private void clickOnCreateGroup(View v){
        dialogCreateGroup.name.setHint(R.string.enter_your_group);
        dialogCreateGroup.ready.setText(R.string.create_group);
        dialogCreateGroup.dialog.show();
    }


    private void clickOnEditProfile(View v){
        dialogUser.name.setText(settings.getString(getString(R.string.key_for_me_name),"XD"));
        dialogUser.image.setImageURI(Uri.parse(USER_PATH));
        dialogUser.dialog.show();
    }
    private void clickOnSaveDialogEdit(View v){
        if(!dialogUser.name.getText().toString().equals("")){
            prefEditor.putString(getString(R.string.key_for_me_name), dialogUser.name.getText().toString()).apply();

            db.user_name().updateMe(dialogUser.name.getText().toString());
            db.group_name().updateMe(dialogUser.name.getText().toString());

            byte[] picture;
            if (dialogUser.image.getDrawable()==null){
                dialogUser.image.setImageDrawable(getContext().getDrawable(R.drawable.defaul_avatar));
                picture = DbBitmapUtility.getBytes(((BitmapDrawable) getContext().getDrawable(R.drawable.defaul_avatar)).getBitmap());
            }
            else {
                picture = DbBitmapUtility.getBytes(((BitmapDrawable) dialogUser.image.getDrawable().getCurrent()).getBitmap());
            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(USER_PATH);
                        fos.write(picture);
                        fos = new FileOutputStream(GROUP_USER_PATH);
                        fos.write(picture);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            imageMe.setImageDrawable(dialogUser.image.getDrawable());
            textNameMe.setText(dialogUser.name.getText());

            if (settings.getString(getString(R.string.key_for_select_group_id),"XD").equals(getString(R.string.me_id))){
                TextView name = mainToolBar.findViewById(R.id.main_name_group);
                ImageView image = mainToolBar.findViewById(R.id.main_image_group);
                name.setText(dialogUser.name.getText());
                image.setImageDrawable(dialogUser.image.getDrawable());
            }
            dialogUser.dialog.dismiss();
        }
    }


    private void clickOnButtonJoinGroup(View  v){
        if(!dialogGroupJoin.name.getText().toString().equals("")){
            String idGroup = dialogGroupJoin.name.getText().toString().toLowerCase().split("#")[0];
            for(char s:idGroup.toCharArray())
                if(!Character.isDigit(s)){
                    Toast toast = Toast.makeText(getContext(),"Некоректный id",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

            long id_group = Long.parseLong(idGroup);
            String name = dialogGroupJoin.name.getText().toString().split("#")[1];

            boolean flag = true;
            for(AllGroups allGroups:adapter.groups)
                if(allGroups.groupName.getId() ==id_group)
                    flag=false;

            if(flag){
                String id_user = settings.getString(getString(R.string.key_for_me_id_server),"no_id");
                if(id_user.equals("no_id")){
                    getMeIdForServer(id_group,name,null,null,true);
                }
                else{
                    addUserInGroup(new UserWithGroup(id_group, name, Long.parseLong(id_user)));
                }
            }
            else{
                Toast toast = Toast.makeText(getContext(),"Группа уже добавлена",Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    void getMeIdForServer(long id_group, String name,String nameMyGroup, byte[] photoMyGroup, boolean Flag){
        String name_user =settings.getString(getString(R.string.key_for_me_name),"no_id");

        String user_path = getContext().getFilesDir() + "/user/" + getString(R.string.me_id);
        byte[] photo = ModelConverter.getPhoto(getContext(),user_path);

        UserApi userApi = server.getRetrofit().create(UserApi.class);
        userApi.addUser(new User(name_user,photo)).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                prefEditor.putString(getString(R.string.key_for_me_id_server), response.body().toString()).apply();
                if(Flag) {
                    addUserInGroup(new UserWithGroup(id_group, name, response.body()));
                }
                else{
                    createGroup(nameMyGroup,photoMyGroup);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast toast = Toast.makeText(getContext(),"Нет доступа к серверу",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void addUserInGroup(UserWithGroup userWithGroup) {
        GroupIdApi groupIdApi = server.getRetrofit().create(GroupIdApi.class);
        groupIdApi.getAllUsers(userWithGroup).enqueue(new Callback<TheAllGroupWithUser>() {
            @Override
            public void onResponse(Call<TheAllGroupWithUser> call, Response<TheAllGroupWithUser> response) {
                TheAllGroupWithUser allgroup = response.body();
                if(allgroup==null){
                    Toast toast = Toast.makeText(getContext(),"Группы не сущесвует",Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                GroupNameEntity groupName = ModelConverter.FromServerGroupToEntity(allgroup.getAllGroup(), String.valueOf(getContext().getFilesDir()));
                List<UserNameEntity> users = ModelConverter.FromServerUserToEntity(allgroup.getUsers(), String.valueOf(getContext().getFilesDir()));
                List<Integer> statuses = allgroup.getUsers().stream().map(UsersGroup::getStatus).collect(Collectors.toList());
                db.user_name().createUsers(users);
                db.group_name().createGroup(groupName);
                db.group().createGroupFromList(groupName,users,statuses);
                updateAdapter();

                dialogGroupJoin.name.setText("");
                dialogGroupJoin.dialog.dismiss();
            }

            @Override
            public void onFailure(Call<TheAllGroupWithUser> call, Throwable t) {
                Toast toast = Toast.makeText(getContext(),"Нет доступа к серверу",Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
    private void createGroup(String name,byte[] photo){
        TheAllGroupApi theAllGroupApi =server.getRetrofit().create(TheAllGroupApi.class);
        TheAllGroup group = new TheAllGroup();
        TheGroupId user = new TheGroupId();
        user.setId(Long.parseLong(settings.getString(getString(R.string.key_for_me_id_server),"no_id")));
        user.setStatus(1);
        group.setName(name);
        group.setPhoto(photo);
        TheAllGroupWithUserId groupWithUser = new TheAllGroupWithUserId();
        groupWithUser.setAllGroup(group);
        groupWithUser.setGroupId(user);
        theAllGroupApi.save(groupWithUser).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                long idGroup = response.body();

                db.group_name().createGroup(new GroupNameEntity(idGroup,dialogCreateGroup.name.getText().toString()));
                db.group().createGroup(new GroupEntity(Long.parseLong(getString(R.string.me_id)), idGroup, true));

                byte[] picture = DbBitmapUtility.getBytes(((BitmapDrawable) dialogCreateGroup.image.getDrawable().getCurrent()).getBitmap());
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ModelConverter.savePhoto(String.valueOf(getContext().getFilesDir()),picture,idGroup,false);
                    }
                });
                thread.start();
                updateAdapter();
                dialogCreateGroup.image.setImageDrawable(null);
                dialogCreateGroup.name.setText("");
                dialogCreateGroup.dialog.dismiss();
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast toast = Toast.makeText(getContext(),"Нет доступа к серверу",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void clickOnButtonCreateGroup(View v){
        if(!dialogCreateGroup.name.getText().toString().equals("")){
            byte[] photo;
            if(dialogCreateGroup.image.getDrawable()==null){
                photo = DbBitmapUtility.getBytes(((BitmapDrawable) getContext().getDrawable(R.drawable.defaul_avatar)).getBitmap());
            }
            else{
                photo =  DbBitmapUtility.getBytes(((BitmapDrawable) dialogCreateGroup.image.getDrawable().getCurrent()).getBitmap());
            }

            String myIdForServer = settings.getString(getString(R.string.key_for_me_id_server),"no_id");
            if(myIdForServer.equals("no_id")){
                getMeIdForServer(0,null,dialogCreateGroup.name.getText().toString(),photo,false);
            }
            else {
                createGroup(dialogCreateGroup.name.getText().toString(),photo);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }
}
package com.example.virtualmeetingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.virtualmeetingapp.Model.User;
import com.example.virtualmeetingapp.SpaceTabLayout.SpaceTabLayoutUserType.SpaceTabLayout1;
import com.example.virtualmeetingapp.SpaceTabLayout.SpaceTabLayoutUserType.SpaceTabLayout2;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.FragmentA;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.FragmentB;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.FragmentC;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.FragmentD;
import com.example.virtualmeetingapp.SpaceTabLayout.fragments.FragmentE;
import com.example.virtualmeetingapp.utils.Global;
import com.example.virtualmeetingapp.utils.TextUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import carbon.widget.ViewPager;

public class MainActivity extends AppCompatActivity {
    SpaceTabLayout1 tabLayout1;
    SpaceTabLayout2 tabLayout2;
    private User userType;
    FirebaseAuth auth;
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout1 = (SpaceTabLayout1) findViewById(R.id.spaceTabLayout1);
        tabLayout2 = (SpaceTabLayout2) findViewById(R.id.spaceTabLayout2);
        auth = FirebaseAuth.getInstance();

//        Bundle intent = getIntent().getExtras();
//        String user = intent.getString("uid", auth.getCurrentUser().getUid());
//        if (intent != null) {
//
//
//            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
//            editor.putString(TextUtil.PREF_LATEST_USER_ID, user);
//            editor.apply();
//
//            List<Fragment> fragmentList = new ArrayList<>();
//            fragmentList.add(new FragmentA());
//
//        }
        String uids = userType.getUserType();


//        if(userType != null) {
        if (uids.equals("visitor"))
            visitor();

        if (uids.equals("officer"))
            officer();

        if (uids.equals("admin"))
            admin();

        if (uids.equals("inmate"))
            prisoner();
//        }

    }

    private void visitor() {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main);
        List<Fragment> fragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        fragmentList.add(new FragmentA());
        fragmentList.add(new FragmentB());
        fragmentList.add(new FragmentC());

        tabLayout1.initialize(viewPager, getSupportFragmentManager(), fragmentList);

        tabLayout1.setTabOneOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Welcome to SpaceTabLayout", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        });

        tabLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "" + tabLayout1.getCurrentPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void officer() {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main);
        List<Fragment> fragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        fragmentList.add(new FragmentA());
        fragmentList.add(new FragmentB());
        fragmentList.add(new FragmentC());
        fragmentList.add(new FragmentD());
        fragmentList.add(new FragmentE());

        tabLayout2.initialize(viewPager, getSupportFragmentManager(), fragmentList);

        tabLayout2.setTabOneOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Welcome to SpaceTabLayout", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        });

        tabLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "" + tabLayout1.getCurrentPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void admin() {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main);
        List<Fragment> fragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        fragmentList.add(new FragmentA());
        fragmentList.add(new FragmentB());
        fragmentList.add(new FragmentC());
        fragmentList.add(new FragmentD());
        fragmentList.add(new FragmentE());

        tabLayout2.initialize(viewPager, getSupportFragmentManager(), fragmentList);

        tabLayout2.setTabOneOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Welcome to SpaceTabLayout", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        });

        tabLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "" + tabLayout1.getCurrentPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prisoner() {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main);
        List<Fragment> fragmentList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        fragmentList.add(new FragmentA());
        fragmentList.add(new FragmentB());
        fragmentList.add(new FragmentC());

        tabLayout1.initialize(viewPager, getSupportFragmentManager(), fragmentList);

        tabLayout1.setTabOneOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Welcome to SpaceTabLayout", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }
        });

        tabLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "" + tabLayout1.getCurrentPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
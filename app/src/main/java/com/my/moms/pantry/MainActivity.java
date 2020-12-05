package com.my.moms.pantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelySaveStateHandler;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public int position;
    private static final int FAB_ID = R.id.fab;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleSignInClient mSignInClient;
    private static final String MESSAGE_URL = "http://pantryapp.firebase.google.com/message/";
    public static final String ANONYMOUS = "anonymous";
    public static final String FOODS_CHILD = "Pantry2";
    private DrawerLayout mDrawerLayout;
    private EditText mName, mQuantity, mLifecycle;
    private Long date;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = database.getReference("Pantry");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //remove below if it breaks
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Set default username as anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        //side bar menu
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //add to database floating action button
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        //new child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //initializes the drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //initializes navigation menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }


        //set up pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        assert viewPager != null;
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
           @Override
           public void onPageSelected(int pos){
               Log.i("POS", "Position: "+pos);
               position = pos;

           }
        });


        //initialize tab layout and bind to view with pager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //initialize fab and bind to view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //set an  onclick event for fab
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                if(position == 0){
                    pantryDialog();
                    Toast.makeText(MainActivity.this, "Fragment 0", Toast.LENGTH_SHORT).show();
                }
                else if(position == 1){
                    groceryDialog();
                    Toast.makeText(MainActivity.this, "Fragment 1", Toast.LENGTH_SHORT).show();

                }
                else if(position == 2){
                    recipeDialog();
                    Toast.makeText(MainActivity.this, "Fragment 2", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    //lovely dialog
//    public void showLovelyDialog(int savedDialogId, Bundle savedInstanceState) {
//        showTextInputDialog(savedInstanceState);
//    }

    //text input dialog
//    private void showTextInputDialog(Bundle savedInstanceState) {
//        new LovelyTextInputDialog(this, R.style.EditTextTintTheme)
//                .setTopColorRes(R.color.PINK)
//                .setTitle(R.string.text_input_title)
//                .setMessage(R.string.text_input_message)
//                .setIcon(R.drawable.ic_forum)
//                .setInstanceStateHandler(FAB_ID, new LovelySaveStateHandler())
//                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
//                    @Override
//                    public void onTextInputConfirmed(String text) {
//                        Toast.makeText(MainActivity.this, "Added  " + text + "to database", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setSavedInstanceState(savedInstanceState)
//                .show();
//    }


    /***
     * this is the dialog to add items to pantry inventory
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pantryDialog() {

        //final Context context = this;
        final LovelyCustomDialog mDialog = new LovelyCustomDialog(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.pantry_dialog, null);

        mDialog.setView(dialogView);
        mDialog.setTopColorRes(R.color.PINK);
        mDialog.setTitle(R.string.pantry_dialog_title);
        mDialog.setIcon(R.drawable.ic_forum);
        mDialog.setInstanceStateHandler(FAB_ID, new LovelySaveStateHandler());
        mDialog.show();

        /***
         * onclick event to bind dialog to view
         */
        mDialog.setListener(R.id.ld_btn_confirm, (View.OnClickListener) view -> {

            final EditText name = (EditText) dialogView.findViewById(R.id.item_name);
            final EditText quantity = (EditText) dialogView.findViewById(R.id.item_quantity);
            final EditText lifecycle = (EditText) dialogView.findViewById(R.id.item_lifecycle);


            SimpleDateFormat purchaseDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

            //initialize strings for database insertion
            String mDate = purchaseDate.format(new Date());
            String mName = WordUtils.capitalize(name.getText().toString().trim());
            String mQuantity = quantity.getText().toString().trim();
            String mLifecycle = lifecycle.getText().toString().trim();
            String mExpireDate = getExpirationDate(mDate, Integer.parseInt(mLifecycle));


            //insert into database
            FirebaseDatabase.getInstance().getReference("Pantry")
                    .child(mName)
                    .setValue(new food(mName, mQuantity, mLifecycle, mDate, mExpireDate));


            //dismiss the dialog box
            mDialog.dismiss();
            Toast.makeText(MainActivity.this, "Added " + mName + " to Pantry Inventory", Toast.LENGTH_LONG).show();
        });

    }

    /***
     * this is the dialog that allows the user to add items to grocery list
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void groceryDialog() {

        //final Context context = this;
        final LovelyCustomDialog mDialog = new LovelyCustomDialog(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.grocery_dialog, null);

        mDialog.setView(dialogView);
        mDialog.setTopColorRes(R.color.PINK);
        mDialog.setTitle(R.string.grocery_dialog_title);
        mDialog.setIcon(R.drawable.ic_forum);
        mDialog.setInstanceStateHandler(FAB_ID, new LovelySaveStateHandler());
        mDialog.show();

        /***
         * onclick event to bind dialog to view
         */
        mDialog.setListener(R.id.ld_btn_confirm, (View.OnClickListener) view -> {

            final EditText name = (EditText) dialogView.findViewById(R.id.item_name);
            final EditText quantity = (EditText) dialogView.findViewById(R.id.item_quantity);


            SimpleDateFormat purchaseDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

            //initialize strings for database insertion
            String mDate = purchaseDate.format(new Date());
            String mName = WordUtils.capitalize(name.getText().toString().trim());
            String mQuantity = quantity.getText().toString().trim();

            //insert into database
            FirebaseDatabase.getInstance().getReference("Grocery List")
                    .child(mName)
                    .setValue(new grocery(mName, mQuantity, mDate));


            //dismiss the dialog box
            mDialog.dismiss();
            Toast.makeText(MainActivity.this, "Added " + mName + " to Grocery List", Toast.LENGTH_LONG).show();
        });
    }

    /***
     * This is the recipe dialog that allows the user to add items to their recipe list
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recipeDialog() {

        //final Context context = this;
        final LovelyCustomDialog mDialog = new LovelyCustomDialog(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.recipe_dialog, null);

        mDialog.setView(dialogView);
        mDialog.setTopColorRes(R.color.PINK);
        mDialog.setTitle(R.string.recipe_dialog_title);
        mDialog.setIcon(R.drawable.ic_forum);
        mDialog.setInstanceStateHandler(FAB_ID, new LovelySaveStateHandler());
        mDialog.show();

        /***
         * onclick event to bind dialog to view
         */
        mDialog.setListener(R.id.ld_btn_confirm, (View.OnClickListener) view -> {

            final EditText name = (EditText) dialogView.findViewById(R.id.item_name);
            final EditText quantity = (EditText) dialogView.findViewById(R.id.item_quantity);
            final EditText lifecycle = (EditText) dialogView.findViewById(R.id.item_lifecycle);


            SimpleDateFormat purchaseDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

            //initialize strings for database insertion
            String mDate = purchaseDate.format(new Date());
            String mName = WordUtils.capitalize(name.getText().toString().trim());
            String mQuantity = quantity.getText().toString().trim();
            String mLifecycle = lifecycle.getText().toString().trim();
            String mExpireDate = getExpirationDate(mDate, Integer.parseInt(mLifecycle));

            Log.i(mExpireDate, "Date: " + mDate + " + " + mLifecycle + " = Expiration date: " + mExpireDate);




            //insert into database
            FirebaseDatabase.getInstance().getReference("Pantry")
                    .child(mName)
                    .setValue(new food(WordUtils.capitalize(mName), mQuantity, mLifecycle, mDate, mExpireDate));

            Log.i(mDate.toString(), "mDate");

            //dismiss the dialog box
            mDialog.dismiss();
            Toast.makeText(MainActivity.this, "Added " + mName + " to database", Toast.LENGTH_LONG).show();
        });

    }

    /***
     * inflates the menu
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    /***
     * switch statements to change mode
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                menu.findItem(R.id.menu_night_mode_system).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                menu.findItem(R.id.menu_night_mode_auto).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                menu.findItem(R.id.menu_night_mode_night).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                menu.findItem(R.id.menu_night_mode_day).setChecked(true);
                break;
        }
        return true;
    }

    /***
     * method to handle sign out event
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mSignInClient.signOut();
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;

            //change the view mode
            default:
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_night_mode_system:
                setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case R.id.menu_night_mode_day:
                setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.menu_night_mode_night:
                setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.menu_night_mode_auto:
                setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /***
     * night mode
     * @param nightMode
     */
    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
    }

    /***
     * sets up the view pager and adds the fragments for each list
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new PantryListFragment(), "Inventory");
        adapter.addFragment(new GroceryListFragment(), "Grocery List");
        adapter.addFragment(new RecipeFragment(), "Recipe List");
        viewPager.setAdapter(adapter);
    }

    /***
     * sets up the left navigation pane
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                });
    }

    /***
     * fragment pager adapter
     */

    static class Adapter extends FragmentPagerAdapter {
        public final List<Fragment> mFragments = new ArrayList<>();
        public final List<String> mFragmentTitles = new ArrayList<>();

        /***
         * initializes the fragment adapter
         * @param fm
         */
        public Adapter(FragmentManager fm) {
            super(fm);
        }

        /***
         * adds the fragment and name
         * @param fragment
         * @param title
         */
        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        /***
         * gets the position
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        /***
         * returns the number of fragments
         * @return
         */
        @Override
        public int getCount() {
            return mFragments.size();
        }

        /***
         * returns the pager title for each position
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    /***
     * Calculates the expiration date by adding the lifecycle to the purchase date
     * @param foodDate is the purchase date
     * @param lifecycle is the lifecycle date entered by user from database
     * @returns the expiration date
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getExpirationDate(String foodDate, int lifecycle) {

        //Converts the string element foodDate date from firebase into a Date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");         //convert String containing the purchase date to a Date object
        try {
            Date purchaseDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss").parse(foodDate);
            Log.i(purchaseDate.toString(), "purchaseDate before conversion: " + foodDate + " purchaseDate after: " + purchaseDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // set time.
        c.add(Calendar.DATE, lifecycle); // Adding the lifecycle, an integer, to calculate mExpireDateation date

        String mExpireDateationDate = sdf.format(c.getTime()); //converts the date to string

        Log.i("Conversion: ", "DATE INPUT: " + foodDate + " PLUS lifecycle: " + lifecycle + " EQUALS " + mExpireDateationDate);
        Log.i(mExpireDateationDate, "DATE OUTPUT: " + mExpireDateationDate);

        return mExpireDateationDate;
    }



}

package uk.ac.horizon.aestheticodes.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;

import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListAdapter;
import uk.ac.horizon.aestheticodes.model.Experience;

public class SelectorActivity extends Activity
{

    private ExperienceListAdapter experienceList;
    private String[] muralIds = {"55a4bbf4-0327-426b-b554-8fb064663b8a","a564fe42-da31-4544-b317-143637bc9c85","053197ac-eedc-4a3f-a248-4ae21b8fb77a"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selector_screen);

        experienceList = new ExperienceListAdapter(this, Aestheticodes.getExperiences());

        experienceList.update();

        SwitchCompat fullscreenSwitch = (SwitchCompat) findViewById(R.id.fullscreen_switch);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean fullscreen = sharedPreferences.getBoolean("fullscreen", false);
        fullscreenSwitch.setChecked(fullscreen);

        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fullscreen", b);
                editor.apply();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectMural1(View view)
    {
        goToMural(this.muralIds[0]);
    }

    public void selectMural2(View view)
    {
        goToMural(this.muralIds[1]);
    }

    public void selectMural3(View view)
    {
        goToMural(this.muralIds[2]);
    }

    private void goToMural(String experienceId)
    {
        Log.i("ID", experienceId);
        final Experience experience = this.experienceList.getSelected(experienceId);

        final Experience prevExperience = this.experienceList.getSelected(loadSelectExperienceFromPreferences());
        if (prevExperience!=null && experience!=null && !prevExperience.getId().equals(experience.getId()) && experience.getName().equals(prevExperience.getName()))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Continue?")
                    .setMessage("Do you want to continue where you were previously or start again?")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            goToMural(prevExperience.getId());
                        }
                    })
                    .setNegativeButton("Start again", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            saveSelectedExperienceToPreferences(experience.getId());
                            goToMural(experience.getId());
                        }
                    })
                    .show();
            return;
        }


        if (experience!=null)
        {
            saveSelectedExperienceToPreferences(experience.getId());
            Intent intent = new Intent();
            intent.putExtra("caller", this.getClass().getCanonicalName());
            if (experience.getStartUpURL()!=null)
            {
                intent.setClass(this, WebActivity.class);
                intent.putExtra("URL", experience.getStartUpURL());
            }
            else
            {
                intent.setClass(this, AestheticodesActivity.class);
            }
            this.startActivity(intent);
        }
    }

    private void saveSelectedExperienceToPreferences(String experienceId)
    {
        if(experienceId != null)
        {
            Log.i("EXPERIENCE_PREF", "Setting experience id " + experienceId);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("experience", experienceId);
            editor.apply();
        }
    }

    public String loadSelectExperienceFromPreferences()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String selectedID = sharedPreferences.getString("experience", null);
        Log.i("EXPERIENCE_PREF", "Loading experience from prefs... "+selectedID);
        Experience newSelected = experienceList.getSelected(selectedID);
        if (newSelected != null)
        {
            Log.i("EXPERIENCE_PREF", "...found " + newSelected.getName());
        }
        else
        {
            Log.i("EXPERIENCE_PREF", "...found nothing.");
        }
        return selectedID;
    }
}

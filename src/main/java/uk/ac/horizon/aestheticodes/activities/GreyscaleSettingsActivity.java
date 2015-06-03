/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2015  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.aestheticodes.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import uk.ac.horizon.aestheticodes.Aestheticodes;
import uk.ac.horizon.aestheticodes.R;
import uk.ac.horizon.aestheticodes.controllers.CameraController;
import uk.ac.horizon.aestheticodes.controllers.ExperienceFileController;
import uk.ac.horizon.aestheticodes.controllers.ExperienceListController;
import uk.ac.horizon.aestheticodes.controllers.MatTranform;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.Greyscaler;

public class GreyscaleSettingsActivity extends ActionBarActivity {

    private CameraController camera;
    private SurfaceHolder holder;
    private ImageThread imageThread;
    private Experience experience;
    private ExperienceListController experiences;
    private boolean hasChanged;
    private final Object lockObject = new Object();
    private final String TAG = "GreyActivity";

    private static List<List<Object> > getColorPresets()
    {
        List<List<Object>> presets = new ArrayList<>();
        List<Object> l;
        // use Double here as serialization can go wrong if you let the compiler use implicit constructors.
        Double zero = new Double(0.0), one = new Double(1.0);
        l = new ArrayList<>(4); l.add("RGB"); l.add(new Double(0.299)); l.add(new Double(0.587)); l.add(new Double(0.114)); presets.add(l);
        l = new ArrayList<>(4); l.add("RGB"); l.add(one ); l.add(zero); l.add(zero); presets.add(l);
        l = new ArrayList<>(4); l.add("RGB"); l.add(zero); l.add(one ); l.add(zero); presets.add(l);
        l = new ArrayList<>(4); l.add("RGB"); l.add(zero); l.add(zero); l.add(one ); presets.add(l);

        l = new ArrayList<>(5); l.add("CMYK"); l.add(one ); l.add(zero); l.add(zero); l.add(zero); presets.add(l);
        l = new ArrayList<>(5); l.add("CMYK"); l.add(zero); l.add(one ); l.add(zero); l.add(zero); presets.add(l);
        l = new ArrayList<>(5); l.add("CMYK"); l.add(zero); l.add(zero); l.add(one ); l.add(zero); presets.add(l);
        l = new ArrayList<>(5); l.add("CMYK"); l.add(zero); l.add(zero); l.add(zero); l.add(one ); presets.add(l);

        l = new ArrayList<>(4); l.add("CMY"); l.add(one ); l.add(zero); l.add(zero); presets.add(l);
        l = new ArrayList<>(4); l.add("CMY"); l.add(zero); l.add(one ); l.add(zero); presets.add(l);
        l = new ArrayList<>(4); l.add("CMY"); l.add(zero); l.add(zero); l.add(one ); presets.add(l);

        return presets;
    }

    public static String getPresetName(Context context, List<Object> preset)
    {
        String[] names = context.getResources().getStringArray(R.array.colourPresetNames);
        if (preset==null)
        {
            return names[0];
        }
        List<List<Object> > presets = getColorPresets();
        int index = presets.indexOf(preset);
        if (index >= 0 && index < names.length)
        {
            return names[index];
        }
        else
        {
            return "Other";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.settings_greyscale);

        // setup the camera for preview:
        camera = new CameraController(this);
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.greyColorPreview);
        holder = surfaceView.getHolder();
        holder.addCallback(camera);
        // deprecated setting, but required on Android versions prior to 3.0
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // get the experience from the bundle:
        final Bundle extras = getIntent().getExtras();
        experiences = Aestheticodes.getExperiences();
        if (extras != null)
        {
            final String experienceID = extras.getString("experience");
            if (experienceID != null)
            {
                experience = experiences.get(experienceID);
            }
        }

        if (experience == null)
        {
            experience = new Experience();
            experience.setId(UUID.randomUUID().toString());
            experience.setOp(Experience.Operation.create);
        }

        // set up the UI elements:
        
        SwitchCompat invertSwitch = (SwitchCompat) findViewById(R.id.greyscaleInvertSwitch);
        invertSwitch.setChecked(experience.getInvertGreyscale());
        invertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                synchronized (lockObject) {
                    experience.setInvertGreyscale(b);
                    hasChanged = true;
                }
            }
        });

        final TextView hueShiftLabel = (TextView) findViewById(R.id.hueSeekBarLabel);
        hueShiftLabel.setText(getString(R.string.greyscaleHueSeekBarLabel,(int) experience.getHueShift()));
        SeekBar hueSlider = (SeekBar) findViewById(R.id.hueSeekBar);
        hueSlider.setProgress((int) experience.getHueShift());
        hueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                experience.setHueShift(i);
                hasChanged = true;
                hueShiftLabel.setText(getString(R.string.greyscaleHueSeekBarLabel,i));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // make sure the preview thread got the last change:
                synchronized (lockObject) {
                    hasChanged = true;
                }
            }
        });

        Spinner colourPresetSpinner = (Spinner) findViewById(R.id.colourPresetSpinner);
        List<CharSequence> names = new ArrayList<>();
        Collections.addAll(names, getResources().getStringArray(R.array.colourPresetNames));

        // presets (the order corresponds to the order of the "colourPresetNames" array in values.xml):
        final List<List<Object>> presets = getColorPresets();

        // find the index of the current preset
        int index = 0;
        if (experience.getGreyscaleOptions()!=null)
        {
            index = presets.indexOf(experience.getGreyscaleOptions());
            if (index<0) // if the current preset is not in the list add an "other" option
            {
                names.add("Other");
                presets.add(this.experience.getGreyscaleOptions());
                colourPresetSpinner.setSelection(presets.size() - 1);
            }
        }

        // setup the adapter for the spinner
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, names);
        ArrayAdapter.createFromResource(this,
                R.array.colourPresetNames, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colourPresetSpinner.setAdapter(adapter);
        colourPresetSpinner.setSelection(index);

        colourPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] names = getResources().getStringArray(R.array.colourPresetNames);
                if (i<presets.size()) {
                    synchronized (lockObject) {
                        experience.setGreyscaleOptions(presets.get(i));
                        hasChanged = true;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_greyscale_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home open_button
            case android.R.id.home:
                //properties.save();
                experiences.add(experience);
                if (experience.getOp() == null)
                {
                    experience.setOp(Experience.Operation.update);
                }
                ExperienceFileController.save(this, experiences);
                Intent intent = new Intent(this, ExperienceActivity.class);
                intent.putExtra("experience", experience.getId());

                NavUtils.navigateUpTo(this, intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopCamera();
    }

    private void startCamera()
    {
        if (camera != null)
        {
            try
            {
                camera.start(holder);
            }
            catch (Exception e)
            {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        if (imageThread==null || imageThread.running==false) {
            Log.i(TAG,"Starting img thread");
            this.imageThread = new ImageThread();
            this.imageThread.greyscaler = this.experience.getGreyscaler();
            this.imageThread.start();
        }
    }

    private void stopCamera()
    {
        if (this.imageThread!=null)
        {
            this.imageThread.running = false;
            this.imageThread.interrupt();
        }
        if (camera != null)
        {
            camera.release();
        }
    }

    void updateImage(final Bitmap bitmap)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView preview = (ImageView) findViewById(R.id.greyPreview);
                preview.setImageBitmap(bitmap);
            }
        });
    }

    private class ImageThread extends Thread {
        private long timeOfLastAutoFocus = 0;
        boolean running = true;
        private Bitmap result;
        Greyscaler greyscaler = null;
        private final String TAG = "GreyImgThread";

        @Override
        public void run() {
            Log.i(TAG, "Starting greyscale preview processing thread");
            try {
                timeOfLastAutoFocus = System.currentTimeMillis();
                Camera.Size size = camera.getSize();
                Mat drawImage = new Mat(size.height, size.width, CvType.CV_8UC3);
                result = null;

                while (running) {
                    try {
                        byte[] data = camera.getData();
                        if (data != null) {

                            Mat yuvImage = new Mat(size.height+size.height/2, size.width, CvType.CV_8UC1);
                            yuvImage.put(0, 0, data);

                            Mat greyImage = null;
                            synchronized (lockObject) {
                                if (hasChanged) {
                                    hasChanged = false;
                                    greyscaler = experience.getGreyscaler();
                                }
                            }
                            if (greyscaler != null)
                            {
                                greyImage = greyscaler.greyscaleImage(yuvImage);
                            }
                            yuvImage.release();

                            if (greyImage!=null) {
                                MatTranform.rotate(greyImage, greyImage, 360 + 90 - camera.getRotation(), camera.isFront());
                                Imgproc.cvtColor(greyImage, drawImage, Imgproc.COLOR_GRAY2BGRA);
                                greyImage.release();

                                if (result == null) {
                                    result = Bitmap.createBitmap(drawImage.cols(), drawImage.rows(), Bitmap.Config.ARGB_8888);
                                }
                                Utils.matToBitmap(drawImage, result);
                                updateImage(result);
                            }

                        } else {
                            Log.i(TAG, "No data");
                            synchronized (this) {
                                wait(200);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                    // Test if camera needs to be focused
                    if (CameraController.deviceNeedsManualAutoFocus && System.currentTimeMillis() - timeOfLastAutoFocus >= 5000) {
                        timeOfLastAutoFocus = System.currentTimeMillis();
                        camera.performManualAutoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean b, Camera camera) {
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            Log.i(TAG, "Finishing greyscale preview processing thread");
        }
    }
}

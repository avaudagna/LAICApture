package alan.laicapture;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    CameraPreview cv;
    FrameLayout alParent;

    private SensorManager mSensorManager;
    private Sensor accelerometer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // requesting to turn the title OFF
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Defino los sensores que voy a utilizar
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initListeners();


        // Try to get the camera
        Camera c = getCameraInstance();

        // If the camera was received, create the app
        if (c != null) {
        	/* Create our layout in order to layer the
        	 * draw view on top of the camera preview.
        	 */
            alParent = new FrameLayout(this);
            alParent.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));

            // Create a new camera view and add it to the layout
            cv = new CameraPreview(this, c);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cv);

            // Add a listener to the Capture button
            Button captureButton = (Button) findViewById(R.id.btnfoto);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            cv.takePicture(null, null, new PhotoHandler(getApplicationContext()));
                            cv.resume();
                        }
                    }
            );
        }

        // If the camera was not received, close the app
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to find camera. Closing.", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

   /* public void onClick(View view) {
        cv.takePicture(null, null,
                new PhotoHandler(getApplicationContext()));
    }*/

    public void initListeners()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    //Serie de funciones que deshabilitan la lectura de los sensores cuando
    //el dispositivo esta bloqueado, o al cerrarse la aplicacion
    @Override
    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    @Override
    public void onResume()
    {
        cv.resume();
        initListeners();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        if (cv != null) {
            cv.release();
            cv = null;
        }
        mSensorManager.unregisterListener(this);
        super.onPause();
    }


    //atributos globales necesarios para la obtencion de la inclinacion
    //y su visualizacion

    float[] inclineGravity = new float[3];
    float[] mGravity;
    float orientation[] = new float[3];

    //Funcion que se ejecuta cada vez que hay un evento del tipo SensorEvent
    //Para agregar sensores a la lista se usa mSensorManager.registerListener
    //en initListeners()
    @Override
    public void onSensorChanged(SensorEvent event) {

        //If type is accelerometer only assign values to global property mGravity
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            mGravity = event.values;
        }

        int inclination[] = get_inclination();

        TextView tv_x = (TextView)findViewById(R.id.x_axis);
        TextView tv_y = (TextView)findViewById(R.id.y_axis);
        TextView tv_z = (TextView)findViewById(R.id.z_axis);

        tv_x.setText(Integer.toString(inclination[0]));
        tv_y.setText(Integer.toString(inclination[1]));
        tv_z.setText(Integer.toString(inclination[2]));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    //Devuelve la inclinacion obtenida a partir de la gravedad sensada por cada eje
    public int[] get_inclination()
    {
        inclineGravity = mGravity.clone();

        double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

        // Normalize the accelerometer vector
        inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
        inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
        inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

        //Checks if device is flat on ground or not
        int inclination [] = {0,0,0};
        inclination[0] = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[0])));
        inclination[1] = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[1])));
        inclination[2] = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

        return inclination;
    }


}

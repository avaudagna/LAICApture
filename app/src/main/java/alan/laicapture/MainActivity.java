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

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    CameraPreview cv;
    FrameLayout alParent;

    private SensorManager mSensorManager;
    private Sensor accelerometer;



    @Override
    /*
     * Llamada al momento de iniciarse la app
     * SensorManager mSensorManager: Se encarga de administrar los sensores
     * Sensor accelerometer        : El sensor acelerometro, instancia de Sensor
     *                                  necesaria para registrar el mismo en mSensorManager
     * Camera c                    : Instancia de android.hardware.Camera que se utiliza
     *                                  para generar la previsualizacion de la camara
     *                                  (Camera Preview) y tomar la foto
     * CameraPreview cv            : Objeto que soluciona la parte de mostrar el CameraPreview
     *                                  en pantalla
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); //"Dibuja" lo que esta en activity_main.xmkl

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Defino los sensores que voy a utilizar
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Los registro
        initListeners();


        // Trato de obtener una instancia de camera
        Camera c = getCameraInstance();

        // Si se pudo obtener con exito, buscio generar el preview
        if (c != null) {
        	/* Create our layout in order to layer the
        	 * draw view on top of the camera preview.
        	 * TODO: Verificar si la parte del Frame configurada aca es indispensable
        	 * TODO: Esta haciendo algo siquiera?
        	 */
            alParent = new FrameLayout(this);
            alParent.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));


            cv = new CameraPreview(this, c); //Crea un nuevo CameraPreview
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cv); //LLena el FrameLayout creado en el activity_main.xml


            Button captureButton = (Button) findViewById(R.id.btnfoto);
            //Agrega un listener para capturar el evento del presionado del boton
            //Esto es una funcion codificada en el momento
            //TODO: vale la pena emprolijar esto?
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Llama indirectamente a takePicture de android.hardware.Camera
                            cv.takePicture(null, null, new PhotoHandler(getApplicationContext()));
                            //Permite que la visualiazacion de la preview continue y no se trabe la
                            //imagen
                            cv.resume();
                        }
                    }
            );
        }

        // Si no se pudo obtener una instancia de la camara, se informa y cerramos la app
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to find camera. Closing.", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    /*
     * Registra los sensores que se estaran utilizando
     * En este momento solo se registra al acelerometro
     */
    public void initListeners()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /*
     * Muestra el menu en el main
     */
    //TODO : Verificar si lo esta haciendo, ver si lo necesitamos para algo, sino borrarlo
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
     * Respuesta a la seleccion de opcion en el menu
     */
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

    /*
     * Trata de crear el objeto Camera que se estara utilizando en la app
     * Si falla al crearla, le hace un catch a la Exception
     * TODO: Hacer que en el catch muestre un mensaje TOAST de que no pudo levantar la camara
     */
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

    /*
     * Funcion de respuesta ante la finalizacion de la aplicacion (cierre)
     * Desregistrando el sensor se supone que deja de leerlo y por consiguiente
     * deja de consumir energia.
     */
    @Override
    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    /*
     * Respuesta ante suspension del telefono
     * Desregistra los sensores
     */
    @Override
    public void onBackPressed()
    {
        mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    /* Funcion de respuesta ante el resumen de la app luego de la suspension
       del movil.
     * Habilita nuevamente los sensores llamando a initListeners()
     * la camara con cv.resume() y el resto de la app con super.onResume()
     */
    @Override
    public void onResume()
    {
        cv.resume();
        initListeners();
        super.onResume();
    }

    /*
     * No se cuando pasa esto, sera al bloquearlo?*
     * TODO: Definir si el bloqueo del telefono sucede aca o en onBackPressed
     */
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
    float[] mGravity;   //Tendra la lectura bruta del acelerometro
                        // (aparentemente el valor de la gravedad en cada eje)
                        //TODO: Medido en que unidad?
    float orientation[] = new float[3]; //TODO: Se esta usando?




    int ocurrences = 0;
    /*
     * Funcion que se ejecuta cada vez que hay un evento del tipo SensorEvent
     * Para agregar sensores a la lista se usa mSensorManager.registerListener en initListeners()
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //If type is accelerometer only assign values to global property mGravity
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            mGravity = event.values;
        }

        float inclination [] = get_inclination();


        //Hago esto para solo tomar 1 de cada 10 mediciones
        //De esta forma lo que marcara el TextView sera el valor real medido y no un
        //promedio de la misma como seria haciendo un promedio de las n mediciones medidas
        // (la media)
        if(ocurrences < 10) {
            ocurrences ++;
        }
        else {
            ocurrences=0;

            //Para quedarme solo con 2 decimales de la medicion
            DecimalFormat df = new DecimalFormat("##.##");
            df.setRoundingMode(RoundingMode.DOWN);

            TextView tv_x = (TextView) findViewById(R.id.x_axis);
            TextView tv_y = (TextView) findViewById(R.id.y_axis);
            TextView tv_z = (TextView) findViewById(R.id.z_axis);

            tv_x.setText(df.format(inclination[0]).toString());
            tv_y.setText(df.format(inclination[1]).toString());
            tv_z.setText(df.format(inclination[2]).toString());
        }
    }


    //Metodo autogenerado necesario
    //TODO: Nos sirve de algo?
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }


    /*
     * Devuelve la inclinacion obtenida a partir de la gravedad sensada por cada eje
     * sin redondear los decimales obtenidos
     */
    public float[] get_inclination()
    {
        inclineGravity = mGravity.clone();

        //Se obtiene la norma del vector gravedad, que contiene la lectura de la misma en cada eje (x,y,z)
        double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

        //Normalizacion del vector aceleracion (el de la gravedad leida en cada eje)
        inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
        inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
        inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

        //Se convierte el valor raw obtenido por el sensor (TODO: EN QUE MAGNITUD??)
        //a grados. Se realiza una aproximacion de decimales tambien para quedarnos con
        //la parte entera nada mas.
        float  inclination [] = {0,0,0};
        inclination[0] = (float) (Math.toDegrees(Math.acos(inclineGravity[0])));
        inclination[1] = (float) (Math.toDegrees(Math.acos(inclineGravity[1])));
        inclination[2] = (float) (Math.toDegrees(Math.acos(inclineGravity[2])));

        return inclination;
    }




}

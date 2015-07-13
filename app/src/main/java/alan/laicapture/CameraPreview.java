package alan.laicapture;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by alan on 23/06/15.
 */

/*
 * Resuelve el tema de mostrar en la pantalla lo que esta viendo la camara (esto es conocido como CameraPreview).
 * Se utiliza la clase android.hardware.Camera
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera; //Esta sera la instancia de android.hardware.Camera que utilizaremos

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CameraView", "Error setting camera preview: " + e.getMessage());
        }
    }

    /*
     *
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    /* Funcion de respuesta ante los cambios de orientacion del celular
     * If your preview can change or rotate, take care of those events here.
     * Make sure to stop the preview before resizing or reformatting it.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("CameraView", "Error starting camera preview: " + e.getMessage());
        }
    }

    //Llamada a la funcion que toma la foto
    //TODO: Pareciera que aca se define el formato en el que se devuelve la foto que seria jpeg, verificar esto.
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg)
    {
        mCamera.takePicture(shutter, raw, jpeg);
    }

    //Cuando el celular se suspende
    public void onPause() {
        mCamera.release();
        mCamera = null;
    }

    public void release() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    //cuando vuelve de la suspension el celular o cuando se termino de sacar la foto
    public void resume() {
        try
        {
            mCamera.startPreview();

        } catch(Exception e)
        {
            Log.d("CameraView", "Error resuming camera preview: " + e.getMessage());
        }
    }

}
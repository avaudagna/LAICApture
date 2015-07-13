# README #


Commits
Alan Vaudagna committed 1aef4fa
an hour ago
Approve

First commit, up to the moment the proyect has the following features:

	* Can show the camera preview with the screen divided

	* In the other division of the screen it shows the 3 axis angles

	* The screen orientation is set to landscape because the Camera class defaults its position that way

	* The angles are obtained from the accelerometer alone

	* The design of the whole app is pretty plain but straightforward

	* Shows a toast message saying whether the image has been succesfully saved or not

	* Taken images are stored on the internal memory in the directory /storage/sdcar0/Pictures/CameraPreview
		The filenames have the format : "Picture_" + date + ".jpg", where date is the string representation of the actual date
		at the actual time (yyyymmddhhmmss)

	* Battery usage has been taken into consideration with the unregistering of the sensors when the device
		gets suspended  or the app closed. The accelerometer data reading is considerably high power demanding
		and it should be taken into account.

	* The android.hardware.Camera class used, is deprecated at the moment (support will no longer be given).
		The alternative of using the Camera2 class is not viable since it was introduced on API level 21
		(Lollipop 5.0) and does not support older APIs. Since the vast mayority of phones in Argentina
		uses from Gingerbread 2.3 to Jelly Bean 4.1/2/3, the deprecated Camera version is used (supports from API level 1 to 19). It is supposed that
		there will be no complications introduced by this decision, but the posibility exists. In that case
		a version of the app could be made for handsets with Lollipop 5.0 on forward, using Camera2.

	* The activity_main.xml uses a combination Layouts:
		A FrameLayout to show the camera preview
		A Linear Layout to contain the button and the angles TextViews
		And a Linear Layout to wrap all that
		The disposition has been accomplished using android:layout_weight , android:gravity and
			android:layout_gravity properties.

	*Important AndroidManifest permissions and features:
		    <uses-permission android:name="android.permission.CAMERA"/>
		    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
		    <uses-feature android:name="android.hardware.camera" />
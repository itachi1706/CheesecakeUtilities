package com.itachi1706.cheesecakeutilities.modules.SystemInformation;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.appcompat.app.AppCompatActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class InitActivity extends AppCompatActivity {
    private static SharedPreferences prefs;

    // TODO: Revamp this as it does not work well in dark mode. We are using a legacy theme to mitigate for now

    private class ClearRenderer implements Renderer {
        ClearRenderer() {
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            String glesver = String.valueOf(((ActivityManager) InitActivity.this.getSystemService(ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion());
            Editor editor = InitActivity.prefs.edit();
            editor.putString("RENDERER", gl.glGetString(GL10.GL_RENDERER));
            editor.putString("VENDOR", gl.glGetString(GL10.GL_VENDOR));
            editor.putString("VERSION", gl.glGetString(GL10.GL_VERSION));
            editor.putString("EXTENSIONS", gl.glGetString(GL10.GL_EXTENSIONS));
            if (glesver.equals("2.0")) editor.putString("EXTENSIONS2", GLES20.glGetString(GL10.GL_EXTENSIONS));
            if (glesver.equals("3.0")) editor.putString("EXTENSIONS3", GLES30.glGetString(GL10.GL_EXTENSIONS));
            editor.apply();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(AccessibilityNodeInfoCompat.ACTION_COPY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("GPUinfo", 0);
        GLSurfaceView mGLView = new GLSurfaceView(this);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.setRenderer(new ClearRenderer());
        setContentView(mGLView);
        mGLView.post(() -> {
            startActivity(new Intent(InitActivity.this, ParentFragmentActivity.class));
            finish();
        });
    }
}

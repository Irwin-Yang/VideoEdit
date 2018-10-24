/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinssible.librecorder.base;

import android.opengl.Matrix;
import android.view.MotionEvent;

import com.pinssible.librecorder.filter.program.GpuImageProgram;
import com.pinssible.librecorder.filter.program.Program;
import com.pinssible.librecorder.filter.program.Texture2dProgram;

import java.nio.FloatBuffer;

/**
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 *
 */
public class FullFrameRect {

    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
    private Program mProgram;
    private final Object mDrawLock = new Object();

    private static final int SIZEOF_FLOAT = 4;

    private float[] IDENTITY_MATRIX = new float[16];

    private static final float TEX_COORDS[] = {  //纹理坐标系（和顶点坐标系有所不同）
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };
    private static final FloatBuffer TEX_COORDS_BUF = GlUtil.createFloatBuffer(TEX_COORDS);
    private static final int TEX_COORDS_STRIDE = 2 * SIZEOF_FLOAT;

    /**
     * Prepares the object.
     *
     * @param program The program to use.  FullFrameRect takes ownership, and will release
     *                the program when no longer needed.
     */
    public FullFrameRect(Program program) {
        mProgram = program;
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }


    public void release(boolean doEglCleanup) {
        if (mProgram != null) {
            if (doEglCleanup) {
                mProgram.release();
            }
            mProgram = null;
        }
    }

    /**
     * Returns the program currently in use.
     */
    public Program getProgram() {
        return mProgram;
    }

    /**
     * Changes the program.  The previous program will be released.
     */
    public void changeProgram(Program program) {
        mProgram.release();
        mProgram = program;
    }

    /**
     * Creates a texture object suitable for use with drawFrame().
     */
    public int createTextureObject() {
        return mProgram.createTextureObject();
    }

    /**
     * Draws a viewport-filling rect, texturing it with the specified texture object.
     */
    public void drawFrame(int textureId, float[] texMatrix) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        synchronized (mDrawLock) {
            mProgram.draw(IDENTITY_MATRIX, mRectDrawable.getVertexArray(), 0,
                    mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
                    mRectDrawable.getVertexStride(),
                    texMatrix, TEX_COORDS_BUF, textureId, TEX_COORDS_STRIDE);
        }
    }

    /**
     * Pass touch event down to the
     * texture's shader program
     *
     * @param ev
     */
    public void handleTouchEvent(MotionEvent ev) {
        mProgram.handleTouchEvent(ev);
    }
}
package com.example.shadertest.vo

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.shadertest.R
import org.apache.commons.io.IOUtils
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.nio.charset.Charset
import java.util.*

class Obj(context: Context) {
    private var verticesList = mutableListOf<String>()
    private var facesList = mutableListOf<String>()

    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var facesBuffer: ShortBuffer
    private var program: Int? = null

    init {
        try {
            val scanner = Scanner(context.assets.open("torus.obj"))
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if(line.startsWith("v")) {
                    verticesList.add(line)
                } else if (line.startsWith("f")) {
                    facesList.add(line)
                }
            }
            scanner.close()

            // Allocate buffers
            createBufferForVertices()
            createBufferForFaces()

            // Fill buffers
            fillVertices()
            fillFaces()

            // Convert vertex_shader.glsl to string
            val vertexShaderStream = context.resources.openRawResource(R.raw.vertex_shader)
            val vertexShaderSource = IOUtils.toString(vertexShaderStream, Charset.defaultCharset())
            vertexShaderStream.close()

            // Convert fragment_shader.glsl to string
            val fragmentShaderStream = context.resources.openRawResource(R.raw.fragment_shader)
            val fragmentShaderSource = IOUtils.toString(fragmentShaderStream, Charset.defaultCharset())
            fragmentShaderStream.close()

            // Create vertex shader
            val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
            GLES20.glShaderSource(vertexShader, vertexShaderSource)

            // Create fragment shader
            val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
            GLES20.glShaderSource(fragmentShader, fragmentShaderSource)

            // Compile shaders
            GLES20.glCompileShader(vertexShader)
            GLES20.glCompileShader(fragmentShader)

            // Create shader program
            program = GLES20.glCreateProgram().also { program ->
                GLES20.glAttachShader(program, vertexShader)
                GLES20.glAttachShader(program, fragmentShader)

                GLES20.glLinkProgram(program)
                GLES20.glUseProgram(program)
            }

        } catch (ex: Exception) {
            Log.e("Obj",ex.message)
        }


    }

    fun draw() {
        program?.let {program ->
            try {
                // Send vertex buffer to vertex shader
                val position = GLES20.glGetAttribLocation(program, "position")
                GLES20.glEnableVertexAttribArray(position)
                GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * 4, verticesBuffer)

                // Set camera position and look at
                val projectionMatrix = FloatArray(16)
                val viewMatrix = FloatArray(16)
                val productMatrix = FloatArray(16)

                Matrix.frustumM(
                    projectionMatrix, 0,
                    -1.0f, 1.0f,
                    -1.0f, 1.0f,
                    2.0f, 9.0f
                )

                Matrix.setLookAtM(
                    viewMatrix, 0,
                    0.0f, 3.0f, -4.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f
                )

                Matrix.multiplyMM(
                    productMatrix, 0,
                    projectionMatrix, 0,
                    viewMatrix, 0
                )


                val matrix = GLES20.glGetUniformLocation(program, "matrix")
                GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0)

                // Use face list to create faces
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer)

                GLES20.glDisableVertexAttribArray(position)
            } catch (ex: Exception) {
                println(ex.localizedMessage)
            }
        }
    }

    /**
     * Uses [facesList] which is a list of lines from OBJ containing face data
     * to create short buffer face list
     */
    private fun fillFaces() {
        for(face in facesList) {
            println(face)
            val vertexIndices = face.split(" ")

            val vertex1: Short = vertexIndices[1].toShort()
            val vertex2: Short = vertexIndices[2].toShort()
            val vertex3: Short = vertexIndices[3].toShort()

            facesBuffer.put((vertex1 - 1).toShort())
            facesBuffer.put((vertex2 - 1).toShort())
            facesBuffer.put((vertex3 - 1).toShort())
        }
        facesBuffer.position(0)

    }

    /**
     * Uses [verticesList] which is a list of lines from OBJ containing vertex data
     * to create float buffer vertex list
     */
    private fun fillVertices() {
        for (vertex in verticesList) {
            val coordinates = vertex.split(" ")
            val x =  coordinates[1].toFloat()
            val y =  coordinates[2].toFloat()
            val z =  coordinates[3].toFloat()
            verticesBuffer.put(x)
            verticesBuffer.put(y)
            verticesBuffer.put(z)
        }
        verticesBuffer.position(0)
    }

    /**
     * Initializes facesBuffer
     */
    private fun createBufferForFaces() {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(facesList.size * 3 * 2)
        buffer.order(ByteOrder.nativeOrder())
        facesBuffer = buffer.asShortBuffer()

    }

    /**
     * Initializes verticesBuffer
     */
    private fun createBufferForVertices() {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(verticesList.size * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        verticesBuffer = buffer.asFloatBuffer()
    }
}
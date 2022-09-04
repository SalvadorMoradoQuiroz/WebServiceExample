package com.salvadormorado.webserviceexample

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var editText_User: EditText
    private lateinit var editText_Pass: EditText
    private lateinit var button_LogIn: Button
    private lateinit var button_Register: Button
    private var progressBar: ProgressBar? = null
    private var progressAsyncTask: ProgressAsyncTask? = null
    private val hosting = "https://webserviceexamplesmq.000webhostapp.com/Services/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText_User = findViewById(R.id.editText_User)
        editText_Pass = findViewById(R.id.editText_Pass)
        button_LogIn = findViewById(R.id.button_LogIn)
        button_Register = findViewById(R.id.button_Register)
        progressBar = findViewById(R.id.progressBar)

        button_LogIn!!.setOnClickListener({
            if(!checkField(editText_Pass) && !checkField(editText_User)){
                val json = JSONObject()
                json.put("login", true)
                json.put("usuario", editText_User.text.toString())
                json.put("contrasena", editText_Pass.text.toString())
                progressAsyncTask = ProgressAsyncTask()
                progressAsyncTask!!.execute("POST", hosting + "login.php", json.toString())
            }
        })
    }


    fun disableView(view: View, isDisabled: Boolean) {
        if (isDisabled) {
            view.alpha = 0.5f
            view.isEnabled = false
        } else {
            view.alpha = 1f
            view.isEnabled = true
        }
    }

    fun checkField(editText: EditText): Boolean {
        if (editText!!.text.toString().isNullOrEmpty()) {
            Toast.makeText(applicationContext, "${editText.hint} es un campo requerido.", Toast.LENGTH_SHORT).show()
        }
        return editText!!.text.toString().isNullOrEmpty()
    }

    inner class ProgressAsyncTask : AsyncTask<String, Unit, String>() {
        val TIME_OUT = 50000

        //Antes de ejecutar
        override fun onPreExecute() {
            super.onPreExecute()
            disableView(button_LogIn!!, true)
            disableView(button_Register!!, true)
            disableView(editText_User!!, true)
            disableView(editText_Pass!!, true)
            progressBar!!.visibility = View.VISIBLE
        }

        //En ejecución en segundo plano
        override fun doInBackground(vararg params: String?): String {
            val url = URL(params[1])
            val httpClient = url.openConnection() as HttpURLConnection
            httpClient.readTimeout = TIME_OUT
            httpClient.connectTimeout = TIME_OUT
            httpClient.requestMethod = params[0]

            if (params[0] == "POST") {
                httpClient.instanceFollowRedirects = false
                httpClient.doOutput = true
                httpClient.doInput = true
                httpClient.useCaches = false
                httpClient.setRequestProperty("Content-Type", "application/json; charset-utf-8")
            }

            try {
                if (params[0] == "POST") {
                    httpClient.connect()
                    val os = httpClient.outputStream
                    val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                    writer.write(params[2])
                    writer.flush()
                    writer.close()
                    os.close()
                }
                if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                    val stream = BufferedInputStream(httpClient.inputStream)
                    val data: String = readStream(inputStream = stream)
                    Log.e("Data:", data)
                    return data
                } else if (httpClient.responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                    Log.e("ERROR:", httpClient.responseCode.toString())
                } else {
                    Log.e("ERROR:", httpClient.responseCode.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                httpClient.disconnect()
            }
            return null.toString()
        }

        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()

            bufferedReader.forEachLine { stringBuilder.append(it) }
            Log.e("StringBuider", "${stringBuilder.toString()}")

            return stringBuilder.toString()
        }
        //Cuando llegan los datos del servidor
        override fun onProgressUpdate(vararg values: Unit?) {
            super.onProgressUpdate(*values)
            if (!values.isNotEmpty() && values[0] != null)
                progressBar!!.visibility = View.VISIBLE
        }

        //Despues de la ejecuión
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            disableView(button_LogIn!!, false)
            disableView(button_Register!!, false)
            disableView(editText_User!!, false)
            disableView(editText_Pass!!, false)
            Log.e("Resultado:", "$result")
            progressBar!!.visibility = View.INVISIBLE

            if (!result.isNullOrBlank() && !result.isNullOrEmpty() && !checkField(editText_User) && !checkField(editText_Pass)
            ) {
                val parser: Parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject

                if (json.int("succes") == 1) {
                    val jsonFinal = JSONObject(result)
                    val dataUser = jsonFinal.getJSONArray("datosUsuario")
                    val idUser = dataUser.getJSONObject(0).getInt("idUsuario")
                    val nameUser = dataUser.getJSONObject(0).getString("nombreUsuario")
                    Log.e("Name user", "$nameUser")
                    Log.e("ID user", "$idUser")
                    val intent = Intent(applicationContext, Activity2::class.java).apply {
                        putExtra("nameUser", "$nameUser")
                        putExtra("idUser", "$idUser")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Usuarion invalido.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (result.isNullOrBlank() && result.isNullOrEmpty() && !checkField(editText_User) && !checkField(editText_Pass)
            ) {
                Toast.makeText(
                    applicationContext,
                    "No se recibio nada desde el servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            disableView(button_LogIn!!, false)
            disableView(button_Register!!, false)
            disableView(editText_User!!, false)
            disableView(editText_Pass!!, false)
            progressBar!!.visibility = View.INVISIBLE
        }
    }

}

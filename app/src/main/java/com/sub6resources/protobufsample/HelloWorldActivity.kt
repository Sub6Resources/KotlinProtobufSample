/*
 * Copyright 2015, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified 2018 Matthew Whitaker.
 * Changes:
 *   Converted to Kotlin
 *   Changed package names
 */

package com.sub6resources.protobufsample

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.sub6resources.protobufsample.helloworld.GreeterGrpc
import com.sub6resources.protobufsample.helloworld.HelloRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class HelloworldActivity : AppCompatActivity() {
    private var sendButton: Button? = null
    private var hostEdit: EditText? = null
    private var portEdit: EditText? = null
    private var messageEdit: EditText? = null
    private var resultText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helloworld)
        sendButton = findViewById(R.id.send_button)
        hostEdit = findViewById(R.id.host_edit_text)
        portEdit = findViewById(R.id.port_edit_text)
        messageEdit = findViewById(R.id.message_edit_text)
        resultText = findViewById(R.id.grpc_response_text)
        resultText!!.movementMethod = ScrollingMovementMethod()
    }

    fun sendMessage(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(hostEdit!!.windowToken, 0)
        sendButton!!.isEnabled = false
        resultText!!.text = ""
        GrpcTask(this)
                .execute(
                        hostEdit!!.text.toString(),
                        messageEdit!!.text.toString(),
                        portEdit!!.text.toString())
    }

    private class GrpcTask constructor(activity: Activity) : AsyncTask<String, Void, String>() {
        private val activityReference = WeakReference(activity)
        private var channel: ManagedChannel? = null

        override fun doInBackground(vararg params: String): String {
            val host = params[0]
            val message = params[1]
            val portStr = params[2]
            val port = if (TextUtils.isEmpty(portStr)) 0 else Integer.valueOf(portStr)
            return try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build()
                val stub = GreeterGrpc.newBlockingStub(channel)
                val request = HelloRequest.newBuilder().apply {
                    name = message
                }.build()
                val reply = stub.sayHello(request)

                reply.message

            } catch (e: Exception) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                pw.flush()

                String.format("Failed... : %n%s", sw)

            }

        }

        override fun onPostExecute(result: String) {
            try {
                channel!!.shutdown().awaitTermination(1, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            val activity = activityReference.get() ?: return
            val resultText = activity.findViewById(R.id.grpc_response_text) as TextView
            val sendButton = activity.findViewById(R.id.send_button) as Button
            resultText.text = result
            sendButton.isEnabled = true
        }
    }
}
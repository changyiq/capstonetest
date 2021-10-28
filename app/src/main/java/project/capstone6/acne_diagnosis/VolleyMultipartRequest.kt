package project.capstone6.acne_diagnosis

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.AuthFailureError
import com.android.volley.ParseError

import com.android.volley.toolbox.HttpHeaderParser
import java.lang.Exception
import com.android.volley.VolleyError
import java.io.*

open class VolleyMultipartRequest(
    method: Int, url: String?,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, mErrorListener) {
    private val twoHyphens = "--"
    private val lineEnd = "\r\n"
    private val boundary = "apiclient-" + System.currentTimeMillis()
    private val mHeaders: Map<String, String>? = null
    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return mHeaders ?: super.getHeaders()
    }

    override fun getBodyContentType(): String? {
        return "text/plain; charset=utf-8"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        try {
            // populate data byte payload
            val data = byteData
            if (data != null && data.size > 0) {
                dataParse(dos, data)
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * Custom method handle data payload.
     *
     * @return Map data part label with data byte
     * @throws AuthFailureError
     */
    @get:Throws(AuthFailureError::class)
    private val byteData: Map<String, DataPart>?
        private get() = null

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {
        return try {
            Response.success(
                response,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse?) {
        mListener?.onResponse(response);
    }

    override fun deliverError(error: VolleyError?) {
        mErrorListener!!.onErrorResponse(error)
    }

    /**
     * Parse data into data output stream.
     *
     * @param dataOutputStream data output stream handle file attachment
     * @param data             loop through data
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun dataParse(dataOutputStream: DataOutputStream, data: Map<String, DataPart>) {
        for (entry: Map.Entry<String, DataPart> in data.entries) {
            buildDataPart(dataOutputStream, entry.value, entry.key)
        }
    }

    /**
     * Write data file into header and data output stream.
     *
     * @param dataOutputStream data output stream handle data parsing
     * @param dataFile         data byte as DataPart from collection
     * @param inputName        name of data input
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildDataPart(
        dataOutputStream: DataOutputStream,
        dataFile: DataPart,
        inputName: String
    ) {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; name=\"" +
                    inputName + "\"; filename=\"" + dataFile.fileName + "\"" + lineEnd
        )
        if (dataFile.type != null && !dataFile.type.trim { it <= ' ' }.isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.type + lineEnd)
            Log.e("Dataoutput", dataOutputStream.toString())
        }
        dataOutputStream.writeBytes(lineEnd)
        val fileInputStream = ByteArrayInputStream(dataFile.content)
        var bytesAvailable: Int = fileInputStream.available()
        val maxBufferSize = 1024 * 1024
        var bufferSize = Math.min(bytesAvailable, maxBufferSize)
        val buffer = ByteArray(bufferSize)
        var bytesRead: Int = fileInputStream.read(buffer, 0, bufferSize)
        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize)
            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
        }
        dataOutputStream.writeBytes(lineEnd)
    }

    class DataPart {
        var fileName: String? = null
            private set
        var content: ByteArray? = null
            private set
        val type: String? = null

        constructor() {}
        constructor(name: String?, data: ByteArray) {
            fileName = name
            content = data
        }
    }

}
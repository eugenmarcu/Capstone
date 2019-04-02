package com.example.android.capstone.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public final class NetworkUtils {
    public static final int HTTP_OK = 200;
    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECTION_TIMEOUT = 15000;


    /**
     * Create a private constructor because no one should ever create a {@link NetworkUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name NetworkUtils (and an object instance of NetworkUtils is not needed).
     */
    private NetworkUtils() {
    }

    public static String fetchImageUrl(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        String image = extractImageFromJson(jsonResponse);
        return image;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    //The goal of this method is to return a URL object for the provided
    //input string url
    private static URL createUrl(String stringUrl) {
        //Ideally we will call the URL constructor and pass in the string url
        //URL url=new URL(stringUrl)-- create a error message malformed url exception
        //check the URL documentation: If you pass a input parameter stringUrl
        // that cannot be converted into URL-- it throws malformedURLException
        //use atl+ enter and android will automatically fix the code.
        //It becomes Url url=null;
        //The url object instance is set to null which means an empty value
        URL url = null;
        try {
            // create a url object
            url = new URL(stringUrl);
            //The url exception is caught here
        } catch (MalformedURLException e) {
            //Print the error stack in detailed format OR use android log methods
            //that include include out own log_tag and custom message
            e.printStackTrace();
            //the Log.e method can take en exception as its third argument
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    //In the makeHttpRequest, we should only perform http request if the
    //url is not null.  It does not make sense to make a connection to an
    //invalid url
    private static String makeHttpRequest(URL url) throws IOException {
        //initialize jsonResponse to an empty string
        String jsonResponse = "";

        // If the URL is null, then return early.
        //At the beginning of the method, let's check whether the url is null
        if (url == null) {
            //if the url is null, let's exit the method early by returning a json response that
            //contains an empty string
            return jsonResponse;
        }
        //urlConnection variable is a type HttpURLConnection
        //inputStream variable is a type InputStream
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            // create a urlConnection object to perform a HTTP request to the url object
            // Let’s create a connection to a given url using GET method:
            //setting up http request
            //url.openConnection();-- opening connection on the url object-- this
            //method returns an urlConnection instance but it may throw an
            //IO exception if there is a problem opening the connection.  It throws
            //other exception such as SocketTimeoutException
            urlConnection = (HttpURLConnection) url.openConnection();
            //HttpUrlConnection class allows setting the connect and read timeouts.
            // These values define the interval of time to wait for the connection to the server
            // to be established or data to be available for reading.
            urlConnection.setReadTimeout(READ_TIMEOUT /* milliseconds */);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT /* milliseconds */);
            //create a connection to a given url using GET method:
            urlConnection.setRequestMethod("GET");
            //Let's check the response code
            //Establish HTTP connection with the server

            urlConnection.connect();
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            // Reading the response of the request can be done
            // by parsing the InputStream of the HttpUrlConnection instance.
            //To execute the request we can use the getResponseCode(),
            // connect(), getInputStream() or getOutputStream() methods:
            //After establishing connection with the server
            //you want to check the response code by calling urlConnection.getResponseCode()
            //it return an integer ==200
            //using the if statement we check if the response code is 200
            //if yes, we
            if (urlConnection.getResponseCode() == HTTP_OK) {
                //we read from the getInputStream, It return inputStream of data that is
                //bytes of data
                //if the response code is 200, we read from the InputStream and
                //extract the json response
                //InputStream allow to retrieve data one at a time
                inputStream = urlConnection.getInputStream();
                //Extract the jsonresponse
                //This is a helper method that read data that comes from the inputStream
                //reading raw binary of data that is 0 and 1, with no specific
                //instructions of what the data represent
                //The data could be an image, sound , geojson data
                jsonResponse = readFromStream(inputStream);
            } else {
                //Otherwise, if there is an error response that is not equal to 200 or
                //error code we go down here
                //we create a log message
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the image.", e);
        } finally {
            //disconnect the connection if the urlConnection is not null
            if (urlConnection != null) {
                //disconnect the HTTPConnection
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                // close the inputStream once the parsing of json is completed

                inputStream.close();
            }
        }
        //returning a jsonResponse
        //If the return value of the makeHTTPRequest is an empty string
        //we have to make sure that the method that takes the jsonResponse
        // as input is handling the empty string
        //Let's look at extractFeatureFromJson method
        //Otherwise, if there is an response that is not equal to 200 or
        //error code we go down here return the jsonResponse that was
        //initialized earlier to an empty string
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    //Data coming from the InputStream is a text
    //In the readFromStream method, we have an inputStream, and we want to return
    //a string containing the contents of the stream
    private static String readFromStream(InputStream inputStream) throws IOException {
        //We create a new StringBuilder and we start appending all the lines of text
        //available to the bufferedreader.  We set up the builder then the reader and the
        //BufferedReader will read a line of text
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            //we will use the InputStreamReader to handle the text which is
            //binary or raw data and convert to readable characters
            //InputStreamReader allow us to read a single character at a time by passing
            // a parameter character set or charset to the constructor
            //A charset specify how to translate inputstream raw data to readable characters
            //utf-8 is the unicde character encoding used for every piece of text
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            //wrap an InputStreamReader object instance in a BufferedReader
            //BufferedReader read lines of data
            // We set up the builder then the reader and the
            //BufferedReader will read a line of text
            BufferedReader reader = new BufferedReader(inputStreamReader);
            //Read the line
            String line = reader.readLine();
            // We set up the builder then the reader and the
            //BufferedReader will read a line of text.  When the line is not null
            //we will append the line to the string buffer and read the next line
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        //The BufferedReader will run out of lines, and we will use toString method and
        //return the final output from the string builder
        return output.toString();
    }

    private static String extractImageFromJson(String dogJSON) {
        // If the JSON string is empty or null, then return early.
        //Before Extracting information from the jsonResponse we should check
        //if the  parameter is an empty string or null by calling TextUtils.isEmpty
        //and passing in the String
        //if the string is empty, the expression is true and will
        // return early from the method.  We have returned null because there is
        // no valid event  object the jsonResponse
        //Note that the extractFeatureFromJson method returns and event
        //datatype
        if (TextUtils.isEmpty(dogJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding Environment news to
        String imageURl = "";

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(dogJSON);

            imageURl = baseJsonResponse.optString("message");
        } catch (JSONException e) {
            Log.e("NetworkUtils", "Problem parsing the JSON results", e);
        }
        return imageURl;
    }

}
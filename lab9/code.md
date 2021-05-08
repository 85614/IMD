## 在manifest添加permissions以使用网络和获取网络状态

```xml
<?xml version="1.0" encoding="utf-8"?>

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.example.android.whowroteitloader">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name="com.example.android.whowroteitloader.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>

</manifest>
```

## MainActivity 

- 实现`LoaderManager.LoaderCallbacks<String>`的接口
- 使用`ConnectivityManager`检查网络状态

```java
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private EditText mBookInput;
    private Spinner protocolSpinner;
    private TextView sourceText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = findViewById(R.id.bookInput);
        protocolSpinner= findViewById(R.id.protocol);
        sourceText = findViewById(R.id.sourceText);

        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    /**
     * onClick handler for the "Search Books" button.
     *
     * @param view The view (Button) that was clicked.
     */
    public void searchBooks(View view) {
        // Get the search string from the input field.
        String queryString = mBookInput.getText().toString();

        // Hide the keyboard when the button is pushed.
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {

            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", protocolSpinner.getSelectedItem().toString() + queryString);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);

            sourceText.setText(R.string.loading);
        }
        // Otherwise update the TextView to tell the user there is no
        // connection, or no search term.
        else {
            if (queryString.length() == 0) {
                sourceText.setText(R.string.no_search_term);
            } else {
                sourceText.setText(R.string.no_network);
            }
        }
    }


    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = "";
        if (args != null) {
            queryString = args.getString("queryString");
        }

        return new BookLoader(this, queryString);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        try {
            // Convert the response into a JSON object.
            JSONObject jsonObject = new JSONObject(data);
            sourceText.setText(String.valueOf(jsonObject));
        } catch (Exception e) {
            // If onPostExecute does not receive a proper JSON string,
            // update the UI to show failed results.
            sourceText.setText(R.string.no_results);
            e.printStackTrace();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Do nothing.  Required by interface.
    }
}

```

## BookLoader类

- `BookLoader`是`AsyncTaskLoader`的子类

```java
public class BookLoader extends AsyncTaskLoader<String> {

    private String mQueryString;

    BookLoader(Context context, String queryString) {
        super(context);
        mQueryString = queryString;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        return NetworkUtils.getBookInfo(mQueryString);
    }
}
```



## NetworkUtils类发送请求接受数据

```java
public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    // Constants for the various components of the Books API request.
    //


    /**
     * Static method to make the actual query to the Books API.
     *
     * @param queryString the query string.
     * @return the JSON response string from the query.
     */
    static String getBookInfo(String queryString) {

        // Set up variables for the try block that need to be closed in the
        // finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            // Build the full query URI, limiting results to 10 items and
            // printed books.
            Uri builtURI = Uri.parse(queryString).buildUpon().build();

            Log.d("DEBUG", "builtURI:" + builtURI.toString());
            // Convert the URI to a URL.
            URL requestURL = new URL(builtURI.toString());
            // Open the network connection.
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                // Add the current line to the string.
                builder.append(line);

                // Since this is JSON, adding a newline isn't necessary (it won't
                // affect parsing) but it does make debugging a *lot* easier
                // if you print out the completed buffer for debugging.
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty.  Exit without parsing.
                return null;
            }

            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the connection and the buffered reader.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Write the final JSON response to the log
        Log.d("DEBUG", "bookJSONString: "+bookJSONString);

        return bookJSONString;
    }
}
```



## activity_main.xml 添加Spinner以切换协议，添加ScrollView和TextView展示网页源代码

```xml
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="16dp"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/instructions"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/instructions"
        android:textAppearance="@style/TextAppearance.AppCompat.Title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

    <Spinner
        android:id="@+id/protocol"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:entries="@array/protocol"
        android:background="@null"
        android:textSize="18sp"
        app:layout_constraintBottom_toBottomOf="@+id/bookInput"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="@+id/bookInput" />

    <EditText
        android:id="@+id/bookInput"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:hint="@string/input_hint"
        android:text="@string/input_hint"
        android:inputType="text"
        android:textSize="18sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@+id/protocol"
        app:layout_constraintTop_toBottomOf="@+id/instructions" />

    <Button
        android:id="@+id/searchButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:onClick="searchBooks"
        android:text="@string/button_text"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/bookInput"/>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/searchButton">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <TextView
                android:id="@+id/sourceText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Page Source will appear here" />
        </LinearLayout>
    </ScrollView>

</android.support.constraint.ConstraintLayout>
```

## 资源文件添加string-array以表示不同的网络协议

```xml
<resources>
    <string name="app_name">Get Web Page Source Code</string>

    <!-- Strings in activity_main view -->
    <string name="instructions">Enter URL:</string>
    <string name="button_text">GET PAGE SOURCE</string>
<!--    <string name="input_hint">shuapi.jiaston.com/book/3143/</string>-->
<!--    <string name="input_hint">sou.jiaston.com/search.aspx?key=3&amp;page=1&amp;siteid=app2</string>-->
    <string name="input_hint">www.example.com</string>
    <!-- User messages for in-process queries and error results. -->
    <string name="loading">Loading…</string>
    <string name="no_results">"No Results Found"</string>
    <string name="no_search_term">Please enter a URL</string>
    <string name="no_network">Please check your network connection and try again.</string>
    <string-array name="protocol">
        <item>http://</item>
        <item>https://</item>
    </string-array>
</resources>
```


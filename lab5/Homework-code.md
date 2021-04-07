## 在MainActivity中增加一个函数进行拍照
```java
    public void takeAPicture(View view) {
        // Parse the location and create the intent.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Find an activity to handle the intent, and start that activity.
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this intent!");
        }
    }
```


## 在activity_main.xml LinearLayout 中增加一个Button
```xml
    <Button
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="takeAPicture"
        android:text="@string/button_take_a_picture" />
```


## 在strings.xml中增加一个string 资源
```xml
<string name="button_take_a_picture">TAKE A PICTURE</string>
```


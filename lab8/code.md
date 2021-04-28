

# 实现共享过渡元素

- Enable Window-content transitions in the app theme.

  ```xml
  <resources>
      <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
          <item name="android:windowActivityTransitions">true</item>
      </style>
  </resources>
  ```

- 设置相同的`android:transitionName`属性.

  ```xml
         <ImageView
              android:id="@+id/sportsImage"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:transitionName="banner"
              android:adjustViewBounds="true"/>
  ```

  ```xml
          <ImageView
              android:id="@+id/sportsImageDetail"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:transitionName="banner"
              android:adjustViewBounds="true" />
  ```

- 使用 `ActivityOptions.makeSceneTransitionAnimation()`方法

  ```java
  		@Override
          public void onClick(View view) {
              Sport currentSport = mSportsData.get(getAdapterPosition());
              Intent detailIntent = new Intent(mContext, DetailActivity.class);
              detailIntent.putExtra("title", currentSport.getTitle());
              detailIntent.putExtra("image_resource",
                      currentSport.getImageResource());
  
              ActivityOptions options = ActivityOptions
                      .makeSceneTransitionAnimation((Activity) mContext,
                              mSportsImage, "switch");
  
              mContext.startActivity(detailIntent, options.toBundle());
          }
  ```

  
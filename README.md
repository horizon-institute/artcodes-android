Artcodes
=============

Artcodes marks a paradigm shift in visual recognition, offering difference to discerning brands. We design visually beautiful images and encode them, resulting in the same interactivity as that of the QR code but with a more visually engaging and playful experience. Images can be the identical with unique codes or the opposite visually unique with identical codes. This interplay offers a new opportunity for visual interaction within, product, packaging, service and interaction design.

------------------------------------
Adding Artcodes to your project
====================================

1. Download the aestheticodes respository [zip file](https://github.com/horizon-institute/aestheticodes-ios/archive/master.zip) or clone it
2. Copy the aestheticodes-core sub-project your project
3. Add the project to your build.gradle dependencies:

    ```gradle
    compile project(":artcodes-scanner")
    ```

4. Create settings.gradle with the line:

    ```gradle
    include 'artcodes-scanner'
    ```

------------------------------------
Basic usage
====================================

To create the Artcode reader

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.ac.horizon.artcodes.scanner.ScannerActivity;

// Create and configure artcode experience
Experience experience = new Experience();

// Create intent
Intent intent = new Intent(ScannerActivity.class);

// Put experience in intent
Gson gson = new GsonBuilder().create();
intent.putExtra("experience", gson.toJson(experience));

// Start artcode reader activity
startActivityForResult(intent, ARTCODE_REQUEST);
```

To handle the response, implement [onActivityResult](http://developer.android.com/reference/android/app/Activity.html)

```java
protected void onActivityResult(int requestCode, int resultCode, Intent data)
{
  if (requestCode == ARTCODE_REQUEST)
  {
    if (resultCode == RESULT_OK)
    {
      // Marker found
      String markerCode = data.getStringExtra("marker");
    }
  }
}
```

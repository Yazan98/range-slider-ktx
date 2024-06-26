# RangeSliderKTX

RangeSliderKTX is a versatile and customizable Android library for implementing range selection sliders in your apps with ease. Whether you need to select a range of dates, prices, or any other numeric values, RangeSliderKTX provides a sleek and intuitive user interface for users to interact with.


## Features

* <strong>Range Selection</strong>: Allow users to easily select a range of values by dragging two thumbs along a track.
* <strong>Customizable Appearance</strong>: Customize the appearance of the slider, including track color, thumb color, and track height, to match your app's design.
* <strong>Flexible Configuration</strong>: Configure the slider to work with various types of numeric ranges, including integers, floats, and dates.
* <strong>Listener Support</strong>: Set listeners to receive updates when the range selection changes, enabling you to react to user interactions programmatically.
* <strong>Easy Integration</strong>: Simple integration with your Android app using Gradle, with Kotlin extensions (KTX) for seamless development experience.

## Slider Screenshots

<img width="358" alt="Screenshot 2024-05-04 at 1 02 30 PM" src="https://github.com/Yazan98/range-slider/assets/29167110/ee1a4b9e-2d98-43a5-a63a-ad589cf34667">

<img width="348" alt="Screenshot 2024-05-04 at 1 02 48 PM" src="https://github.com/Yazan98/range-slider/assets/29167110/ffcd86c5-39df-4acb-a3df-95e3dec4b841">

<img width="339" alt="Screenshot 2024-05-04 at 1 03 18 PM" src="https://github.com/Yazan98/range-slider/assets/29167110/5aad22eb-6feb-4b23-a02e-7c28dbcdd52c">

<img width="455" alt="Screenshot 2024-05-04 at 1 09 07 PM" src="https://github.com/Yazan98/range-slider/assets/29167110/73261889-b8ce-4b75-b10a-527ef81ab82d">

## Getting Started

To get started with RangeSliderKTX, follow these steps:

1. Add Dependency: Add the library dependency to your project's build.gradle file:

```
    // Gradle
    implementation 'com.yazantarifi:range-slider:1.0.0'
    
    // Gradle Splitted Version
    implementation group: 'com.yazantarifi', name: 'slider', version: '1.0.0'
    
    // Gradle KTX
    implementation("com.yazantarifi:slider:1.0.0")
```

2. Integrate into Layout: Integrate RangeSliderKTX into your layout XML file:

```
<com.yazantarifi.slider.RangeSliderView
        android:layout_width="match_parent"
        android:layout_height="90dp"
        android:id="@+id/slider"
        app:rectangle_height="13dp"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        app:slider_from_progress="10"
        app:slider_to_progress="80"
        app:corner_radius="30dp"
        app:thumb_size="15dp"
        app:thumb_single_color="false"
        app:thumb_color="@color/active_color"
        app:thumb_second_color="@color/background_color_screen"
        app:slider_min_value="0"
        app:slider_max_value="100"
        app:slider_active_color="@color/active_color"
        app:slider_background="@color/background_color" />
```

3. Customize Appearance: Customize the appearance and behavior of the slider using XML attributes or programmatically in your Kotlin code.
4. Handle Events: Set listeners to handle events when the range selection changes or when the user interacts with the slider.

## Examples

```
binding.slider.let {
    it.onUpdateRangeValues(0f, 100f) // Initial Values Minimum and Maximum

    it.onAddRangeListener(object : RangeSliderListener {
         override fun onRangeProgress(
                    fromValue: Float,
                    toValue: Float,
                    isFromUser: Boolean
         ) {
                    if (isFromUser) { // Event if the Change From User Movement or from Code
                        binding.fromValue.setText(fromValue.toString())
                        binding.toValue.setText(toValue.toString())
                    }
         }

         override fun onThumbMovement(value: Float, thumbIndex: Int, isFromUser: Boolean) {

         }
    })
}

```

> Note, The Included App in this Repo has 3 Examples

## Slider Customization Attributes
| Attribtes            | Value                         | Description                                     |
|----------------------|-------------------------------|-------------------------------------------------|
| rectangle_height     | Dimention Number (Dp, Sp, Px) | The Height of the Slider                        |
| corner_radius        | Dimention Number (Dp, Sp, Px) | Corner Radius of the Slider Progress            |
| slider_background    | Color Resource                | The Background Color of the Range behind Slider |
| slider_active_color  | Color Resource                | The Active Color of the Progress Area           |
| slider_from_progress | Float                         | Current From Thumb Progress Value               |
| slider_to_progress   | Float                         | Current To Thumb Progress Value                 |
| slider_min_value     | Float                         | Minimum Value of the Range                      |
| slider_max_value     | Float                         | Maximum Value of the Range                      |
| thumb_size           | Dimention Number (Dp, Sp, Px) | Thumb Size (The 2 Circles for Progress)         |
| thumb_color          | Color Resource                | Thumb Color (The 2 Circles for Progress)        |
| thumb_second_color   | Color Resource                | Second Thumb Color to Create Layers             |
| thumb_single_color   | Boolean                       | To Draw the Thumbs With One Color or 2 Colors   |
| slider_step_size     | Float                         | Steps Between Range to make Thumbs Jumping      |

> Note Anything Customizable from Xml has a Function to change it in Runtime (for Dynamic UIs, Api Response Colors)

## Library Versions
1. Library Version: 1.0.0
2. Minimum Supported Version: 26
3. Target Version: 34

## License
RangeSliderKTX is released under the MIT License. See LICENSE for details.

## Contributing
Contributions are welcome! Please feel free to open issues or submit pull requests to help improve this project.

## Commands to Publish
```
    ./gradlew build
    ./gradlew slider:publishReleasePublicationToSonatypeRepository
```
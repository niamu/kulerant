# kulerant

> #### colorant
> [**kuhl**-er-*uh* nt]
>
> noun
> 1. something used as a coloring matter; pigment; dye.


Kulerant is a Clojure library that helps you discover visually pleasing colour themes for web designs. Themes are pulled from the [Adobe Color CC](http://color.adobe.com) (previously named Kuler) API. The swatches are then dumped into a hash-map of color name and hex color.

Kulerant has an opiniated way of sorting swatch colors of a theme and is not expected to work well with any themes found on Adobe Color CC. Kulerant likes to sort swatches in the following order:

1. The closest colour to white (commonly expected to be a background color)
2. Second closest color to white (can sometimes be used as a light shadow and works well as a gradient color combined with color #3)
3. Furthest color from white (hopefully a dark color that will work well as a contrasting text color on the #1 background color)
4. Secondary accent color (not as vibrant/colorful as the last color)
5. Primary accent color (most vibrant color and useful for buttons)

This works well for my personal preferences in aesthetics. In time, perhaps Kulerant will have other algorithms for sorting swatches for other aesthetics (possibly darker web designs with light on dark).

## Usage

Add the following dependency to your `project.clj` file:

```Clojure
[kulerant "0.1.0"]
```

```Clojure
user=> (require '[kulerant.core :as kulerant])
nil
user=> (def api-key "177DDC0741F78C9148F583B886ADBCAC")
#'user/api-key
user=> (kulerant/theme->map api-key 1504814)
{:name "Furious",
 :author "dezi9er",
 :id "1504814",
 :href "https://color.adobe.com/Furious-color-theme-1504814/",
 :swatches {"Pomegranate" "#E0401C",
            "Anzac" "#E6B051",
            "Shark" "#272F30",
            "Astra" "#F7EDB7",
            "Roof Terracotta" "#9E2B20"}}
nil
```

You can also provide the full URL for a theme as an argument to `theme->map` instead...

```Clojure
user=> (kulerant/theme->map api-key
                            "https://color.adobe.com/Furious-color-theme-1504814/")
```

To produce an indexed map of a theme's swatches that are sorted for web design as described above, use `color`...

```Clojure
user=> (def theme (kulerant/theme->map api-key 1504814))
#'user/theme
user=> (kulerant/color theme)
{0 "#F7EDB7", 1 "#E6B051", 2 "#272F30", 3 "#9E2B20", 4 "#E0401C"}
user=> (-> theme kulerant/color (get 0))
"#F7EDB7"
```

## Design Examples

The following are examples of what `kulerant` can do when applied to CSS.

[Living room colors](https://color.adobe.com/Living-room-colors-color-theme-5822057/)

![Heroi.cc - Living room colors](https://raw.githubusercontent.com/niamu/kulerant/master/resources/samples/hecoicc-sample5822057.png)

[Saltillo](https://color.adobe.com/Saltillo-color-theme-1747003/)

![Heroi.cc - Saltillo](https://raw.githubusercontent.com/niamu/kulerant/master/resources/samples/heroicc-sample1747003.png)

[Crying in the Rain](https://color.adobe.com/Crying-in-the-Rain-color-theme-1344922/)

![Heroi.cc - Crying in the Rain](https://raw.githubusercontent.com/niamu/kulerant/master/resources/samples/heroicc-sample1344922.png)

## License

Copyright © 2016 Brendon Walsh

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

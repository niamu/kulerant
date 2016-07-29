(ns kulerant.core
  (:require [kulerant.colors :refer [colors]]
            [garden.color :refer [as-rgb]]
            [clojure.string :as string]
            #?@(:clj [[org.httpkit.client :as http]
                      [clojure.data.json :as json]])))

(defn- pow [x n]
  (reduce * (repeat n x)))

(defn- color-diff
  "Calculate the distance between two colors"
  [[r1 g1 b1] [r2 g2 b2]]
  (+ (pow (- r1 r2) 2)
     (pow (- g1 g2) 2)
     (pow (- b1 b2) 2)))

(defn- mean
  [coll]
  (let [sum (apply + coll)
        total (count coll)]
    (if (pos? total)
      (/ sum total)
      0)))

(defn- standard-deviation
  [coll]
  (let [avg (mean coll)
        squares (for [x coll]
                  (let [x-avg (- x avg)]
                    (* x-avg x-avg)))
        total (count coll)]
    (->> (/ (apply + squares)
            (- total 1))
         #?(:clj (Math/sqrt)
            :cljs (.sqrt js/Math)))))

(defn- rgb
  "Return the RGB values of a given color"
  [c]
  (vals (select-keys (as-rgb c) [:red :green :blue])))

(defn- colorfulness
  "Attempts to calculate 'colorfulness' as determined by the
   distance of RGB channels from one another within a color"
  [c]
  (standard-deviation (rgb c)))

(defn- whiteness
  "Calculate distance of color from pure white"
  [c]
  (color-diff (rgb c) [255 255 255]))

(defn- colors->diffmap
  "Produce a map of all colors and the distance from the provided color"
  [[r g b] colors]
  (reduce (fn [accl c]
            (assoc accl
                   (color-diff (rgb (first c))
                               [r g b])
                   (second c)))
          {}
          colors))

(def ^:private color->name
  "Determine the closest matching color name"
  (memoize
   (fn [[r g b] colors]
     (let [diffmap (colors->diffmap [r g b] colors)]
       (get diffmap (->> diffmap keys (apply min)))))))

(defn- theme-id
  "Coerce theme url into theme id"
  [s]
  (cond-> s
    (string/starts-with? s "http") (-> (re-matches #".*-theme-([0-9]+).*")
                                       second)))

(defn- name->keyword
  "Keywordize a theme name"
  [n]
  (-> n
      (string/replace #"\'|\(|\)" "")
      (string/replace #"&" "and")
      (string/replace #"[ ]+" "-")
      (string/lower-case)
      keyword))

(defn color
  "Produces indexed map of colors.
   Assumes 3 neutral colors and 2 accent colors as input from swatches. "
  [theme]
  (let [swatches (-> theme :swatches vals)
        sorted-white (reverse (sort-by whiteness swatches))
        accent-colors (sort-by colorfulness
                               (drop-last 2 sorted-white))]
    (apply merge (map-indexed #(hash-map %1 %2)
                              (apply conj
                                     accent-colors
                                     (remove (set accent-colors)
                                             sorted-white))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API Methods
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


#?(:clj
   (defn- request
     "HTTP Request to Adobe Color's API and return parsed JSON results"
     [url api & [params]]
     (-> (http/get url {:headers {"x-api-key" api}
                        :query-params params})
         deref
         :body
         (json/read-str :key-fn keyword))))

#?(:clj
   (defn theme->map
     "Convert theme to a normalized map with color names for swatches"
     [api theme]
     (let [k (request (str "https://color.adobe.com/api/v2/themes/"
                           (theme-id theme))
                      api)]
       {:name (:name k)
        :author (get-in k [:author :name])
        :id (:id k)
        :href (:href k)
        :swatches (reduce (fn [accl swatch]
                            (let [rgb (rgb (:hex swatch))]
                              (assoc accl
                                     (color->name rgb colors)
                                     (str "#" (:hex swatch)))))
                          {}
                          (:swatches k))})))

#?(:clj
   (defn themes->map
     "Convert themes to a normalized map with color names for swatches"
     [api themes]
     (apply merge (map #(hash-map (name->keyword (:name %)) %)
                       (map #(theme->map api %) themes)))))

#?(:clj
   (defn popular-themes
     "Popular themes with a default limit of 20"
     [api & limit]
     (->> (:themes (request "https://color.adobe.com/api/v2/themes"
                            api
                            {:filter "public"
                             :startIndex 0
                             :maxNumber (or limit 20)
                             :sort "like_count"
                             :time "all"
                             :metadata "all"}))
          (map :id)
          (themes->map api))))

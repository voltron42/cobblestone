(ns cobblestone.spec
  (:require [clojure.spec.alpha :as s]
            [cobblestone.color :as colors]))

(def coordinate #"[a-z]([a-z])?")

(defn- coord-check [func]
  (fn [value]
    (let [str-val (func value)]
      (if-not (or (nil? str-val) (re-matches coordinate str-val))
        false
        (let [[low high] (map identity str-val)
              high (if (nil? high) low high)]
          (>= 0 (.compareTo (str low) (str high))))))))

(def tile-pattern-1 #"([a-z]([0-9])?)*[a-z]([|](([a-z]([0-9])?)*[a-z])?){0,15}")

(s/def ::color-component (s/and int? (partial <= 0) (partial >= 255)))

(s/def ::rgb-color
  (s/cat :r ::color-component
         :g ::color-component
         :b ::color-component))

(s/def ::hex-color (s/and keyword? #(re-matches #"[#][0-9A-F]{3}([0-9A-F]{3})?" (str %))))

(def color-names (set (mapv #(keyword (name %)) (keys colors/css))))

(def pantone-colors (set (mapv #(keyword (name %)) (keys colors/pantone))))

(s/def ::named-color color-names)

(s/def ::pantone-color pantone-colors)

(s/def ::color (s/alt :none #{:none}
                      :hex ::hex-color
                      :rgb ::rgb-color
                      :named ::named-color
                      :pantone ::pantone-color))

(s/def ::tile (s/and string? (partial re-matches tile-pattern-1)))

(s/def ::tile-name keyword?)

(s/def ::tile-set (s/map-of ::tile-name ::tile))

(s/def ::tile-loc (s/and keyword? (coord-check namespace) (coord-check name)))

(s/def ::palette (s/and vector?
                        (s/+ ::color)))

(s/def ::palette-name keyword?)

(s/def ::palette-set (s/map-of ::palette-name ::palette))

(s/def ::action #{:turn-right :turn-left :flip-down :flip-over})

(s/def ::tile-listing
  (s/and vector?
         (s/cat :name ::tile-name
                :palette-name ::palette-name
                :actions (s/* ::action)
                :locs (s/+ ::tile-loc))))

(s/def ::tile-doc
  (s/and vector?
         (s/cat :tiles ::tile-set
                :palettes ::palette-set
                :pixel-size (s/? int?)
                :list (s/+ ::tile-listing))))

(s/def ::doc-name keyword?)

(s/def ::tile-doc-set
  (s/and vector?
         (s/cat :tiles ::tile-set
                :palettes ::palette-set
                :pixel-size (s/? int?)
                :docs (s/map-of ::doc-name (s/and vector? (s/+ ::tile-listing))))))
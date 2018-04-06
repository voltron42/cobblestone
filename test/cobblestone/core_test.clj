(ns cobblestone.core-test
  (:require [clojure.test :refer :all]
            [cobblestone.core :as pixel]
            [cobblestone.spec :as tiles]
            [clojure.xml :as xml]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [picasso.core :as img]
            [clojure.spec.alpha :as s])
  (:import (java.io FileOutputStream ByteArrayInputStream)))

(defn- build-files [xml-file img-file tile-doc]
  (let [svg (pixel/build-svg-from-tile-doc tile-doc)
        _ (pp/pprint svg)
        input (-> svg
                  (xml/emit-element)
                  (with-out-str)
                  (.getBytes)
                  (ByteArrayInputStream.))
        xml (->> svg
                 (xml/emit)
                 (with-out-str))
        out (FileOutputStream. (io/file (str "resources/" img-file)))]
    (spit (str "resources/" xml-file) xml)
    (img/rasterize :png {} input out)))

(def ^:private doc (edn/read-string (slurp "resources/practicetiles/pixel-tiles.edn")))

(deftest test-pixel-small
  (let [[tiles palettes size {:keys [single]}] doc]
    (build-files "one-tile.xml" "small.png"
                 (into [tiles palettes] single))))

(deftest test-pixel-big
  (let [[tiles palettes size {:keys [simple-room]}] doc]
    (build-files "tiles.xml" "map.png"
                 (into [tiles palettes size] simple-room))))

(deftest test-pixel-w-hall
  (let [[tiles palettes size {:keys [room-with-door]}] doc]
    (build-files "open.xml" "door.png"
                 (into [tiles palettes size] room-with-door))))

(deftest test-explode-tile
  (let [full "aaaaaaaaaaaaaaaa|abbbbbbbbdcbbbbb|abcccdddddcbddbd|abccccddddcddddd|abdccccdddcddddd|abddcccccccccccc|abdddcccbbbbdcbb|abdddcccdddddcdd|abdddcbdccdddcdd|abbddcbdcccccccc|abdddcccccccbbbb|abdddcdddcccdddd|abbddcbddcbccccc|acccccbddcbdccdd|addddcbddcbdcccc|abdddcbddcbdcdcc"
        abbrev "a|ab7dcb|abc2d4cbddbd|abc3d3cd|abdc3d2cd|abddc|abd2c2b3dcb|abd2c2d4cd|abd2cbdccd2cd|abbddcbdc|abd2c6b|abd2cd2c2d|abbddcbddcbc|ac4bddcbdccd|ad3cbddcbdc|abd2cbddcbdcdc"
        smallest "a"
        all-As (str/join "|" (repeat 16 (apply str (repeat 16 "a"))))]

    (is (= full (pixel/explode-tile full)))
    (is (= full (pixel/explode-tile abbrev)))
    (is (= all-As (pixel/explode-tile all-As)))
    (is (= all-As (pixel/explode-tile smallest)))

    (pp/pprint (str/split (pixel/explode-tile "a||b||||a||||b||||a") #"[|]"))
    (pp/pprint (str/split (pixel/explode-tile "a|b||a||b||a||b||a||b||a") #"[|]"))))

(deftest test-pixel-stairs
  (let [[tiles palettes size {:keys [endless-stairs]}] doc
        single (into [tiles palettes size] endless-stairs)]
    (println (pr-str (s/explain-data ::tiles/tile-doc single)))
    (build-files "stairs.xml" "stairs.png" single)))


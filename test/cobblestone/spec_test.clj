(ns cobblestone.spec-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.edn :as edn]
            [cobblestone.spec :as pixel]
            [clojure.string :as str]))

(def corner-tile
  (str/join
    "|"
    ["aaaaaaaaaaaaaaaa"
     "abbbbbbbbdcbbbbb"
     "abcccdddddcbddbd"
     "abccccddddcddddd"
     "abdccccdddcddddd"
     "abddcccccccccccc"
     "abdddcccbbbbdcbb"
     "abdddcccdddddcdd"
     "abdddcbdccdddcdd"
     "abbddcbdcccccccc"
     "abdddcccccccbbbb"
     "abdddcdddcccdddd"
     "abbddcbddcbccccc"
     "acccccbddcbdccdd"
     "addddcbddcbdcccc"
     "abdddcbddcbdcdcc"]))

(def edge-tile
  (str/join
    "|"
    ["aabddcbddcbdcdcc"
     "aabbdcdddcbdcdcc"
     "bbabdcccccbdcccc"
     "dbbabcbdddbdcdcc"
     "dddbacbddcbdcdcc"
     "cccccbbddccccdcc"
     "dddddbaddcddcdcc"
     "ddddddbadcbdcdbb"
     "dddddcddacbdcdcc"
     "cccccccccdbdcdcc"
     "bdcbbbbbbbadcdcc"
     "ddcddddddddbcdcc"
     "ccccccccccccdbcc"
     "dddddcdddddddbcc"
     "cccccccccccccccc"
     "cccccccccccccccc"]))

(def wall-tile
  (str/join
    "|"
    ["aaaaaaaaaaaaaaaa"
     "bdcbbbbbbdcbbbbb"
     "ddcbddbdddcbddbd"
     "ddcdddddddcddddd"
     "ddcdddddddcddddd"
     "cccccccccccccccc"
     "bbbbdcbbbbbbdcbb"
     "dddddcdddddddcdd"
     "dddddcdddddddcdd"
     "cccccccccccccccc"
     "bdcbbbbbbdcbbbbb"
     "ddcdddddddcddddd"
     "cccccccccccccccc"
     "dddddcdddddddcdd"
     "cccccccccccccccc"
     "cccccccccccccccc"]))

(def floor-tile
  (str/join
    "|"
    ["bcaabaabbcaabaaa"
     "abcaaacbbbcaaaaa"
     "aabcccbaaabcabaa"
     "aabbbbaaaaabcaba"
     "aabbaaaabacbbaaa"
     "acbaabaaacbbbcaa"
     "cbaabaaacbaaabcc"
     "bbabaaacbaaaaabb"
     "bbcaaacbaaaaaacb"
     "aabcccbaaaabacba"
     "aaabbbaaaabacbaa"
     "aaaabaababacbbaa"
     "abaabcaaaacbbbaa"
     "aabaabccccbaabca"
     "aaabaabbbbaaaabc"
     "caaaaaabbaaaaaab"]))

(deftest test-tile
  (is
    (= nil
       (s/explain-data
         ::pixel/tile
         corner-tile
         )))
  (is
    (= nil
       (s/explain-data
         ::pixel/tile-set
         {:corner corner-tile})))
  (is
    (= nil
       (s/explain-data
         ::pixel/tile-set
         {:corner corner-tile
          :wall wall-tile
          :floor floor-tile})))
  )

(deftest test-palette
  (is
    (= nil
       (s/explain-data
         ::pixel/palette
         [:lightgrey :grey :darkgrey])))

  (is
    (= nil
       (s/explain-data
         ::pixel/palette
         [:white :pink :purple :black])))

  (is
    (= nil
       (s/explain-data
         ::pixel/palette-set
         {:floor [:lightgrey :grey :darkgrey]
          :wall [:white :pink :purple :black]}
         )))

  )

(deftest test-tile-listing
  (is
    (= nil
       (s/explain-data
         ::pixel/tile-listing
         [:corner :wall :a/a]))))

(deftest test-tile-listing-1
  (is
    (= nil
       (s/explain-data
         ::pixel/tile-listing
         [:corner :wall :ab/a]))))

(deftest test-doc
  (let [tile-doc
        [{:corner corner-tile
          :wall wall-tile
          :floor floor-tile}
         {:floor [:lightgrey :grey :darkgrey]
          :wall [:white :pink :purple :black]}
         10
         [:corner :wall :a/a]
         [:corner :wall :flip-down :a/h]
         [:corner :wall :flip-over :j/a]
         [:corner :wall :flip-down :flip-over :j/h]
         [:wall :wall :bi/a]
         [:wall :wall :flip-down :bi/h]
         [:wall :wall :turn-left :a/bg]
         [:wall :wall :turn-left :flip-over :j/bg]
         [:floor :floor :bi/bg]]]
    (is
      (= nil
         (s/explain-data
           ::pixel/tile-doc
           tile-doc)))

    (println (pr-str (s/conform ::pixel/tile-doc tile-doc)))))

(def ^:private doc (edn/read-string (slurp "resources/practicetiles/pixel-tiles.edn")))

(deftest test-tiles
  (let [{:keys [cave-wall
                stone-edge
                brick-edge
                block
                floral-stone
                broken-floor
                cave-edge
                shiny-tile
                cave-floor
                stone-corner
                shuttered-door
                stone-floor
                stair-up
                cave-corner
                wet-floor
                stairs
                tile-floor
                wall-braziers
                standing-brazier
                stair-down
                brick-corner
                stone-tile
                statue
                bubble-tile
                stone-wall
                brick-wall]} (first doc)]

    (is (= nil (s/explain-data ::pixel/tile cave-wall)))

    (is (= nil (s/explain-data ::pixel/tile stone-edge)))

    (is (= nil (s/explain-data ::pixel/tile brick-edge)))

    (is (= nil (s/explain-data ::pixel/tile block)))

    (is (= nil (s/explain-data ::pixel/tile floral-stone)))

    (is (= nil (s/explain-data ::pixel/tile broken-floor)))

    (is (= nil (s/explain-data ::pixel/tile shiny-tile)))

    (is (= nil (s/explain-data ::pixel/tile cave-edge)))

    (is (= nil (s/explain-data ::pixel/tile stone-corner)))

    (is (= nil (s/explain-data ::pixel/tile shuttered-door)))

    (is (= nil (s/explain-data ::pixel/tile cave-floor)))

    (is (= nil (s/explain-data ::pixel/tile stone-floor)))

    (is (= nil (s/explain-data ::pixel/tile cave-corner)))

    (is (= nil (s/explain-data ::pixel/tile wet-floor)))

    (is (= nil (s/explain-data ::pixel/tile stairs)))

    (is (= nil (s/explain-data ::pixel/tile tile-floor)))

    (is (= nil (s/explain-data ::pixel/tile wall-braziers)))

    (is (= nil (s/explain-data ::pixel/tile standing-brazier)))

    (is (= nil (s/explain-data ::pixel/tile stair-down)))

    (is (= nil (s/explain-data ::pixel/tile stair-up)))

    (is (= nil (s/explain-data ::pixel/tile brick-corner)))

    (is (= nil (s/explain-data ::pixel/tile stone-tile)))

    (is (= nil (s/explain-data ::pixel/tile statue)))

    (is (= nil (s/explain-data ::pixel/tile bubble-tile)))

    (is (= nil (s/explain-data ::pixel/tile stone-wall)))

    (is (= nil (s/explain-data ::pixel/tile brick-wall)))

    ))
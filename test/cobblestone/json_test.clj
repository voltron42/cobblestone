(ns cobblestone.json-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [cobblestone.json :refer [j2e]]
            [clojure.spec.alpha :as s]
            [cobblestone.spec :as tiles]))

(def ^:private doc (edn/read-string (slurp "practice/practicetiles/pixel-tiles.edn")))

(deftest test-json
  (let [[tiles palettes _ lists] doc]
    (is (= doc (j2e (json/parse-string (json/generate-string [tiles palettes lists]) keyword))))
    (is (= nil (s/explain-data ::tiles/tile-doc-set doc)))
    ))

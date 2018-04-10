(ns cobblestone.core
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.xml :as xml]
            [cobblestone.color :as colors]
            [cobblestone.spec :as pixel])
  (:import (clojure.lang ExceptionInfo)))

(defmulti ^:private process-color first)

(defmethod ^:private process-color :default [[_ str-val]] str-val)

(defmethod ^:private process-color :rgb [[_ {:keys [r g b]}]] (str "rgb(" r "," g "," b ")"))

(defmethod ^:private process-color :named [[_ name-key]] (name name-key))

(defmethod ^:private process-color :pantone [[_ pantone]] (name (get colors/pantone (keyword "pantone" (name pantone)))))

(defmethod ^:private process-color :none [_] nil)

(defmulti ^:private do-action
          (fn [_ action]
            action))

(defmethod ^:private do-action :default [point _] point)

(defmethod ^:private do-action :turn-right
  [{:keys [x y]} _] {:x (- 15 y) :y x})

(defmethod ^:private do-action :turn-left
  [{:keys [x y]} _] {:x y :y (- 15 x)})

(defmethod ^:private do-action :flip-down
  [{:keys [x y]} _] {:x x :y (- 15 y)})

(defmethod ^:private do-action :flip-over
  [{:keys [x y]} _] {:x (- 15 x) :y y})

(def ^:private printer-css
  "@media print {\n    svg {\n        page-break-after: always;\n        margin: 0.25in 0.5in 0.25in 0.5in;\n    }\n    @page {\n        size: landscape;\n        margin: 0;\n    }\n}")

(defn- index [c]
  (- (int c) 97))

(defn- optimize-tile [tile]
  (let [counts (reduce-kv #(assoc %1 %2 (count %3)) {} tile)
        bg (->> counts (sort-by second) last first)]
    {:bg bg
     :pixels (dissoc tile bg)}))

(defn- parse-tile [tile]
  (let [rows (mapv vector (range 16) (str/split tile #"[|]"))
        pixels (reduce
                 (fn [out [y ^String row]]
                   (reduce
                     (fn [out [x c]]
                       (conj out {:c c :x x :y y}))
                     out
                     (mapv #(vector %1 (index %2)) (range 16) row)))
                 []
                 rows)
        pixels (group-by :c pixels)]
    {:pixels (reduce-kv #(assoc %1 %2 (map (fn [p] (select-keys p [:x :y])) %3)) {} pixels)}))

(defn explode-tile [tile]
  (let [{:keys [out]} (reduce
                        (fn [{:keys [out]} row]
                          {:out
                           (if (empty? row)
                             (conj out (last out))
                             (let [row (reduce
                                         (fn [row-out c]
                                           (let [c (try (Integer/parseInt c) (catch Exception _ c))
                                                 c (if (int? c) (apply str (repeat c (last row-out))) c)]
                                             (str row-out c)))
                                         ""
                                         (map str row))
                                   diff (- 16 (count row))
                                   row (apply str row (repeat diff (last row)))]
                               (conj out row)))})
                        {:out []}
                        (str/split tile #"[|]"))
        diff (- 16 (count out))
        out (concat out (repeat diff (last out)))]
    (str/join "|" out)))

(defn- explode-svg [pixel-size defs uses width height]
  (let [tile-size (* 16 pixel-size)
        [width height] (map (partial * tile-size) [width height])]
    {:tag :svg
     :attrs {:width width :height height
             :xmlns "http://www.w3.org/2000/svg"
             :xmlns:xlink "http://www.w3.org/1999/xlink"}
     :content (into [{:tag :defs
                      :content (mapv (fn [{:keys [id bg pixels]}]
                                       (let [background (if (nil? bg) [] [{:tag :rect
                                                                           :attrs {:width tile-size
                                                                                   :height tile-size
                                                                                   :stroke "none"
                                                                                   :fill bg}}])]
                                         {:tag :g
                                          :attrs {:id id}
                                          :content (into background
                                                         (map (fn [{:keys [c x y]}]
                                                                {:tag :rect
                                                                 :attrs {:x (* x pixel-size)
                                                                         :y (* y pixel-size)
                                                                         :width pixel-size
                                                                         :height pixel-size
                                                                         :stroke "none"
                                                                         :fill c}})
                                                              pixels))}))
                                     defs)}]
                    (reduce
                      (fn [out {:keys [id x y]}]
                        (concat out
                                [{:tag :use
                                  :attrs {:x (* x tile-size)
                                          :y (* y tile-size)
                                          :xlink:href (str "#" id)}}
                                 {:tag :rect
                                  :attrs {:x (* x tile-size)
                                          :y (* y tile-size)
                                          :width tile-size
                                          :height tile-size
                                          :stroke "black"
                                          :stroke-width 1
                                          :fill "none"}}
                                 ]))
                      []
                      uses))}))

(defn- span-loc [loc]
  (let [[[min-x max-x] [min-y max-y]] (map (fn [func] (map index (func loc))) [namespace name])
        [max-x max-y] (mapv #(if (nil? %2) %1 %2) [min-x min-y] [max-x max-y])]
    (for [x (range min-x (inc max-x)) y (range min-y (inc max-y))] [x y])))

(defn- condenser [coll truncate-fn concat-fn]
  (loop [items coll
         out ""]
    (let [f (first items)
          r (rest items)
          n (count (take-while (partial = f) r))
          n (truncate-fn n)
          r (drop n r)
          f (if (or (zero? n) (empty? r)) (str f) (str f (concat-fn)))
          out (str out f)]
      (if (empty? r)
        out
        (recur r out)))))

(defn- condense-row [row]
  (condenser row #(if (< 9 %) 9 %) identity))

(defn- condense-rows [rows]
  (condenser rows identity #(repeat (inc %) "|")))

(defn condense-tile [tile]
  (let [tile (str/join "|" (str/split tile #"\n"))
        rows (str/split tile #"[|]")
        rows (mapv condense-row rows)]
    (condense-rows rows)))

(defn pivot-tile [tile]
  (let [tile (explode-tile (str/join "|" (str/split tile #"\n")))]
    (str/join "\n" (apply mapv #(apply str %&) (str/split tile #"[|]")))))

(defn- build-svg-from-exploded-tile-doc [tile-name {:keys [tiles palettes pixel-size list] :or {pixel-size 1}}]
  (let [tiles (reduce-kv #(assoc %1 %2 (parse-tile (explode-tile %3))) {} tiles)
        palettes (reduce-kv #(assoc %1 %2 (mapv process-color %3)) {} palettes)
        list (into (sorted-map) (mapv vector (range) list))
        {:keys [defs uses]} (reduce-kv
                              (fn [& n]
                                (let [
                                      [m k v] n
                                      {:keys [defs uses]} m
                                      index k
                                      {:keys [name palette-name actions locs]} v
                                      actions (if (empty? actions) [] actions)
                                      {:keys [pixels]} (get tiles name)
                                      palette (get palettes palette-name)
                                      {:keys [bg pixels]} (if (some nil? palette) [nil pixels] (optimize-tile pixels))
                                      pixels (reduce-kv #(assoc %1 %2 (mapv (fn [pixel] (reduce do-action pixel actions)) %3)) {} pixels)
                                      id (str tile-name index)
                                      bg (get palette bg)
                                      base (if (nil? bg) {} {:bg bg})]
                                  {:defs (conj defs (assoc base
                                                      :id id
                                                      :pixels (reduce-kv
                                                                #(let [color (get palette %2)]
                                                                   (if-not (nil? color)
                                                                     (concat %1 (mapv (fn [pixel] (assoc pixel :c color)) %3))
                                                                     %1))
                                                                [] pixels)))
                                   :uses (concat uses (reduce (fn [out loc]
                                                                (concat out (mapv (fn [[x y]] {:id id :x x :y y})
                                                                                  (span-loc loc))))
                                                              [] locs))}))
                              {:defs [] :uses []}
                              list)
        [width height] (mapv #(inc (apply max (mapv % uses))) [:x :y])]
    (explode-svg pixel-size defs uses width height)))

(defn build-svg-from-tile-doc [tile-doc]
  (when-let [error (s/explain-data ::pixel/tile-doc tile-doc)]
    (throw (ExceptionInfo. "Error in tile-doc" {:error error})))
  (build-svg-from-exploded-tile-doc "tile" (s/conform ::pixel/tile-doc tile-doc)))

(defn build-svgs-from-tile-docs [tile-docs]
  (when-let [error (s/explain-data ::pixel/tile-doc-set tile-docs)]
    (throw (ExceptionInfo. "Error in tile-doc" {:error error})))
  (let [tile-doc-set (s/conform ::pixel/tile-doc-set tile-docs)
        {:keys [tiles palettes pixel-size docs] :or {pixel-size 1} :as tiles-n-palettes} tile-doc-set
        tile-map (reduce-kv #(assoc %1 %2 {:tiles tiles :palettes palettes :pixel-size pixel-size :list %3}) {} docs)]
    (reduce-kv #(do (println %2) (assoc %1 %2 (build-svg-from-exploded-tile-doc (name %2) %3))) {} tile-map)))

(defn build-html-from-tile-docs-text [in-str]
  (let [docs (edn/read-string in-str)
        svgs (build-svgs-from-tile-docs docs)
        build-page {:tag :html
                    :content [{:tag :head
                               :content [{:tag :style
                                          :content [printer-css]}]}
                              {:tag :body
                               :content (vals svgs)}]}]
    (with-out-str (xml/emit-element build-page))))

(defn -main [& [filename]]
  (let [path (.getParent (io/file filename))
        in-str (slurp path)
        build-html (build-html-from-tile-docs-text in-str)]
  (spit (.getAbsolutePath (io/file path "build.html")) build-html)))

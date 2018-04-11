(ns cobblestone.json)

(defn- to-values [func my-map]
  (reduce-kv #(assoc %1 %2 (func %2 %3)) {} my-map))

(defn j2e [[tiles palettes lists]]
  (let [palettes (to-values #(mapv (fn [c] (if (string? c) (keyword c) c)) %2) palettes)
        lists (to-values #(mapv (partial mapv keyword) %2) lists)]
    [tiles palettes 6 lists]))


(ns tool.zipper
  (:require [clojure.java.io :as io]))

(defn -main [& args]
  (let [english-file (first args)
        chinese-file (second args)
        english-lines (with-open [rdr (io/reader english-file)]
                        (doall (line-seq rdr)))
        chinese-lines (with-open [rdr (io/reader chinese-file)]
                        (doall (line-seq rdr)))
        zipped-lines (map vector english-lines chinese-lines)]
    (doseq [[english chinese] zipped-lines]
      (println english)
      (println chinese))))

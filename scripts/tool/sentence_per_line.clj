(ns tool.sentence-per-line
    (:require [clojure.java.io :as io]
              [ada.ada :as ada]
              [clojure.string :as string]))

(defn end_of_sentence? [sentence]
    (apply #(or (or %1 %2) %3) (map #(string/ends-with? sentence %) ["." "?" "!"])))

(defn handle-paragraph [paragraph]
    (let [sentences (string/split paragraph #"((?<=[.?!])\s+(?=[a-zA-Z]))")]
        (if (> (count sentences) 1)
            (if (end_of_sentence? (last sentences))
                (do (doseq [sentence sentences]
                        (println (.replace (.replace sentence "\n" " ") "  " "\n\n")))
                    (swap! ada/ctx assoc :rest nil))
                (do (doseq [sentence (butlast sentences)]
                        (println (.replace (.replace sentence "\n" " ") "  " "\n\n")))
                    (swap! ada/ctx assoc :rest (last sentences))))
            (do (swap! ada/ctx assoc :rest (last sentences))))))

(defn -main [& args]
  (with-open [rdr (io/reader *in*)]
    (let [input (string/trim (slurp rdr))
          input (if (nil? args) input (str args " " input))
          paragraphs (string/split (.replace (.replace input "\n" " ") "  " "\n\n") #"(\n\n)")]
        (doseq [paragraph paragraphs]
          (handle-paragraph paragraph) (println)))))

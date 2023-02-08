(ns tool.pdf2txt
    (:require [clojure.java.io :as io])
    (:import (org.apache.pdfbox.pdmodel PDDocument)
             (org.apache.pdfbox.text PDFTextStripper))
    (:gen-class))

(defn -main [& args]
  (let [file (PDDocument/load (io/file (first args)))
        pages (.getPages (.getDocumentCatalog file))
        iter (.iterator pages)
        text (StringBuilder.)]
    (while (.hasNext iter)
      (let [page (.next iter)
            doc (PDDocument.)
            stripper (PDFTextStripper.)]
        (.addPage doc page)
        (println (.getText stripper doc))
        (.close doc)))))

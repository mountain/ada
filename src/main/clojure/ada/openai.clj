(ns ada.openai
    (:require [clojure.string :as string]
              [clojure.data.json :as json]
              [org.httpkit.client :as http]))

(def openai-org (.substring (slurp (str (System/getProperty "user.home")
                           (System/getProperty "file.separator")
                           ".ada"
                           (System/getProperty "file.separator")
                           ".openai")) 5 33))

(def openai-key (.substring (slurp (str (System/getProperty "user.home")
                           (System/getProperty "file.separator")
                           ".ada"
                           (System/getProperty "file.separator")
                           ".openai")) 39 91))

(defn correction [callback err-back]

    )

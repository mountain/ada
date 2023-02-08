(ns bot.xiaoyi
    (:require
        [clojure.java.io :as io]
        [clojure.string :as string]
        [clojure.data.json :as json]
        [org.httpkit.client :as http]
        [ada.ada :as ada])
    (:gen-class :main true))


(def xiaoyi-token (string/trim (slurp (str (System/getProperty "user.home")
                           (System/getProperty "file.separator")
                           ".ada"
                           (System/getProperty "file.separator")
                           ".xiaoyi"))))

(def xiaoyi-url "https://api.interpreter.caiyunai.com/v1/translator")

(defn translate [direction paragraphs callback err-back]
    (let [text (json/write-str paragraphs)
          bodystr (str "{\"source\":" text ",\"trans_type\":\"" direction "\",\"detect\":true,\"media\":\"text\",\"request_id\":\"demo\"}")
          bodystr (json/write-str (json/read-str bodystr))]

         (Thread/sleep 1000)
         @(http/request {:url xiaoyi-url :method :post
                   :headers {"Content-Type" "application/json"
                             "User-Agent" "curl/7.87.0"
                             "Accept" "*/*"
                             "X-Authorization" (str "token " xiaoyi-token)}
                   :body bodystr}
            (fn [{:keys [opts status body headers error] :as resp}]
                (if (or error (not (== status 200)))
                    (err-back error {:opt opts :status status :body body :headers headers :error error})
                    (let [result (json/read-str body)
                          translation (string/join "\n" (get result "target"))
                          confidence (get result "confidence")]
                        (if (nil? translation)
                            (err-back "other" {:opt opts :status status :body body :headers headers :error error})
                            (callback translation confidence))))))))

(defn -main [& args]
    (with-open [rdr (io/reader *in*)]
        (let [lines (line-seq rdr)]
          (doseq [chunks (partition 5 5 nil lines)]
            (try
                (translate (nth args 0) chunks
                  (fn [translation confidence]
                    (println translation))
                  (fn [err ctx]
                    (println "error:" err ctx)))
                (catch Exception e (do (println e) (System/exit -1))))))))

(ns bot.davinci
        (:require
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]))

(def openai-org (string/trim (.substring (slurp (str (System/getProperty "user.home")
                           (System/getProperty "file.separator")
                           ".ada"
                           (System/getProperty "file.separator")
                           ".openai")) 5 33)))

(def openai-key (string/trim (.substring (slurp (str (System/getProperty "user.home")
                           (System/getProperty "file.separator")
                           ".ada"
                           (System/getProperty "file.separator")
                           ".openai")) 39 91)))

(def openai-completion-url "https://api.openai.com/v1/completions")

(def correction-prompt-for-english-to-chinese-translation (str
  "The following is a conversation with an AI assistant. The assistant is helpful, creative, clever, and very friendly."
  "\n\nHuman: Hello, who are you?"
  "\nAI: I am an AI created by OpenAI. How can I help you today?"
  "\nHuman: I have a few translation works need to be corrected. I will give you the original text and the translation."
  "\nHuman: You can correct the translation and give me the corrected text, and also give some comments on why you correct it."
  "\nHuman: The translation is from English to Chinese."
  "\nHuman: Here is the format we will take:"
  "\n\nEnglish: He became the earth in which he lay as nutrients leached from his body and his bone mineralised into fossil."
  "\nChinese: 他变成了他赖以生存的土地，营养物质从他的身体中流失，他的骨头变成了化石。"
  "\nCorrection: 他成为了自己所处的土壤，他的身体中的养分流失，骨骼变成了化石。"
  "\nComments: The original translation is grammatically correct, but there are some phrasing and vocabulary choices that could be improved."
  "\nHuman: we begin now:"
  "\n\nEnglish: %s"
  "\nChinese: %s"))

(defn translation-correct [english chinese callback err-back]
    (let [bodystr (json/write-str {
            "model" "text-davinci-003"
            "prompt" (format correction-prompt-for-english-to-chinese-translation english chinese)
            "max_tokens" 1024
            "temperature" 0.9
            "top_p" 0.1})]

         (Thread/sleep 1000)
         @(http/request {:url openai-completion-url :method :post
                   :headers {"Content-Type" "application/json"
                             "Authorization" (str "Bearer " openai-key)}
                   :body bodystr}
            (fn [{:keys [opts status body headers error] :as resp}]
                (if (or error (not (== status 200)))
                    (err-back error {:opt opts :status status :body body :headers headers :error error})
                    (let [result (json/read-str body)
                          texts (string/split (get (nth (get result "choices") 0) "text") #"\n")
                          correction (nth texts 1)
                          comments  (nth texts 2)]
                        (if (nil? correction)
                            (err-back "other" {:opt opts :status status :body body :headers headers :error error})
                            (callback correction comments))))))))


(defn -main [& args]
    (with-open [rdr (io/reader *in*)]
        (let [lines (line-seq rdr)]
          (doseq [chunks (partition 2 2 nil lines)]
            (try
                (if (not (.equals (string/trim (first chunks)) ""))
                    (translation-correct (first chunks) (second chunks)
                      (fn [correction comments]
                        (println (str "Original: " (first chunks)))
                        (println (str "Translation: " (second chunks)))
                        (println correction)
                        (println comments)
                        (println))
                      (fn [err ctx]
                        (println "error:" err ctx)))
                    (do (println)(println)))
                (catch Exception e (do (println e) (System/exit -1))))))))


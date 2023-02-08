(ns ada.main
    (:import [org.gjt.sp.jedit jEdit])
    (:require
        [clojure.string :as string]
        [clojure.tools.cli :as cli]
        [clojure.main :as clj]
        [ada.ada :as ada])
    (:gen-class :main true))

(def cli-options
  ;; An option with a required argument
  [;; A port number (:default is 80)
   ["-p" "--port PORT" "Port number"
    :default 80
    :id :port
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ;; A non-idempotent option (:default is applied first)
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn run-editor []
    (jEdit/main (make-array String 0)))

(defn run-bot [opts]
    (let [arguments   (:arguments opts)
          script      (.replace (get arguments 1) "-" "_")
          separator   (System/getProperty "file.separator")
          script-path (str "scripts" separator "bot" separator script ".clj")
          script-args (subvec arguments 1 (count arguments))]
        (load-file script-path)
        (let [ctx (swap! ada/ctx assoc :rest [])
          rest (if (nil? ctx) [] (:rest ctx))]
          (apply (clj/main script-path) (concat script-args rest)))))

(defn run-tool [opts]
    (let [arguments   (:arguments opts)
          script      (.replace (get arguments 1) "-" "_")
          separator   (System/getProperty "file.separator")
          script-path (str "scripts" separator "tool" separator script ".clj")
          script-args (subvec arguments 1 (count arguments))]
        (load-file script-path)
        (let [ctx (swap! ada/ctx assoc :rest [])
          rest (if (nil? ctx) [] (:rest ctx))]
          (apply (clj/main script-path) (concat script-args rest)))))

(defn -main [& args]
    (let [opts (cli/parse-opts args cli-options)
          errors (:errors opts)]
        (if (not-empty errors)
            (.println *err* (str "!" (string/join "|" errors)))
            (let [arguments   (:arguments opts)
                  command     (get arguments 0)]
                (cond
                    (.equals command "bot") (run-bot opts)
                    (.equals command "tool") (run-tool opts)
                    (.equals command "editor") (run-editor)
                    :else (println "Unknown mode"))))))

(ns clclient.core
  (:require [seesaw.core :as seesaw])
  (:require [seesaw.mig :as mig])
  (:require [clojure.data.json :as json])
  (:require [clj-http.client :as client])
  (:gen-class))

(def default-config-file (clojure.java.io/file (System/getProperty "user.home")
                                               ".clclient.clj"))
(def default-config 
  {
   :server-address "http://127.0.0.1:3000"
   })

(defn write-default-config [default-config-file]
  "Writes the default-config to default-config-file"
  (with-open [writer (clojure.java.io/writer default-config-file)]
    (print-dup default-config writer)))

(defn read-default-config [default-config-file]
  "Returns the the config stored in default-config-file"
  (with-open [reader (java.io.PushbackReader. (clojure.java.io/reader default-config-file))]
    (read reader)))

(defn read-or-write-config [default-config-file]
  "Reads the config file from default-config-file. It writes a default-config if default-config-file does not exist"
  (try
    (read-default-config default-config-file)
    (catch java.io.FileNotFoundException e
      (println "Writing config to " (.getCanonicalPath default-config-file))
      (write-default-config default-config-file)
      default-config)))

(defn get-version-object [server-address]
  "Returns the object from <server-address>/version"
  (json/read-json (:body (client/get (str server-address "/version")))))

(defn main-window [config]
  (let [server-address-text (seesaw/text :text (:server-address config))
        server-status-label (seesaw/label :text "UNKNOWN")
        check-server-button (seesaw/button :text "Check")
        frame (seesaw/frame :title "ClClient"
                            :content (mig/mig-panel
                                      :items
                                      [[(seesaw/label "Configured server") ""]
                                       [server-address-text ""]
                                       ["Status:" ""]
                                       [server-status-label ""]
                                       [check-server-button ""]])

                            ;(seesaw/vertical-panel :items ["First line" (seesaw/horizontal-panel :items ["Configured server:" server-address-text "Status:" server-status-label check-server-button])])
                            )]
    (seesaw/listen check-server-button
                   :action (fn [e]
                             (seesaw/config! server-status-label :text "Checking")
                             (try
                               (when (= (:name (get-version-object (seesaw/config server-address-text :text))) "nodetest")
                                 (seesaw/config! server-status-label :text "Found"))
                               (catch org.apache.http.conn.HttpHostConnectException e
                                 (seesaw/config! server-status-label :text "Refused")))))
    frame))

(defn -main [& args]
  (let [config (read-or-write-config default-config-file)
        window (main-window config)]
    (seesaw/native!)
    (-> window seesaw/pack! seesaw/show!)))
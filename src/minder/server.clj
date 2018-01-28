(ns minder.server
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [uswitch.lambada.core :refer [deflambdafn]]))

(defn handler
  [event]
  {:bodytext (str "here's the content of the tweet: " (:text event))})

(deflambdafn
  minder.server.lambdaPostHandler
  [in out ctx]
  (let [event (json/read (io/reader in) :key-fn keyword)
        res (handler event)]
    (with-open [w (io/writer out)]
      (json/write res w))))


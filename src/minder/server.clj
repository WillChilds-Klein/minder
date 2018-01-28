(ns minder.server
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [uswitch.lambada.core :refer [deflambdafn]]
    [minder.twitter :refer [post-tweet]]))

(defn- abbrv-tweet
  [text]
  (if (> (count text) 240)
    (-> text (subs 0 240) (str "..."))
    text))

(defn handler
  [data]
  (let [text "the commander in queef says: \"%s\""
        text (format text (-> data :text abbrv-tweet))]
    (post-tweet text)
    text))

(deflambdafn
  minder.server.lambdaPostHandler
  [in out ctx]
  (let [event (json/read (io/reader in) :key-fn keyword)
        res (handler event)]
    (with-open [w (io/writer out)]
      (json/write res w))))

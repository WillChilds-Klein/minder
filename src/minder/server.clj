(ns minder.server
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [uswitch.lambada.core :refer [deflambdafn]]
    [minder.twitter :refer [post-tweet]]
    [minder.core :refer [compose-tweet event-underway?]]))

(defn- handler
  [data]
  (when (event-underway?)
    (-> data
        compose-tweet
        post-tweet)
    "success"))

(deflambdafn
  minder.server.lambdaPostHandler
  [in out ctx]
  (let [event (json/read (io/reader in) :key-fn keyword)
        res (handler event)]
    (with-open [w (io/writer out)]
      (json/write res w))))

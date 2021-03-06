(ns minder.server
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [uswitch.lambada.core :refer [deflambdafn]]
    [minder.core :refer [compose-tweet]]
    [minder.twitter :refer [post-tweet]]))

(defn- handler
  [data]
  (-> data
      compose-tweet
      post-tweet))

(deflambdafn
  minder.server.lambdaPostHandler
  [in out ctx]
  (let [event (json/read (io/reader in) :key-fn keyword)
        res (handler event)]
    (with-open [w (io/writer out)]
      (json/write res w))))

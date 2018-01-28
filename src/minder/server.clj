(ns minder.server
 (:require
    [clojure.data.json :as json])
  (:gen-class
    :name minder.server.Handlers
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]
    :methods [[lambdaPostHandler [models.IfttTweet] String]]))

(defn -lambdaPostHandler
  [this tweet]
  (->> {:bodytext (str "here's the content of the tweet: " (.getText tweet))}
       (json/write-str)))
